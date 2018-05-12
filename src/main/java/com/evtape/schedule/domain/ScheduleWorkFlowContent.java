package com.evtape.schedule.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * 排班模板单元格
 */
@Entity
@Getter
@Setter
@Table(name="biz_schedule_work_flow_content",indexes = {
		@Index(name="IDX_DISTRICT_ID", columnList="districtId,workFlowId"),
		@Index(name="IDX_SUITE_ID", columnList="suiteId")
})
public class ScheduleWorkFlowContent {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	
	private Integer districtId;

	private Integer suiteId;

	private Integer dutyClassId;

	private Integer workFlowId;

	private Integer startTime;

	private Integer endTime;

	private String content;

	private String color;

	private Integer lineNumber;
}
