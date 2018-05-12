package com.evtape.schedule.service;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.UploadFileRequest;
import com.evtape.schedule.consts.CacheKeyConstant;
import com.evtape.schedule.consts.Constants;
import com.evtape.schedule.domain.BroadcastUrl;
import com.evtape.schedule.domain.RecordedSegment;
import com.evtape.schedule.domain.RecorderUser;
import com.evtape.schedule.domain.UserUrl;
import com.evtape.schedule.persistent.Repositories;
import com.evtape.schedule.tasks.EndingTask;
import com.evtape.schedule.tasks.TransformTask;
import com.evtape.schedule.util.LocalAppUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class VideoRecorder implements ApplicationContextAware {

    private static Logger logger = LoggerFactory.getLogger(VideoRecorder.class);
    private ThreadPoolExecutor pool = null;
    @Autowired
    private RedisService redis;
    @Value("${hlsdl.config.rootPath}")
    private String rootPath;
    @Value("${hlsdl.config.exeName}")
    private String exeName;
    @Value("${hlsdl.config.userVideoDir}")
    private String userVideoDir;


    @Value("${aliyun.oss.endpoint}")
    private String endpoint;
    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;
    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;
    @Value("${aliyun.oss.bucket}")
    private String bucket;
    @Value("${aliyun.ecs.number}")
    private int nodeNumber;
    @Value("${spring.profiles.active}")
    private String profileName;

    @Value("${threadpool.core.pool.size}")
    private int corePoolSize;

    @Value("${threadpool.maximum.pool.size}")
    private int maximumPoolSize;

    @Value("${threadpool.keep.alive.time}")
    private long keepAliveTime;

    private LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();

    Map<String, UrlRecordManager> tasks = new HashMap<>();

    private ApplicationContext ctx;

    static final long THREE_DAYS = 3 * 24 * 3600 * 1000;


    @PostConstruct
    public void init() {
        pool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime,
                TimeUnit.SECONDS, workQueue);
        logger.info("开始初始化线程池");
        logger.info("corePoolSize:{}", corePoolSize);
        logger.info("maximumPoolSize:{}", maximumPoolSize);
        logger.info("keepAliveTime:{}", keepAliveTime);
        logger.info("workQueue:{}", workQueue.getClass().getName());
        logger.info("初始化线程池完成......");
//        SysConfig conf = Repositories.sysConfigRepo.findByConfigKey(Constants.TOTAL_NODE_COUNT);
//        int totalNodeCount = Integer.parseInt(conf.getConfigValue());
//        redis.set(CacheKeyConstant.SYS_CONFIG_NODE_COUNT_KEY, conf.getConfigValue());
//        redis.del(CacheKeyConstant.SYS_CONFIG_NODE_COUNT_MAP_KEY);
//        for (int i = 0; i < totalNodeCount; i++) {
//            Long count = Repositories.broadcastUrlRepository.countByNodeNumber(i);
//            redis.zadd(CacheKeyConstant.SYS_CONFIG_NODE_COUNT_MAP_KEY, count.doubleValue(), CacheKeyConstant.SYS_CONFIG_NODE_FIELD_PREFIX + i);
//        }
        File folder = new File("/home/appusr");
        File[] mp4s = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith("mp4");
            }
        });
        for (File mp4 :
                mp4s) {
            logger.info("transforming file : {}", mp4);
            String fileName = mp4.getName();
            String[] parts=fileName.split("_");
            String urlId=parts[1].substring(1);
            String time=parts[3].substring(0,13);
            List<UserUrl> urlList = Repositories.userUrlRepository.findByUrlId(Long.parseLong(urlId));
            DateFormat df = new SimpleDateFormat("yyMMdd_HHmm");
            String fromDate = df.format(new Date(Long.parseLong(time)));
            String fileKey = fromDate + "_" + mp4.getName();
            OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
            UploadFileRequest uploadFileRequest = new UploadFileRequest(bucket, fileKey);
            uploadFileRequest.setUploadFile(mp4.getAbsolutePath());
            uploadFileRequest.setTaskNum(5);
            uploadFileRequest.setPartSize(10 * 1024 * 1024);
            uploadFileRequest.setEnableCheckpoint(true);
            try {
                logger.info("begin to upload: {}", fileKey);
                ossClient.uploadFile(uploadFileRequest);
                logger.info("upload complete: {}", fileKey);
                for(UserUrl url: urlList){
                    RecordedSegment rs = new RecordedSegment();
                    rs.setCreateUserId(url.getCreateUserId());
                    rs.setRecordFrom(new Date());
                    rs.setUrlId(url.getId());
                    rs.setFileKey(fileKey);
                    rs.setFileSize(mp4.length());
                    rs.setStatus(Constants.READY);
                    rs.setUploadedDate(new Date());
                    rs.setTenantId(url.getTenantId());
                    rs.setUrlAlias(url.getUrlAlias());
                    rs.setUrlId(rs.getUrlId());
                    Repositories.recordedSegmentRepository.save(rs);
                }
                if (mp4.exists()) {
                    mp4.delete();
                }
            } catch (Throwable throwable) {
                logger.error("upload error: ", throwable);
            } finally {
                ossClient.shutdown();
            }
        }

    }

    //    @Scheduled(cron = "0 0/5 * * * ?")
    public void cron() {
        if (isWebProfile()) {
            return;
        }
        logger.info("checking new url...");
        List<BroadcastUrl> broadcastUrls = Repositories.broadcastUrlRepository.findByNodeNumber(nodeNumber);
        logger.info(" urls need to be processed : {}", broadcastUrls);

        for (BroadcastUrl url :
                broadcastUrls) {
            if (!tasks.containsKey(url.getUrlString())) {
                if (hasRecordingProcess(url.getUrlString())) {
                    terminateRecording(url);
                }
                UrlRecordManager task = ctx.getBean(UrlRecordManager.class);
                task.setUrl(url);
                task.setPool(pool);
                tasks.put(url.getUrlString(), task);
                pool.execute(task);
                logger.info("start new recorder : {}", url.getUrlString());
            }
        }
    }

    public boolean isWebProfile() {
        if (profileName.equals("web")) {
            return true;
        }
        return false;
    }

    public boolean isProd1Profile() {
        if (profileName.equals("prod1") || profileName.equals("dev")) {
            return true;
        }
        return false;
    }

    private void terminateRecording(BroadcastUrl url) {
        logger.info("terminating : {}", url.getUrlString());
        String uuid = redis.hget(CacheKeyConstant.RECORDING_UUID_MAP, url.getUrlString());
        EndingTask endingTask = new EndingTask(uuid);
        Future<Long> endingResult = pool.submit(endingTask);
        try {
            Long endTime = endingResult.get();
            List<UserUrl> list = Repositories.userUrlRepository.findByUrlString(url.getUrlString());
            List<String> files = new ArrayList<>();
            File dir = new File(userVideoDir + Constants.defaultUserId + "/");
            long start = Long.MAX_VALUE;
            for (File f :
                    dir.listFiles()) {
                if (f.getName() != null && f.getName().startsWith(uuid)) {
                    files.add(f.getAbsolutePath());
                    start = Math.min(start, f.lastModified());
                }
            }
            Map<String, Object> param = new HashMap<>();
            param.put("userVideoDir", userVideoDir);
            param.put("endpoint", endpoint);
            param.put("accessKeyId", accessKeyId);
            param.put("accessKeySecret", accessKeySecret);
            param.put("bucket", bucket);
            param.put("uuid", uuid);
            param.put("urlList", list);
            param.put("start", start);
            param.put("end", endTime);
            param.put("files", files);
            param.put("url", url);
            TransformTask trans = new TransformTask(param);
            pool.submit(trans);
        } catch (Exception e) {
            logger.error("error on termination: {}", uuid);
        }
    }

    private boolean hasRecordingProcess(String url) {
        if (!redis.hexists(CacheKeyConstant.RECORDING_UUID_MAP, url)) {
            return false;
        }
        String uuid = redis.hget(CacheKeyConstant.RECORDING_UUID_MAP, url);
        if (LocalAppUtil.checkProcessLive(uuid)) {
            return true;
        }
        redis.hdel(CacheKeyConstant.RECORDING_UUID_MAP, url);
        return false;
    }

    @Scheduled(cron = "0 0/2 * * * ?")
    public void rotate() {
        if (isWebProfile()) {
            return;
        }
        for (UrlRecordManager task :
                tasks.values()) {
            if (!task.isActive()) {
                synchronized (task) {
                    logger.info("notify task: {}", task.getUrl());
                    task.notify();
                }
            }
        }
    }

    @Autowired
    private UserService userService;

    //    @Scheduled(cron = "0 0 * * * ?")
    public void checkFileDeletable() {
        if (isProd1Profile()) {
            logger.info("check file deletable...");
            long llen = redis.llen(CacheKeyConstant.NEW_CREATED_RS_KEY);
            if (llen > 0) {
                List<String> list = redis.rpop(CacheKeyConstant.NEW_CREATED_RS_KEY, (int) llen);
                logger.info("process new files: {}", list);
                for (String s :
                        list) {
                    long id = Long.parseLong(s);
                    RecordedSegment rs = Repositories.recordedSegmentRepository.findOne(id);
                    String policy = userService.getUserDelPolicy(rs.getCreateUserId());
                    if (Integer.parseInt(policy) == Constants.DEL_AFTER_N_DAYS) {
                        String nDays = userService.getUserNDays(rs.getCreateUserId());
                        int days = nDays == null ? 3 : Integer.parseInt(nDays);
                        double score = System.currentTimeMillis() + days * Constants.ONE_DAY_MILLIS;
                        logger.info("file id {} will be deleted at {}", s, score);
                        redis.zadd(CacheKeyConstant.FILE_EXPIRE_KEY, score, s);
                    }
                }
            }
            double now = System.currentTimeMillis();
            Set<String> list = redis.zRangeByScore(CacheKeyConstant.FILE_EXPIRE_KEY, 0, now);
            logger.info("removing files: {}", list);
            for (String s :
                    list) {
                long id = Long.parseLong(s);
                RecordedSegment rs = Repositories.recordedSegmentRepository.findOne(id);
                rs.setStatus(Constants.COMPLETED);
                Repositories.recordedSegmentRepository.save(rs);
                deleteFromOSS(rs.getFileKey());
                redis.zrem(CacheKeyConstant.FILE_EXPIRE_KEY, s);
            }
        }
    }

    //    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteExpiredFiles() {
        if (isProd1Profile()) {
            logger.info("check file expired...");
            long current = System.currentTimeMillis();
            List<RecorderUser> activeUsers = Repositories.recorderUserRepo.getActiveUsers();
            for (RecorderUser user :
                    activeUsers) {
                if (Constants.DEL_AFTER_DOWNLOAD == user.getDelPolicy()) {
                    long limit = current - Constants.ONE_DAY_MILLIS;
                    List<RecordedSegment> segments = Repositories.recordedSegmentRepository.findByCreateUserIdAndStatus(user.getId(), Constants.READY);
                    for (RecordedSegment segment : segments) {
                        if (segment.getUploadedDate().getTime() < limit) {
                            redis.zadd(CacheKeyConstant.FILE_EXPIRE_KEY, 0d, segment.getId().toString());
                        }
                    }
                } else if (Constants.DEL_AFTER_N_DAYS == user.getDelPolicy()) {
                    long limit = current - Constants.ONE_DAY_MILLIS * user.getReserveDays();
                    List<RecordedSegment> segments = Repositories.recordedSegmentRepository.findByCreateUserIdAndStatus(user.getId(), Constants.READY);
                    for (RecordedSegment segment : segments) {
                        if (segment.getUploadedDate().getTime() < limit) {
                            redis.zadd(CacheKeyConstant.FILE_EXPIRE_KEY, 0d, segment.getId().toString());
                        }
                    }
                }
            }
        }
    }

    private void deleteFromOSS(String key) {
        OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
        ossClient.deleteObject(bucket, key);
        ossClient.shutdown();
    }

    public boolean recording(String url) {
        if (hasRecordingProcess(url)) {
            return true;
        }
        return false;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ctx = applicationContext;
    }


}
