package com.evtape.schedule.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * 用户
 */
@Entity
@Getter
@Setter
@Table(name = "biz_user", indexes = { @Index(name = "IDX_DISTRICT_ID", columnList = "districtId,stationId"),
		@Index(name = "IDX_POSITION_ID", columnList = "districtId,positionId") })
public class User {
	//	包括所有用户，管理员，员工，都可以登录，查看自己的排班信息
	//	但是只有站区或站的管理员能填写请假排班等信息
	//	体现在用户角色权限三者的关联上
	
	/**
	 * 主键
	 */
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
	 * 用户名
	 */
	private String userName;

	/**
	 * 电话号，用于登录
	 */
	private String phoneNumber;

	/**
	 * 密码
	 */
	private Integer passWord;

	/**
	 * 是否是补位人员1是0不是，默认0
	 */
	private Integer backup;
	
	/**
	 * 身份证号
	 */
	private Integer idCardNumber;
}
