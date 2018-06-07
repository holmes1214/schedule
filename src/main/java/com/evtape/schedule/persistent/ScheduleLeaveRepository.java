package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.ScheduleLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleLeaveRepository extends JpaRepository<ScheduleLeave, Integer> {

}
