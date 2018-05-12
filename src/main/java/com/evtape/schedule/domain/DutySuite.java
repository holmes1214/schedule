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
@Table(name="biz_duty_suite",indexes = {
		@Index(name="IDX_DISTRICT_ID", columnList="districtId,stationId"),
		@Index(name="IDX_POSITION_ID", columnList="districtId,positionId")
})
public class DutySuite {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	
	private String dutyName;

	private Integer districtId;

	private Integer stationId;

	private Integer positionId;

	private Integer maxWorkingHour;

	private Integer minWorkingHour;

	private Integer maxWeeklyRestDays;

	private Integer minWeeklyRestDays;

	private Integer monthlyWorkingHourLimit;

	private Integer yearlyWorkingHourLimit;

	private Integer backup;

}
