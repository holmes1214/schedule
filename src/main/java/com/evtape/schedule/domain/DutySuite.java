package com.evtape.schedule.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * 班制
 */
@Entity
@Getter
@Setter
@Table(name = "biz_duty_suite", indexes = { @Index(name = "IDX_DISTRICT_ID", columnList = "districtId,stationId"),
		@Index(name = "IDX_POSITION_ID", columnList = "districtId,positionId") })
public class DutySuite {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	private String dutyName;

	private Integer districtId;

	private Integer stationId;

	private Integer positionId;

	/**
	 * 每周最大工时
	 */
	private Integer maxWorkingHour;
	/**
	 * 每周最小工时
	 */
	private Integer minWorkingHour;

	/**
	 * 每周最多休几天
	 */
	private Integer maxWeeklyRestDays;
	/**
	 * 每周最少休几天
	 */
	private Integer minWeeklyRestDays;
	/**
	 * 每月最大工时数
	 */
	private Integer monthlyWorkingHourLimit;
	/**
	 * 每年最大工时数
	 */
	private Integer yearlyWorkingHourLimit;

	private Integer backup;

}
