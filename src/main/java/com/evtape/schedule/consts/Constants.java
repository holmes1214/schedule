package com.evtape.schedule.consts;

public interface Constants {

    Long defaultUserId = 1L;

    int READY = 0;
    int COMPLETED = 100;

    String TOTAL_NODE_COUNT="totalNodeCount";

    int ONE_DAY=3600*24;
    int ONE_DAY_MILLIS=3600*24*1000;
    int DEL_AFTER_DOWNLOAD = 1;
    int DEL_AFTER_N_DAYS=2;
}
