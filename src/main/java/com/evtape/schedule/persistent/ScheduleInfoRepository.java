package com.evtape.schedule.persistent;

import java.util.List;

import com.evtape.schedule.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.evtape.schedule.domain.ScheduleInfo;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleInfoRepository extends JpaRepository<ScheduleInfo, Integer> {


    @Query("delete from ScheduleInfo where suiteId=?1 and dateStr>?2")
    void deleteBySuiteIdAndDateStr(Integer suiteId, String dateStr);
    
    void deleteBySuiteId(Integer suiteId);
    
    List<ScheduleInfo> findBySuiteId(Integer suiteId);

    ScheduleInfo findByUserIdAndDateStr(User leaveUser, String dateStr);
}
