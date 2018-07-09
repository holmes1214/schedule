package com.evtape.schedule.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * 班制的检查规则，每天几点到几点，应该有多少人在岗，一个岗，有多条数据
 */
@Entity
@Getter
@Setter
@Table(name="sys_duty_period",indexes = {
		@Index(name="IDX_SUITE_ID", columnList="suiteId")
})
public class DutyPeriodChecking {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	/**
	 * 班制id
	 */
	private Integer suiteId;
	/**
	 * 班次几点上班-分钟
	 */
	private Integer startTime;
	/**
	 * 班次几点下班-分钟
	 */
	private Integer endTime;
	/**
	 * 班次几点上班-小时
	 */
    private String startTimeStr;
	/**
	 * 班次几点下班-小时
	 */
    private String endTimeStr;
	/**
	 * 人数
	 */
    private Integer userCount;
}
