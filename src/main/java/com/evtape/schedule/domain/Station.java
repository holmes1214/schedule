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
@Table(name="biz_station",indexes = {
		@Index(name="IDX_DISTRICT_ID", columnList="districtId")
})
public class Station {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	
	/**
	 * 站点名
	 */
	private String stationName;

	/**
	 * 站点所在站区
	 */
	private Integer districtId;

}
