package com.evtape.schedule.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

/**
 * 权限字典表
 */
@Entity
@Getter
@Setter
@Table(name="sys_permission")
public class Permission {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	private String code;
	private String name;
}
