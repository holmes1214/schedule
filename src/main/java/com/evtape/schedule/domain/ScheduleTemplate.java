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
@Table(name="sys_schedule_template",indexes = {
		@Index(name="IDX_DISTRICT_ID", columnList="districtId,stationId"),
		@Index(name="IDX_SUITE_ID", columnList="suiteId"),
		@Index(name="IDX_POSITION_ID", columnList="districtId,positionId")
})
public class ScheduleTemplate {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	
	private Integer districtId;

	private Integer stationId;

	private Integer positionId;

	private Integer suiteId;

	private Integer dutyClassId;

	private Integer weekNum;

	private Integer dayNum;

	private String cellCode;
}
