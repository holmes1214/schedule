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
@Table(name="biz_user_holiday",indexes = {
		@Index(name="IDX_LINE_NUMBER", columnList="yearStr,districtId"),
})
public class UserHolidayLimit {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	private String lineNumber;
	
	private Integer districtId;

	private String districtName;

	private Integer stationId;

	private String stationName;

	private Integer positionId;

	private Integer leaveType;

	private Integer userId;

	private String userIdCard;

	private String userCode;

	private String userName;

	private String yearStr;

	private Integer yearlyLimit;
	
}
