package com.evtape.schedule.domain;

import java.util.Date;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;

/**
 * 角色有哪些权限
 */
@Entity
@Getter
@Setter
@Table(name = "sys_role_permission", indexes = { @Index(name = "IDX_ROLE_ID", columnList = "roleId"),
		@Index(name = "IDX_PERMISSION_ID", columnList = "permissionId") })
public class RolePermission {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	private Integer roleId;

	private Integer roleCold;

	private Integer roleName;

	private Integer permissionId;

	private Integer permissionCode;

	private Integer permissionName;

}
