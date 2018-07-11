package com.evtape.schedule.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * 岗位字典表
 */
@Entity
@Getter
@Setter
@Table(name = "biz_position", indexes = { @Index(name = "IDX_DISTRICT_ID", columnList = "districtId") })
public class Position {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	/**
	 * 岗位名，身份：管理员、普通职工
	 */
	private String positionName;
	/**
	 * 冗余：user表中含站区id
	 */
	private Integer districtId;
	/**
	 * 是否是补位岗，补位岗是挂在站区下面的，不挂在站下面，1补位，0正常
	 */
	private Integer backupPosition;

}
