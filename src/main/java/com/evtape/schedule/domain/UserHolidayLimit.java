package com.evtape.schedule.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * 用户年假数
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
	/**
	 * 线路号
	 */
	private String lineNumber;
	/**
	 * 站区id
	 */
	private Integer districtId;
	/**
	 * 站区名称
	 */
	private String districtName;
	/**
	 * 站点id
	 */
	private Integer stationId;
	/**
	 * 站点名称
	 */
	private String stationName;
	/**
	 * 岗位id
	 */
	private Integer positionId;
	/**
	 * 离岗类型，冗余字段，暂时没用
	 */
	private Integer leaveType;
	/**
	 * 用户id
	 */
	private Integer userId;
	/**
	 * 用户卡号
	 */
	private String userIdCard;
	/**
	 * 用户编码
	 */
	private String userCode;
	/**
	 * 姓名
	 */
	private String userName;
	/**
	 * 年份
	 */
	private String yearStr;
	/**
	 * 年假数
	 */
	private Integer yearlyLimit;
	
}
