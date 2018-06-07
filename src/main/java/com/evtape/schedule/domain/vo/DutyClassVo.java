package com.evtape.schedule.domain.vo;

import java.util.List;

import com.evtape.schedule.domain.DutyClass;

import lombok.Getter;
import lombok.Setter;

/**
 * 班次，一个岗有多条数据
 */
@Getter
@Setter
public class DutyClassVo extends DutyClass {

	private List<ScheduleWorkflowVo> scheduleWorkflowVolist;

}
