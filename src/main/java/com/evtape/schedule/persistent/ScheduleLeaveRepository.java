package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.ScheduleLeave;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleLeaveRepository extends JpaRepository<ScheduleLeave, Integer> {

}
