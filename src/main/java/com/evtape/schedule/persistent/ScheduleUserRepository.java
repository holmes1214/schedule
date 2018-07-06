package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.ScheduleUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleUserRepository extends JpaRepository<ScheduleUser, Integer> {

    ScheduleUser findBySuiteIdAndWeekNum(Integer suiteId, Integer weekNum);

    List<ScheduleUser> findBySuiteIdOrderByWeekNum(Integer suiteId);
    
    ScheduleUser findBySuiteIdAndWeekNumAndUserId(Integer suiteId, Integer weekNum,Integer userId);
    
    ScheduleUser findBySuiteIdAndUserId(Integer suiteId, Integer userId);


    List<ScheduleUser> findBySuiteId(Integer suiteId);
}
