package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.ScheduleWorkflowContent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleWorkflowContentRepository extends JpaRepository<ScheduleWorkflowContent, Integer> {

}
