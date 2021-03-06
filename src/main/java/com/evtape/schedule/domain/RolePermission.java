package com.evtape.schedule.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

/**
 * 角色有哪些权限
 */
@Entity
@Getter
@Setter
@Table(name = "sys_role_permission", indexes = { 
		@Index(name = "IDX_ROLE_ID", columnList = "roleId"),
		@Index(name = "IDX_PERMISSION_ID", columnList = "permissionId")})
public class RolePermission {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	/**
	 * 角色id
	 */
	private Integer roleId;

	private String roleCold;
	/**
	 * 角色名称
	 */
	private String roleName;
	/**
	 * 权限id
	 */
	private Integer permissionId;
	/**
	 * 权限编码
	 */
	private String permissionCode;
	/**
	 * 权限名称
	 */
	private String permissionName;

}
