package com.evtape.schedule.service;

import com.evtape.schedule.consts.Constants;
import com.evtape.schedule.persistent.Repositories;
import com.evtape.schedule.consts.CacheKeyConstant;
import com.evtape.schedule.domain.BroadcastUrl;
import com.evtape.schedule.domain.UserUrl;
import com.evtape.schedule.tasks.EndingTask;
import com.evtape.schedule.tasks.RecordingTask;
import com.evtape.schedule.tasks.TransformTask;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by holmes1214 on 13/11/2017.
 * 需要控制整个url的生命周期，当有视频流时由上层服务通知，并开始录制，一直到视频结束
 * 把短视频拼接为长视频，并最终分拆成1小时左右一段的段落上传
 */
@Component
@Scope("prototype")
public class UrlRecordManager implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(UrlRecordManager.class);
    public static int oneHour;

    @Value("${hlsdl.config.period}")
    public void setOneHour(int hour) {
        logger.debug("period set to: {}", hour);
        oneHour = hour;
    }

    private ThreadPoolExecutor pool = null;

    @Value("${hlsdl.config.rootPath}")
    private String rootPath;
    @Value("${hlsdl.config.exeName}")
    private String exeName;
    @Value("${hlsdl.config.userVideoDir}")
    private String userVideoDir;
    private BroadcastUrl url;
    @Value("${aliyun.oss.endpoint}")
    private String endpoint;
    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;
    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;
    @Value("${aliyun.oss.bucket}")
    private String bucket;

    @Autowired
    private RedisService redis;


    private boolean active = true;

    public void setUrl(BroadcastUrl url) {
        this.url = url;
    }

    public String getUrl() {
        return url.getUrlString();
    }

    public void setPool(ThreadPoolExecutor pool) {
        this.pool = pool;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public void run() {

        while (true) {
            if (!active) {
                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
            try {
                List<UserUrl> list = Repositories.userUrlRepository.findByUrlString(url.getUrlString());
                if (list == null || list.size() == 0) {
                    logger.info("no user registered this url:{}", url.getUrlString());
                    active = false;
                    continue;
                }
                int[] period = calcRecordingPeriod(list);
                logger.info("rotate recorder : {}", url.getUrlString());
                long startTime = System.currentTimeMillis(), endTime = 0;
                String uuid = "n" + url.getNodeNumber() + "_u" + url.getId() + "_" + RandomStringUtils.randomAlphabetic(4) + "_" + startTime;
                String dir = userVideoDir + Constants.defaultUserId + "/";
                String commandPath = rootPath + exeName;
                RecordingTask recordingTask = new RecordingTask(period, uuid, commandPath, dir, url.getUrlString());
                Future<List<String>> submit = pool.submit(recordingTask);
                redis.hset(CacheKeyConstant.RECORDING_UUID_MAP, url.getUrlString(), uuid);
                List<String> files = null;

                try {
                    if (url.getUrlString().contains("chushou.tv")) {
                        files = submit.get();
                    } else {
                        files = submit.get(oneHour, TimeUnit.SECONDS);
                    }
                } catch (TimeoutException te) {
                    logger.info("ending task: {}", url.getUrlString());
                    EndingTask endingTask = new EndingTask(uuid);
                    Future<Long> endingResult = pool.submit(endingTask);
                    files = submit.get();
                    endingResult.get();
                    endTime = System.currentTimeMillis();
                    redis.hdel(CacheKeyConstant.RECORDING_UUID_MAP, url.getUrlString());
                }
                logger.info("grab files: {}", files);
                if (files.size() == 0) {
                    active = false;
                    continue;
                }
                if (files.size() > 0) {
                    active = true;
                    Map<String, Object> param = new HashMap<>();
                    param.put("userVideoDir", userVideoDir);
                    param.put("endpoint", endpoint);
                    param.put("accessKeyId", accessKeyId);
                    param.put("accessKeySecret", accessKeySecret);
                    param.put("bucket", bucket);
                    param.put("uuid", uuid);
                    param.put("urlList", list);
                    param.put("start", startTime);
                    param.put("end", endTime);
                    param.put("files", files);
                    param.put("redis", redis);
                    param.put("url", url);
                    TransformTask trans = new TransformTask(param);
                    pool.submit(trans);
                }
            } catch (Exception e) {
                logger.error("task error:", e);
            }
        }
    }

    private int[] calcRecordingPeriod(List<UserUrl> list) {
        int[] result = new int[48];
        for (UserUrl url : list) {
            if (url.getOpenForRecord() == 0) {
                continue;
            }
            if (url.getRecordFrom() == null && url.getRecordTo() == null) {
                for (int i = 0; i < result.length; i++) {
                    result[i] = 1;
                }
            }
            if (url.getRecordFrom() == null || url.getRecordTo() == null) {
                continue;
            }
            for (int i = url.getRecordFrom(); i < url.getRecordTo(); i += 30) {
                result[i / 30] = 1;
            }
        }
        return result;
    }

}
