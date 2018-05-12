package com.evtape.schedule.tasks;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.UploadFileRequest;
import com.evtape.schedule.consts.Constants;
import com.evtape.schedule.domain.BroadcastUrl;
import com.evtape.schedule.domain.RecordedSegment;
import com.evtape.schedule.domain.SysRecordedFiles;
import com.evtape.schedule.domain.UserUrl;
import com.evtape.schedule.exception.BaseException;
import com.evtape.schedule.persistent.Repositories;
import com.evtape.schedule.consts.CacheKeyConstant;
import com.evtape.schedule.service.RedisService;
import com.evtape.schedule.util.ErrorCode;
import com.evtape.schedule.util.LocalAppUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

import javax.transaction.Transactional;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created by holmes1214 on 15/11/2017.
 */
public class TransformTask implements Callable<Long> {

    private static Logger logger = LoggerFactory.getLogger(TransformTask.class);

    private final String uuid;
    private final List<UserUrl> urlList;
    private final BroadcastUrl url;
    private final long start;
    private final long end;
    private final List<String> files;
    private final String userVideoDir;
    private final String endpoint;
    private final String accessKeyId;
    private final String accessKeySecret;
    private final String bucket;
    private final RedisService redis;

    public TransformTask(Map<String,Object> params){
        this.userVideoDir= (String) params.get("userVideoDir");
        this.endpoint= (String) params.get("endpoint");
        this.accessKeyId= (String) params.get("accessKeyId");
        this.accessKeySecret= (String) params.get("accessKeySecret");
        this.bucket= (String) params.get("bucket");
        this.uuid= (String) params.get("uuid");
        this.urlList= (List<UserUrl>) params.get("urlList");
        this.start= (long) params.get("start");
        this.end= (long) params.get("end");
        this.files= (List<String>) params.get("files");
        this.redis= (RedisService) params.get("redis");
        this.url= (BroadcastUrl) params.get("url");
    }

    @Override
    @Transactional
    public Long call() throws Exception {
        if (files == null || files.size() == 0) {
            return null;
        }
        String fileName=null;
        if (files.size() > 1) {
            fileName = combine();
        }
        if (files.size() == 1) {
            fileName = trans();
        }
        if(fileName==null){
            return null;
        }
        File file=new File(fileName);
        String key = uploadOneFile(file);

        if(key!=null){
            SysRecordedFiles sysFile=new SysRecordedFiles();
            sysFile.setRecordFrom(new Date(start));
            sysFile.setUrlId(url.getId());
            sysFile.setFileKey(key);
            sysFile.setFileSize(file.length());
            sysFile.setStatus(Constants.READY);
            sysFile.setUploadedDate(new Date());
            Repositories.sysRecordedFilesRepo.save(sysFile);
            for (UserUrl u : urlList) {
                createRecordedSegment(u, key,file.length(),sysFile);
            }

            file.delete();
        }else {
            throw new BaseException(ErrorCode.UPLOAD_ERROR);
        }
        return System.currentTimeMillis();
    }

    private String combine() throws Exception {
        logger.info("combining files : {}", files);
        Collections.sort(files);
        String fileName = userVideoDir + Constants.defaultUserId + "/" + uuid + ".list";
        String dstName = userVideoDir + Constants.defaultUserId + "/" + uuid + ".mp4";
        BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
        for (String file : files) {
            bw.write("file '" + file + "'");
            bw.newLine();
        }
        bw.flush();
        bw.close();
        try{
            String cmd = "ffmpeg -f concat -i " + fileName + " -c copy " + dstName;
            logger.info("combining files : {}",files);
            LocalAppUtil.getOutputFromProgram(cmd);
            File dstFile = new File(dstName);
            if (dstFile.exists()) {
                new File(fileName).delete();
                for (String f : files) {
                    new File(f).delete();
                }
                return dstName;
            }
        }catch (Exception e){
            logger.error("combining error: ",e);
        }
        throw new BaseException(ErrorCode.FILE_TRANSFORM_ERROR);
    }

