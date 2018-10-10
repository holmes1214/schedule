package com.evtape.schedule.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

/**
 * 离岗记录
 */
@Entity
@Getter
@Setter
@Table(name="biz_schedule_leave")
public class ScheduleLeave {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	/**
	 * 站区id
	 */
	private Integer districtId;
	/**
	 * 排班信息id
	 */
	private Integer scheduleInfoId;
	/**
	 * 离岗类型
	 */
	private Integer leaveType;
	/**
	 * 离岗子类型
	 */
	private Integer subType;
	/**
	 * 离岗工时数
	 */
	private Double leaveHours;
	/**
	 * 离岗备注
	 */
	private String comment;
	/**
	 * 离岗描述
	 */
	private String leaveDesc;
	/**
	 * 用户id
	 */
	private Integer userId;
	/**
	 * 离岗日期
	 */
	private String leaveDateStr;

	private Date createDate;

	/**
	 * 是否替班，如果是请假人，instead=0
	 */
	private Integer instead;
	/**
	 * 替班人id，如果有替班人，则不为空
	 */
	private Integer exchangeUserId;
	/**
	 * 替班人名称
	 */
	private String exchangeUserName;

	/**
	 * 是否计算原工时，大部分离岗情况需要在原工时基础上加减，例如零星事假，但年假或者整日假不计算原工时
	 */
	private Integer countOriginal;
}
