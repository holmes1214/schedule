package com.evtape.schedule.tasks;

import com.evtape.schedule.service.UrlRecordManager;
import com.evtape.schedule.util.LocalAppUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created by holmes1214 on 15/11/2017.
 */
public class RecordingTask implements Callable<List<String>> {
    private static Logger logger = LoggerFactory.getLogger(RecordingTask.class);

    final int[] periods;
    final String uuid;
    final long startTime;
    final long endTime;
    final String commandPath;
    final String dir;
    final String url;

    public RecordingTask(int[] periods, String uuid, String commandPath, String dir, String url) {
        this.periods = periods;
        this.uuid = uuid;
        startTime = System.currentTimeMillis();
        endTime = startTime + (UrlRecordManager.oneHour - 1) * 1000;
        this.commandPath = commandPath;
        this.dir = dir;
        this.url = url;
    }

    @Override
    public List<String> call() throws Exception {
        List<String> result = new ArrayList<>();
        Calendar c = Calendar.getInstance(Locale.CHINA);
        c.setTime(new Date(startTime));
        int time = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
        if (periods[time / 30] == 0) {
            logger.info("need not record currently: {},{}", time, periods);
            return result;
        }
        int index = 0;
        while (System.currentTimeMillis() < endTime) {
            String fileName = uuid + index++;
            logger.info("start to record : {}", fileName);
            String cmd = "python {0} -o {1} -O {2} {3}";
            cmd = MessageFormat.format(cmd, commandPath, dir, fileName, url);
            try {
                logger.info("start job: {}", cmd);
                long beginTime = System.currentTimeMillis();
                List<String> output = LocalAppUtil.getOutputFromProgram(cmd);
                if (isOffline(output) || (System.currentTimeMillis() - beginTime < 5000)) {
                    logger.info("live show is not on line: {}", url);
                    break;
                }
            } catch (Exception e) {
                logger.error("抓取视频出现异常", e);
            }
        }
        File folder = new File(dir);
        if (folder.exists()) {
            for (File f : folder.listFiles()) {
                logger.debug("iterate files: {}", f.getAbsoluteFile());
                if (f.getName().startsWith(uuid)) {
                    logger.info("find record: {}", f.getAbsoluteFile());
                    result.add(f.getAbsolutePath());
                }
            }
        }
        Collections.sort(result);
        return result;
    }

    private boolean isOffline(List<String> output) {
        for (String line : output) {
            if (line.contains("live show is not on line")) {
                return true;
            }
        }
        return false;
    }
}
