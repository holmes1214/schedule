package com.evtape.schedule.domain.vo;

import java.util.List;

import com.evtape.schedule.domain.ScheduleWorkflow;
import com.evtape.schedule.domain.ScheduleWorkflowContent;

import lombok.Getter;
import lombok.Setter;

/**
 * 排班模板单元格
 */
@Getter
@Setter
public class ScheduleWorkflowVo extends ScheduleWorkflow {

	List<ScheduleWorkflowContent> contentlist;
	
}
