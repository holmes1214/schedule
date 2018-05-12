package com.evtape.schedule.tasks;

import com.evtape.schedule.util.LocalAppUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * Created by holmes1214 on 15/11/2017.
 */
public class EndingTask implements Callable<Long> {

    private static Logger logger = LoggerFactory.getLogger(EndingTask.class);
    final String uuid;
    public EndingTask(String uuid){
        this.uuid=uuid;
    }

    @Override
    public Long call() throws Exception {
        do{
            logger.info("finish process: {}",uuid);
            LocalAppUtil.finishProcess(uuid);
            Thread.sleep(2000);
        }while(LocalAppUtil.checkProcessLive(uuid));
        return System.currentTimeMillis();
    }


}
