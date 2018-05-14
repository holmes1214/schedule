package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.ScheduleWorkFlow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleWorkflowRepository extends JpaRepository<ScheduleWorkFlow, Integer> {

}
