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
@Table(name="biz_schedule_leave",indexes = {
		@Index(name="IDX_CLASS_ID", columnList="districtId,workFlowId")
})
public class ScheduleLeave {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	
	private Integer districtId;

	private Integer scheduleInfoId;

	private Integer leaveSetId;

	private Integer leaveHours;

	private String comment;

	private String leaveDesc;
}
