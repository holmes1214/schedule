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
	
	private Integer districtId;

	private Integer scheduleInfoId;

	private Integer leaveType;

	private Integer subType;

	private Double leaveHours;

	private String comment;

	private String leaveDesc;

	private Integer userId;

	private String leaveDateStr;

	private Date createDate;

	//是否替班
	private Integer instead;

	private Integer exchangeUserId;

	private String exchangeUserName;

	//是否计算原工时
	private Integer countOriginal;
}
