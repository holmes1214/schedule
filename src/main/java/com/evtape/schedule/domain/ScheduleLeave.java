package com.evtape.schedule.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

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
	
	private Integer districtId;

	private Integer scheduleInfoId;

	private Integer type;

	private Integer subType;

	private Double leaveHours;

	private String comment;

	private String leaveDesc;

	private Integer userId;

	//是否替班
	private Integer instead;
	//是否计算原工时
	private Integer countOriginal;
}
