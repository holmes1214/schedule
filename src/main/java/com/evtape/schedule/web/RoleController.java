package com.evtape.schedule.web;

import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.beust.jcommander.internal.Lists;
import com.evtape.schedule.domain.*;
import com.evtape.schedule.domain.form.RoleForm;
import com.evtape.schedule.domain.vo.ResponseBundle;
import com.evtape.schedule.util.PinyinUtils;
import com.evtape.schedule.web.base.RolePermissionController;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import com.evtape.schedule.persistent.Repositories;

/**
 * @author ripper 用戶列表
 */
@RestController
@RequestMapping("/role")
public class RoleController extends RolePermissionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoleController.class);

    @GetMapping
    @RequiresRoles(value = {"role:admin", "role:district"}, logical = Logical.OR)
    public ResponseBundle getRoles() {
        Subject currentUser = SecurityUtils.getSubject();
        if (currentUser.hasRole("role:admin")) {
            List<Role> roles = Repositories.roleRepository.findAll();
            return new ResponseBundle().success(roles);
        }
        if (currentUser.hasRole("role:district")) {
            List<Role> roles = Repositories.roleRepository.findByCodeNot("role:admin");
            return new ResponseBundle().success(roles);
        }
        throw new UnauthenticatedException();
    }

    @ApiOperation(value = "添加角色 ", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "角色名称", required = true, paramType = "body", dataType = "string"),
            @ApiImplicitParam(name = "description", value = "角色描述", required = false, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "permissionIds", value = "权限ids", required = true, paramType = "body", dataType =
                    "integer[]")
    })
    @PostMapping
    @RequiresRoles("role:admin")
    public ResponseBundle addRole(@RequestBody RoleForm form) {
        Role role = new Role();
        role.setName(form.getName());
        role.setCode(PinyinUtils.getPinYin("role:", form.getName()));
        role.setDescription(StringUtils.isBlank(form.getDescription()) ? "---" : form.getDescription());
        role = Repositories.roleRepository.saveAndFlush(role);
        List<RolePermission> rolePermissions = Lists.newArrayList();
        for (int i = 0; i < form.getPermissionIds().length; i++) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setPermissionId(form.getPermissionIds()[i]);
            rolePermission.setRoleId(role.getId());
            rolePermissions.add(rolePermission);
        }
        Repositories.rolePermissionRepository.save(rolePermissions);
        return new ResponseBundle().success();
    }

    @ApiOperation(value = "获取当前用户角色权限信息 ", produces = "application/json")
    @ApiImplicitParam(name = "roleId", value = "角色id", required = true, paramType = "path", dataType =
            "integer")
    @GetMapping("/{roleId}")
    @RequiresRoles("role:admin")
    public ResponseBundle getRolePermissions(@PathVariable Integer roleId) {
        Role role = Repositories.roleRepository.findOne(roleId);
        List<RolePermission> rolePermissions = Repositories.rolePermissionRepository.findByRoleId(roleId);
        List<Integer> selectedIds = rolePermissions.stream().map(rolePermission -> rolePermission.getPermissionId())
                .collect(Collectors.toList());
        List<Permission> permissions = Repositories.permissionRepository.findAll();
        Map<String, Map<String, List<RolePermissionController.P>>> map = group(permissions, selectedIds);
        JSONObject json = new JSONObject();
        json.put("role", role);
        json.put("permissions", map);
        return new ResponseBundle().success(json);
    }

    @ApiOperation(value = "更新角色权限信息 ", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roleId", value = "角色id", required = true, paramType = "path", dataType =
                    "integer"),
            @ApiImplicitParam(name = "name", value = "角色名称", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "description", value = "角色描述", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "permissionIds", value = "权限ids", required = true, paramType = "body",
                    dataType =
                            "integer[]")
    })
    @PutMapping("/{roleId}")
    @RequiresAuthentication
    public ResponseBundle updateRole(@PathVariable("roleId") Integer id, @RequestBody RoleForm form) {
        Role role = Repositories.roleRepository.findOne(id);
        role.setName(form.getName());
        role.setDescription(role.getDescription());
        Repositories.roleRepository.save(role);
        List<RolePermission> rolePermissions = Repositories.rolePermissionRepository.findByRoleId(id);
        Repositories.rolePermissionRepository.delete(rolePermissions);

        List<RolePermission> newRolePermissions = new ArrayList<>();
        for (int i = 0; i < form.getPermissionIds().length; i++) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(role.getId());
            rolePermission.setPermissionId(form.getPermissionIds()[i]);
            newRolePermissions.add(rolePermission);
        }
        Repositories.rolePermissionRepository.save(newRolePermissions);
        return new ResponseBundle().success();
    }

}
