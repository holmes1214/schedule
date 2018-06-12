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

	/**
	 * 班制名
	 */
	private String dutyName;
	
	/**
	 * 当前启用的suite为1,没启用为0
	 */
	private String isActive;

	/**
	 * 站区id
	 */
	private Integer districtId;

	/**
	 * 站id
	 */
	private Integer stationId;

	/**
	 * 岗位id
	 */
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

	/**
	 * 备班班制，backup值为1
	 */
	private Integer backup;

}
