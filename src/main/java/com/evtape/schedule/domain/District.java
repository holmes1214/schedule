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
@Table(name = "sys_district")
public class District {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	private String lineNumber;
	/**
	 * 站区名
	 */
	private String districtName;

	/**
	 * 站区说明
	 */
	private String content;

	@Transient
	private List<User> managers;
}
