package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.ScheduleUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleUserRepository extends JpaRepository<ScheduleUser, Integer> {

    ScheduleUser findBySuiteIdAndWeekNum(Integer suiteId, Integer weekNum);

    List<ScheduleUser> findBySuiteIdOrderByWeekNum(Integer suiteId);
    
}
