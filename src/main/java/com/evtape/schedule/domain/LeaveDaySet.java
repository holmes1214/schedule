package com.evtape.schedule.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * 离岗字典表
 */
@Entity
@Getter
@Setter
@Table(name="sys_leave_day_set")
public class LeaveDaySet {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	private Integer leaveType;

	private String leaveName;

	private Integer subType;

	private String subName;
	/**
	 * 计算工时数
	 */
	private Integer workingHourCount;

	/**
	 * 是否按小时计时
	 */
	private Integer isHourly;

	/**
	 * 是否增加工时
	 */
	private Integer isPositive;

	private Integer districtId;

}
