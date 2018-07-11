package com.evtape.schedule.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * 排班模板用户
 */
@Entity
@Getter
@Setter
@Table(name="biz_schedule_user",indexes = {
		@Index(name="IDX_DISTRICT_ID", columnList="districtId,stationId"),
		@Index(name="IDX_SUITE_ID", columnList="districtId,suiteId"),
		@Index(name="IDX_POSITION_ID", columnList="districtId,positionId"),
		@Index(name="IDX_WEEKNUM_ID", columnList="suiteId,weekNum", unique = true),
		@Index(name="IDX_USER_ID", columnList="suiteId,userId", unique = true)
})
public class UserHolidayLimit {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	
	private Integer districtId;

	private Integer stationId;

	private Integer positionId;

	private Integer leaveType;

	private Integer userId;

	private String yearStr;

	private Integer yearlyLimit;
	
}
