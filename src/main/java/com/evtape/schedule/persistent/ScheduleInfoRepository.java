package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.ScheduleInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ScheduleInfoRepository extends JpaRepository<ScheduleInfo, Integer> {


    @Query("delete from ScheduleInfo where suiteId=?1 and dateStr>?2")
    void deleteBySuiteIdAndDateStr(Integer suiteId, String dateStr);
}
