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
public class ScheduleWorkflowContent {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
    /**
     * 站区表id
     */
	private Integer districtId;
    /**
     * 站点表id
     */
	private Integer stationId;
    /**
     * 岗位表id
     */
	private Integer positionId;
    /**
     * 班次id
     */
	private Integer classId;
    /**
     * 班制id
     */
	private Integer suiteId;

	/**
	 * 流程id
	 */
	private Integer workFlowId;

	/**
	 * 开始时间
	 */
	private Integer startTime;
	/**
	 * 结束时间
	 */
	private Integer endTime;
	
	/**
	 * 描述
	 */
	private String content;
	/**
	 * 颜色
	 */
	private String color;
	/**
	 * 第几行
	 */
	private Integer lineNumber;
}
