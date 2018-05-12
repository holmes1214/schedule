package com.evtape.schedule.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * 用户
 */
@Entity
@Getter
@Setter
@Table(name="biz_user",indexes = {
		@Index(name="IDX_DISTRICT_ID", columnList="districtId,stationId"),
		@Index(name="IDX_POSITION_ID", columnList="districtId,positionId")
})
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	
	private String userName;

	private String phoneNumber;

	private Integer districtId;

	private Integer stationId;

	private Integer positionId;



}
