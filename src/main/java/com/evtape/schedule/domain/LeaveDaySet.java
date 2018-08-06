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

	/**
	 * 离岗类型
	 */
	private Integer leaveType;
	/**
	 * 离岗类型名称
	 */
	private String leaveName;
	/**
	 * 离岗子类型
	 */
	private Integer subType;
	/**
	 * 离岗子类型名称
	 */
	private String subName;

	private String description;

}