    private String trans() {
        logger.info("transforming file : {}", files.get(0));
        String fileName = files.get(0);
        if (!fileName.endsWith("mp4")) {
            String dstName = userVideoDir + Constants.defaultUserId + "/" + uuid + ".mp4";
            LocalAppUtil.transform(fileName, dstName);
            File dstFile = new File(dstName);
            if (dstFile.exists()) {
                new File(fileName).delete();
                return dstName;
            }
            throw new BaseException(ErrorCode.FILE_TRANSFORM_ERROR);
        }
        return fileName;
    }

    public void createRecordedSegment(UserUrl url, String key, Long size,SysRecordedFiles sysFile) {
        logger.info("creating recorded segment : {}", key);
        Date startDate = new Date(start);
        Date endDate = new Date(end);
        if (url.getRecordFrom() != null && url.getRecordTo() != null) {
            Calendar c1 = Calendar.getInstance(Locale.CHINA);
            Calendar c2=Calendar.getInstance(Locale.CHINA);
            c1.setTime(startDate);
            c2.setTime(endDate);
            int startMin=c1.get(Calendar.HOUR_OF_DAY)*60+c1.get(Calendar.MINUTE);
            int endMin=c2.get(Calendar.HOUR_OF_DAY)*60+c2.get(Calendar.MINUTE);
            if(url.getRecordFrom()<url.getRecordTo()){
                if((startMin<url.getRecordFrom()&&endMin<url.getRecordFrom())||(startMin>=url.getRecordTo()&&endMin>url.getRecordTo())){
                    logger.info("not in the recording period: {},{}",url.getCreateUserId(),url.getUrlString());
                    return ;
                }
            }
            if(url.getRecordFrom()>url.getRecordTo()){
                if((startMin>=url.getRecordTo()&&startMin<url.getRecordFrom())&&(endMin>=url.getRecordTo()&&endMin<url.getRecordFrom())){
                    logger.info("not in the recording period: {},{}",url.getCreateUserId(),url.getUrlString());
                    return ;
                }
            }
        }
        RecordedSegment rs = new RecordedSegment();
        rs.setCreateUserId(url.getCreateUserId());
        rs.setRecordFrom(startDate);
        rs.setUrlId(url.getId());
        rs.setFileKey(key);
        rs.setFileSize(size);
        rs.setStatus(Constants.READY);
        rs.setUploadedDate(new Date());
        rs.setTenantId(url.getTenantId());
        rs.setUrlAlias(url.getUrlAlias());
        rs.setUrlId(rs.getUrlId());
        rs.setFilesId(sysFile.getId());
        Repositories.recordedSegmentRepository.save(rs);

        redis.lpush(CacheKeyConstant.NEW_CREATED_RS_KEY,rs.getId().toString());
    }

    @Async
    public String uploadOneFile(File file) {
        DateFormat df = new SimpleDateFormat("yyMMdd_HHmm");
        String fromDate = "unknown";
        if (start>0) {
            fromDate = df.format(new Date(start));
        }
        String fileKey = fromDate + "_" + uuid + ".mp4";
        OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
        UploadFileRequest uploadFileRequest = new UploadFileRequest(bucket, fileKey);
        uploadFileRequest.setUploadFile(file.getAbsolutePath());
        uploadFileRequest.setTaskNum(5);
        uploadFileRequest.setPartSize(10 * 1024 * 1024);
        uploadFileRequest.setEnableCheckpoint(true);
        try {
            logger.info("begin to upload: {}", fileKey);
            ossClient.uploadFile(uploadFileRequest);
            logger.info("upload complete: {}", fileKey);
            return fileKey;
        } catch (Throwable throwable) {
            logger.error("upload error: ", throwable);
        } finally {
            ossClient.shutdown();
        }
        return null;
    }

}
