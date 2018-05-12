package com.evtape.schedule.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * 站区
 */
@Entity
@Getter
@Setter
@Table(name="sys_district")
public class District {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	
	private String districtName;

	private String phoneNumber;

	private Integer districtId;

}
