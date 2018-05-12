package com.evtape.schedule.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * 班制时间段验证
 */
@Entity
@Getter
@Setter
@Table(name="sys_duty_suite",indexes = {
		@Index(name="IDX_DISTRICT_ID", columnList="districtId,stationId"),
		@Index(name="IDX_SUITE_ID", columnList="suiteId"),
		@Index(name="IDX_POSITION_ID", columnList="districtId,positionId")
})
public class DutyPeriodChecking {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	
	private Integer districtId;

	private Integer stationId;

	private Integer positionId;

	private Integer suiteId;

	private Integer backup;

}
