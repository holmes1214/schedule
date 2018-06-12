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
@Table(name="biz_schedule_work_flow",indexes = {
		@Index(name="IDX_DISTRICT_ID", columnList="districtId,stationId"),
		@Index(name="IDX_SUITE_ID", columnList="suiteId,classId"),
		@Index(name="IDX_POSITION_ID", columnList="districtId,positionId")
})
public class ScheduleWorkflow {

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
     * 班制id
     */
	private Integer suiteId;

	/**
	 * 流程编号
	 */
	private Integer classId;
	/**
	 * 流程code
	 */
	private String code;
	
}
