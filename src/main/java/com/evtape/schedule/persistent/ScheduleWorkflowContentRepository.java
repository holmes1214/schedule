package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.ScheduleInfo;
import com.evtape.schedule.domain.ScheduleWorkFlowContent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleWorkflowContentRepository extends JpaRepository<ScheduleWorkFlowContent, Integer> {

}
