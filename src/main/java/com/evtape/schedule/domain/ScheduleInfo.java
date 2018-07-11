package com.evtape.schedule.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * 根据模板生成排班
 */
@Entity
@Getter
@Setter
@Table(name="biz_schedule_info",indexes = {
		@Index(name="IDX_DISTRICT_ID", columnList="districtId")
})
public class ScheduleInfo {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	
	private Integer districtId;

	private Integer stationId;

	private Integer positionId;

	private String positionName;

	private Integer dutySuiteId;

	private Integer dutyClassId;

	private Integer workflowId;

	private String workflowCode;

	private String dutyName;

	private String dutyCode;

	private String cellColor;

	private Integer userId;

	private String userName;
	
	private Integer suiteId;

	private String dateStr;

	private Date scheduleDate;

	private String scheduleWeek;

	private Double workingHours;

	private Date createDate;

	private Date modifyDate;

	//是否包含请假数据
	private Integer modified;

	@Transient
	private List<ScheduleLeave> leaveList;

}
