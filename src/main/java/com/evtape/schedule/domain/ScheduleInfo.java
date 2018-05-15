package com.evtape.schedule.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

/**
 * 排班模板单元格
 */
@Entity
@Getter
@Setter
@Table(name="biz_schedule_info",indexes = {
		@Index(name="IDX_CLASS_ID", columnList="districtId,dutySuiteId,dutyClassId")
})
public class ScheduleInfo {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	
	private Integer districtId;

	private Integer dutySuiteId;

	private Integer dutyClassId;

	private Integer workFlowId;

	private String workFlowCode;

	private String dutyName;

	private Integer userId;

	private String dateStr;

	private String scheduleWeek;

	private String serialNumber;

	private Date createDate;

	private Date modifyDate;

	private Integer modified;

}
