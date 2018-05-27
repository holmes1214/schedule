package com.evtape.schedule.web;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.evtape.schedule.consts.ResultCode;
import com.evtape.schedule.consts.ResultMap;
import com.evtape.schedule.domain.Permission;
import com.evtape.schedule.domain.Role;
import com.evtape.schedule.domain.RolePermission;
import com.evtape.schedule.domain.RoleUser;
import com.evtape.schedule.persistent.Repositories;

/**
 * @author ripper 用戶列表
 */
@Controller
@RequestMapping("/role")
public class RoleController {

	/**
	 * 查找用户的权限列表
	 *
	 * @param userId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/permissionlist", method = { RequestMethod.POST, RequestMethod.GET })
	public ResultMap permissionlist(@RequestParam("userId") Integer userId) {
		ResultMap resultMap;
		try {
			resultMap = new ResultMap(ResultCode.SUCCESS);
            RoleUser roleUser = Repositories.roleUserRepository.findByUserId(userId);
			List<RolePermission> rolePermission = Repositories.rolePermissionRepository
					.findByRoleId(roleUser.getRoleId());
			resultMap.setData(rolePermission);
		} catch (Exception e) {
			resultMap = new ResultMap(ResultCode.SERVER_ERROR);
		}
		return resultMap;
	}

	/**
	 * 添加role
	 *
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/addrole", method = { RequestMethod.POST, RequestMethod.GET })
	public ResultMap addrole(@RequestBody Role role) {
		ResultMap resultMap;
		try {
			resultMap = new ResultMap(ResultCode.SUCCESS);
			Repositories.roleRepository.saveAndFlush(role);
		} catch (Exception e) {
			resultMap = new ResultMap(ResultCode.SERVER_ERROR);
		}
		return resultMap;
	}

	/**
	 * 添加permission
	 *
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/addpermission", method = { RequestMethod.POST, RequestMethod.GET })
	public ResultMap addpermission(@RequestBody Permission permission) {
		ResultMap resultMap;
		try {
			resultMap = new ResultMap(ResultCode.SUCCESS);
			Repositories.permissionRepository.saveAndFlush(permission);
		} catch (Exception e) {
			resultMap = new ResultMap(ResultCode.SERVER_ERROR);
		}
		return resultMap;
	}

	/**
	 * 绑定role和permission
	 *
	 * @param permissionId
	 * @param roleId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/bindpermission", method = { RequestMethod.POST, RequestMethod.GET })
	public ResultMap bindpermission(@RequestParam("permissionId") Integer permissionId,
			@RequestParam("roleId") Integer roleId) {
		ResultMap resultMap;
		try {
			resultMap = new ResultMap(ResultCode.SUCCESS);
			Permission permission = Repositories.permissionRepository.getOne(permissionId);
			Role role = Repositories.roleRepository.getOne(roleId);
			RolePermission rolePermission = new RolePermission();
			rolePermission.setPermissionCode(permission.getCode());
			rolePermission.setPermissionId(permission.getId());
			rolePermission.setPermissionName(permission.getName());
			rolePermission.setRoleCold(role.getCode());
			rolePermission.setRoleId(role.getId());
			rolePermission.setRoleName(role.getName());
			Repositories.rolePermissionRepository.saveAndFlush(rolePermission);
		} catch (Exception e) {
			resultMap = new ResultMap(ResultCode.SERVER_ERROR);
		}
		return resultMap;
	}

	/**
	 * 绑定role和user TODO user只能有一个role，绑定之前需要先解绑别的
	 *
	 * @param userId
	 * @param roleId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/binduser", method = { RequestMethod.POST, RequestMethod.GET })
	public ResultMap binduser(@RequestParam("userId") Integer userId, @RequestParam("roleId") Integer roleId) {
		ResultMap resultMap;
		try {
			resultMap = new ResultMap(ResultCode.SUCCESS);
			RoleUser roleUser = Repositories.roleUserRepository.findByUserId(userId);
			if (roleUser == null) {
				roleUser = new RoleUser();
			}
			roleUser.setRoleId(roleId);
			roleUser.setUserId(userId);
			Repositories.roleUserRepository.saveAndFlush(roleUser);
		} catch (Exception e) {
			resultMap = new ResultMap(ResultCode.SERVER_ERROR);
		}
		return resultMap;
	}

}
