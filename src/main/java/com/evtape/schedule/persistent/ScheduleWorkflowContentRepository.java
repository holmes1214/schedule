package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.ScheduleWorkflowContent;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleWorkflowContentRepository extends JpaRepository<ScheduleWorkflowContent, Integer> {
	
	List<ScheduleWorkflowContent> findByWorkFlowId(Integer workFlowId);

}
