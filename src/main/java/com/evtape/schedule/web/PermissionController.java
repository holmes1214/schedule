package com.evtape.schedule.web;

import com.evtape.schedule.domain.Permission;
import com.evtape.schedule.domain.RolePermission;
import com.evtape.schedule.domain.RoleUser;
import com.evtape.schedule.domain.User;
import com.evtape.schedule.domain.vo.ResponseBundle;
import com.evtape.schedule.persistent.Repositories;
import com.evtape.schedule.web.auth.Identity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Created by lianhai on 2018/6/26.
 */
@RestController
@RequestMapping("/permissions")
public class PermissionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionController.class);

    private Map<String, Map<String, List<P>>> group(List<Permission> permissions) {

        Map<String, Map<String, List<P>>> levelMap = new HashMap<>();

        permissions.forEach(permission -> {

            String levelKey = permission.getLevel();
            String categoryKey = permission.getCategory();

            if (levelMap.containsKey(levelKey)) {
                // category为key
                Map<String, List<P>> categoryMap = levelMap.get(levelKey);
                if (categoryMap.containsKey(categoryKey)) {
                    // 若categoryMap中存在category的key, 则对应的value一定存在, 直接put
                    categoryMap.get(categoryKey).add(new P(permission.getId(), permission.getName()));
                } else {
                    List<P> nameList = new ArrayList<>();
                    nameList.add(new P(permission.getId(), permission.getName()));
                    categoryMap.put(categoryKey, nameList);
                }
            } else {
                Map<String, List<P>> categoryMap = new HashMap<>();
                List<P> nameList = new ArrayList<>();
                nameList.add(new P(permission.getId(), permission.getName()));
                categoryMap.put(categoryKey, nameList);
                levelMap.put(levelKey, categoryMap);
            }
        });
        return levelMap;
    }

    @GetMapping
    @RequiresRoles("role:admin")
    public ResponseBundle getAllPermissions(@Identity String phoneNumber) {
        Optional<User> user = Optional.ofNullable(Repositories.userRepository.findByPhoneNumber(phoneNumber));
        return user.map(u -> {
            List<Permission> permissions = Repositories.permissionRepository.findAll();
            return new ResponseBundle().success(group(permissions));
        }).orElseThrow(UnauthenticatedException::new);
    }

    @GetMapping("specific")
    public ResponseBundle getPermissionByUser(@Identity String phoneNumber) {
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

    @AllArgsConstructor
    @Getter
    private class P {
        int id;
        String name;
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
