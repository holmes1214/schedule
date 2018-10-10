package com.evtape.schedule.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * 排班信息
 */
@Entity
@Getter
@Setter
@Table(name="biz_schedule_info",indexes = {
		@Index(name="IDX_DISTRICT_ID", columnList="districtId,scheduleDate"),
		@Index(name="IDX_USER_DATE", columnList="userId,scheduleDate,version",unique = true),

})
public class ScheduleInfo {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	/**
	 * 站区id
	 */
	private Integer districtId;
	/**
	 * 站点id
	 */
	private Integer stationId;
	/**
	 * 岗位id
	 */
	private Integer positionId;
	/**
	 * 岗位名称
	 */
	private String positionName;
	/**
	 * 班次id
	 */
	private Integer dutyClassId;
	/**
	 * 工作流程id
	 */
	private Integer workflowId;
	/**
	 * 工作流程编码
	 */
	private String workflowCode;
	/**
	 * 班次名称
	 */
	private String dutyName;
	/**
	 * 班次编号
	 */
	private String dutyCode;
	/**
	 * 单元格颜色
	 */
	private String cellColor;
	/**
	 * 用户id
	 */
	private Integer userId;
	/**
	 * 用户名称
	 */
	private String userName;
	/**
	 * 班制id
	 */
	private Integer suiteId;
	/**
	 * 排班日期
	 */
	private String dateStr;
	/**
	 * 排班日期
	 */
	private Date scheduleDate;
	/**
	 * 排班星期
	 */
	private String scheduleWeek;
	/**
	 * 工时数
	 */
	private Double workingHours;

	private Date createDate;

	private Date modifyDate;

	/**
	 * 是否有离岗记录
	 */
	private Integer modified;

	/**
	 * 版本号默认0
	 */
	private Integer version;

	@Transient
	private List<ScheduleLeave> leaveList;

}
