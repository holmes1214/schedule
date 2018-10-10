package com.evtape.schedule.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * 排班模板单元格
 */
@Entity
@Getter
@Setter
@Table(name="biz_schedule_template",indexes = {
		@Index(name="IDX_DISTRICT_ID", columnList="districtId,stationId"),
		@Index(name="IDX_SUITE_ID", columnList="suiteId,weekNum,dayNum"),
		@Index(name="IDX_SUITEONLY_ID", columnList="suiteId"),
		@Index(name="IDX_CLASS_ID", columnList="suiteId,classId"),
		@Index(name="IDX_POSITION_ID", columnList="districtId,positionId")
})
public class ScheduleTemplate {

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
	 * 班制id
	 */
	private Integer suiteId;
	/**
	 * 班次id
	 */
	private Integer classId;
	/**
	 * 模板单元格所在周序号
	 */
	private Integer weekNum;
	/**
	 * 模板单元格所在日序号
	 */
	private Integer dayNum;
	/**
	 * 工作时长，单位分钟
	 */
	private Integer workingLength;
	/**
	 * 单元格在班制模板中的排序
	 */
	private Integer orderIndex;
	/**
	 * 单元格颜色
	 */
	private String cellColor;
	/**
	 * 工作流程id
	 */
	private Integer workflowId;
	/**
	 * 工作流程编号
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
}
