package com.evtape.schedule.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

/**
 * 工时统计报表
 */
@Entity
@Getter
@Setter
@Table(name = "sys_work_load", indexes = { @Index(name = "IDX_YEAR", columnList = "yearStr,districtId"),
		@Index(name = "IDX_LINE_NUMBER", columnList = "lineNumber") })
public class WorkLoadReport {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	/**
	 * 线路
	 */
	private String lineNumber;
	/**
	 * 站区id
	 */
	private Integer districtId;
	/**
	 * 站区名称
	 */
	private String districtName;
	/**
	 * 年份
	 */
	private String yearStr;
	/**
	 * 季度
	 */
	private String seasonStr;
	/**
	 * 月度
	 */
	private String monthStr;
	/**
	 * 平均出勤人数 所有上过班的人数
	 */
	private Integer averWorkerCount;
	/**
	 * 计划工时  上班总工时
	 */
	private Double plannedHours;
	/**
	 * 实际工时  实际出勤工时
	 */
	private Double actualHours;
	/**
	 * 出勤率  实际/计划
	 */
	private Double workedRate;
	/**
	 * 加班率  加班数据/计划  负数为0
	 */
	private Double extraHours;
	/**
	 * 缺勤率
	 */
	private Double offWorkRate;

}
