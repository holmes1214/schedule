package com.evtape.schedule.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name="biz_position",indexes = {
		@Index(name="IDX_DISTRICT_ID", columnList="districtId,stationId")
})
public class Position {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	
	private String positionName;

	private Integer districtId;

	private Integer stationId;

	private Integer backupPosition;

}
