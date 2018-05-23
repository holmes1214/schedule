package com.evtape.schedule.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * 班次，一个岗有多条数据
 */
@Entity
@Getter
@Setter
@Table(name="sys_duty_class",indexes = {
		@Index(name="IDX_DISTRICT_ID", columnList="districtId,stationId"),
		@Index(name="IDX_POSITION_ID", columnList="districtId,positionId"),
		@Index(name="IDX_DUTY_SUITE_ID", columnList="districtId,suiteId")
})
public class DutyClass {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	
	private String dutyName;

	private String dutyCode;

	private Integer districtId;

	private Integer stationId;

	private Integer positionId;

	/**
	 * 班制id
	 */
	private Integer suiteId;

	/**
	 * 班次人数
	 */
	private Integer userCount;

	/**
	 * 班次颜色
	 */
	private String classColor;

	/**
	 * 班次几点上班(从零点开始算，第多少分钟开始上班)
	 */
	private Integer startTime;

	/**
	 * 班次几点下班(从零点开始算，第多少分钟下班)
	 */
	private Integer endTime;

	/**
	 * 班次时长
	 */
	private Integer workingLength;

	private Integer restMinutes;

	private Integer relevantClassId;

	@Transient
	private DutyClass relevant;

	private Integer backup;


}
