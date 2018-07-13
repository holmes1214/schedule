package com.evtape.schedule.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

/**
 * 站区
 */
@Entity
@Getter
@Setter
@Table(name = "sys_work_load")
public class WorkLoadReport {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	private String lineNumber;
	private Integer districtId;
	private String districtName;
	private String yearStr;
	private String seasonStr;
	private String monthStr;
	private Double averWorkerCount;
	private Double planedHours;
	private Double actualHours;
	private Double workedRate;
	private Double extraHours;
	private Double offWorkRate;

}
