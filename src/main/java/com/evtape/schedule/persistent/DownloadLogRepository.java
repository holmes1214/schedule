package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.DownloadLog;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface DownloadLogRepository extends PagingAndSortingRepository<DownloadLog, Long>,JpaSpecificationExecutor<DownloadLog> {
    @Query(nativeQuery = true,value = "select sum(file_size) from download_log where create_user_id=?1 and DATE(download_time)>= ?2 and DATE(download_time)<= ?3")
    List<Number> getUserMonthlyDownload(Long id, String from, String to);
    @Query(nativeQuery = true,value = "select sum(file_size) from download_log where tenant_id=?1 and DATE(download_time)>= ?2 and DATE(download_time)<= ?3")
    List<Number> getTenantMonthlyDownload(Long id, String from, String to);
}
