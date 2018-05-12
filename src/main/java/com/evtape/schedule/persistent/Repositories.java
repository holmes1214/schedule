package com.evtape.schedule.persistent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Repositories {

    public static BroadcastUrlRepository broadcastUrlRepository;

    public static SysConfigRepo sysConfigRepo;

    public static RecordedSegmentRepository recordedSegmentRepository;
    public static SysRecordedFilesRepo sysRecordedFilesRepo;

    public static DownloadLogRepository downloadLogRepository;

    public static UserUrlRepository userUrlRepository;

    public static RecorderUserRepo recorderUserRepo;

    public static SysTenantRepo sysTenantRepo;

    @Autowired
    public void setBroadcastUrlRepository(BroadcastUrlRepository broadcastUrlRepository) {
        Repositories.broadcastUrlRepository = broadcastUrlRepository;
    }

    @Autowired
    public void setSysConfigRepo(SysConfigRepo repo) {
        Repositories.sysConfigRepo = repo;
    }

    @Autowired
    public void setRecordedSegmentRepository(RecordedSegmentRepository recordedSegmentRepository) {
        Repositories.recordedSegmentRepository = recordedSegmentRepository;
    }

    @Autowired
    public void setDownloadLogRepository(DownloadLogRepository downloadLogRepository) {
        Repositories.downloadLogRepository = downloadLogRepository;
    }

    @Autowired
    public void setUrlRepository(UserUrlRepository urlRepository) {
        Repositories.userUrlRepository = urlRepository;
    }


    @Autowired
    public void setRecorderUserRepo(RecorderUserRepo repo) {
        Repositories.recorderUserRepo = repo;
    }

    @Autowired
    public void setSysTenantRepo(SysTenantRepo repo) {
        Repositories.sysTenantRepo = repo;
    }
    @Autowired
    public void setSysRecordedFilesRepo(SysRecordedFilesRepo repo) {
        Repositories.sysRecordedFilesRepo = repo;
    }

}
