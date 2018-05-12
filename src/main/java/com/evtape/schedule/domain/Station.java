package com.evtape.schedule.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;


/**
 * 站点
 */
@Entity
@Getter
@Setter
@Table(name="sys_station",indexes = {
		@Index(name="IDX_DISTRICT_ID", columnList="districtId")
})
public class Station {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	
	private String stationName;

	private Integer districtId;

}
