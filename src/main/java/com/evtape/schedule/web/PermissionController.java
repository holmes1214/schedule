package com.evtape.schedule.web;

import com.evtape.schedule.domain.Permission;
import com.evtape.schedule.domain.RolePermission;
import com.evtape.schedule.domain.User;
import com.evtape.schedule.domain.vo.ResponseBundle;
import com.evtape.schedule.persistent.Repositories;
import com.evtape.schedule.web.auth.Identity;
import com.evtape.schedule.web.base.RolePermissionController;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by lianhai on 2018/6/26.
 */
@RestController
@RequestMapping("/permissions")
public class PermissionController extends RolePermissionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionController.class);


    @GetMapping
    @RequiresRoles("role:admin")
    public ResponseBundle getAllPermissions(@Identity String phoneNumber) {
        Optional<User> user = Optional.ofNullable(Repositories.userRepository.findByPhoneNumber(phoneNumber));
        return user.map(u -> {
            List<Permission> permissions = Repositories.permissionRepository.findAll();
            return new ResponseBundle().success(group(permissions, Lists.newArrayListWithCapacity(0)));
        }).orElseThrow(UnauthenticatedException::new);
    }

    @GetMapping("specific")
    public ResponseBundle getPermissionByUser(@Identity String phoneNumber) {
        Optional<User> user = Optional.ofNullable(Repositories.userRepository.findByPhoneNumber(phoneNumber));
        return user.map(u -> {
            List<RolePermission> rolePermission = new ArrayList<>();
            rolePermission.addAll(Repositories.rolePermissionRepository.findByRoleId(u.getRoleId()));
            return new ResponseBundle().success(rolePermission);
        }).orElseThrow(UnauthenticatedException::new);
    }

}
