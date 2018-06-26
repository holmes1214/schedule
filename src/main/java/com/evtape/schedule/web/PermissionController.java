package com.evtape.schedule.web;

import com.evtape.schedule.domain.Permission;
import com.evtape.schedule.domain.RolePermission;
import com.evtape.schedule.domain.RoleUser;
import com.evtape.schedule.domain.User;
import com.evtape.schedule.domain.vo.ResponseBundle;
import com.evtape.schedule.persistent.Repositories;
import com.evtape.schedule.web.auth.Identity;
import org.apache.shiro.authz.UnauthenticatedException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by lianhai on 2018/6/26.
 */
@RestController
@RequestMapping("/permission")
public class PermissionController {

    @GetMapping
    public ResponseBundle getPermissions(@Identity String phoneNumber) {
        Optional<User> user = Optional.ofNullable(Repositories.userRepository.findByPhoneNumber(phoneNumber));
        return user.map(u -> {
            List<RoleUser> roleUsers = Repositories.roleUserRepository.findByUserId(u.getId());
            List<RolePermission> rolePermission = new ArrayList<>();
            for (RoleUser roleUser : roleUsers) {
                rolePermission.addAll(Repositories.rolePermissionRepository.findByRoleId(roleUser.getRoleId()));
            }
            return new ResponseBundle().success(rolePermission);
        }).orElseThrow(UnauthenticatedException::new);
    }

//    @PostMapping
//    public ResponseBundle addPermission(@RequestBody Permission permission, @Identity String phoneNumber) {
//        Optional<User> user = Optional.ofNullable(Repositories.userRepository.findByPhoneNumber(phoneNumber));
//        return user.map(u -> {
//            Repositories.permissionRepository.saveAndFlush(permission);
//            return new ResponseBundle().success();
//        }).orElseThrow(UnauthenticatedException::new);
//    }
}
