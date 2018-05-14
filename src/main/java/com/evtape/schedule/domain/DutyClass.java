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
		@Index(name="IDX_POSITION_ID", columnList="districtId,position"),
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

	private Integer suiteId;

	private Integer userCount;

	private Integer classColor;

	private Integer startTime;

	private Integer endTime;

	private Integer workingLength;

	private Integer restHours;

	private Integer backup;


}
