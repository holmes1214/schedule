package com.evtape.schedule.web;

import java.util.List;
import java.util.Optional;

import com.evtape.schedule.domain.*;
import com.evtape.schedule.domain.vo.ResponseBundle;
import com.evtape.schedule.web.auth.Identity;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.annotation.Logical;
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
public class RoleController {

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

    @PostMapping
    @RequiresRoles("role:admin")
    public ResponseBundle addRole(@RequestBody Role role, @Identity String phoneNumber) {
        Optional<User> user = Optional.ofNullable(Repositories.userRepository.findByPhoneNumber(phoneNumber));
        return user.map(u -> {
            Repositories.roleRepository.saveAndFlush(role);
            return new ResponseBundle().success();
        }).orElseThrow(UnauthenticatedException::new);
    }


//    @GetMapping("/permission")
//    public ResponseBundle permissionlist(@Identity String phoneNumber) {
//        Optional<User> user = Optional.ofNullable(Repositories.userRepository.findByPhoneNumber(phoneNumber));
//        return user.map(u -> {
//            List<RoleUser> roleUsers = Repositories.roleUserRepository.findByUserId(u.getId());
//            List<RolePermission> rolePermission = new ArrayList<>();
//            for (RoleUser roleUser : roleUsers) {
//                rolePermission.addAll(Repositories.rolePermissionRepository.findByRoleId(roleUser.getRoleId()));
//            }
//            return new ResponseBundle().success(rolePermission);
//        }).orElseThrow(UnauthenticatedException::new);
//    }

    //    /**
//     * 查找用户的权限列表
//     *
//     * @param userId
//     * @return
//     */
//    @ResponseBody
//    @RequestMapping(value = "/permissionlist", method = {RequestMethod.POST, RequestMethod.GET})
//    public ResultMap permissionlist(@RequestParam("userId") Integer userId) {
//        ResultMap resultMap;
//        try {
//            resultMap = new ResultMap(ResultCode.SUCCESS);
//            List<RoleUser> roleUsers = Repositories.roleUserRepository.findByUserId(userId);
//            List<RolePermission> rolePermission = new ArrayList<>();
//            for (RoleUser roleUser : roleUsers) {
//                rolePermission.addAll(Repositories.rolePermissionRepository.findByRoleId(roleUser.getRoleId()));
//            }
//            resultMap.setData(rolePermission);
//        } catch (Exception e) {
//            resultMap = new ResultMap(ResultCode.SERVER_ERROR);
//        }
//        return resultMap;
//    }
//    @RequestMapping(value = "/addrole", method = {RequestMethod.POST, RequestMethod.GET})

//    /**
//     * 添加role
//     *
//     * @return
//     */
//    @ResponseBody
//    @RequestMapping(value = "/addrole", method = {RequestMethod.POST, RequestMethod.GET})
//    public ResultMap addrole(@RequestBody Role role) {
//        ResultMap resultMap;
//        try {
//            resultMap = new ResultMap(ResultCode.SUCCESS);
//            Repositories.roleRepository.saveAndFlush(role);
//        } catch (Exception e) {
//            resultMap = new ResultMap(ResultCode.SERVER_ERROR);
//        }
//        return resultMap;
//    }

//    /**
//     * 添加permission
//     *
//     * @return
//     */
//    @ResponseBody
//    @RequestMapping(value = "/addpermission", method = {RequestMethod.POST, RequestMethod.GET})
//    public ResultMap addpermission(@RequestBody Permission permission) {
//        ResultMap resultMap;
//        try {
//            resultMap = new ResultMap(ResultCode.SUCCESS);
//            Repositories.permissionRepository.saveAndFlush(permission);
//        } catch (Exception e) {
//            resultMap = new ResultMap(ResultCode.SERVER_ERROR);
//        }
//        return resultMap;
//    }


//    @PutMapping("/permission/relation")
//    public ResponseBundle bindpermission(@RequestParam("permissionId") Integer permissionId,
//                                         @RequestParam("roleId") Integer roleId, @Identity String phoneNumber) {
//
//        Optional<User> user = Optional.ofNullable(Repositories.userRepository.findByPhoneNumber(phoneNumber));
//        return user.map(u -> {
//            Permission permission = Repositories.permissionRepository.findOne(permissionId);
//            Role role = Repositories.roleRepository.findOne(roleId);
//            RolePermission rolePermission = new RolePermission();
//            rolePermission.setPermissionCode(permission.getCode());
//            rolePermission.setPermissionId(permission.getId());
//            rolePermission.setPermissionName(permission.getName());
//            rolePermission.setRoleCold(role.getCode());
//            rolePermission.setRoleId(role.getId());
//            rolePermission.setRoleName(role.getName());
//            Repositories.rolePermissionRepository.saveAndFlush(rolePermission);
//            return new ResponseBundle().success();
//        }).orElseThrow(UnauthenticatedException::new);
//    }


//    /**
//     * 绑定role和permission
//     *
//     * @param permissionId
//     * @param roleId
//     * @return
//     */
//    @ResponseBody
//    @RequestMapping(value = "/bindpermission", method = {RequestMethod.POST, RequestMethod.GET})
//    public ResultMap bindpermission(@RequestParam("permissionId") Integer permissionId,
//                                    @RequestParam("roleId") Integer roleId) {
//        ResultMap resultMap;
//        try {
//            resultMap = new ResultMap(ResultCode.SUCCESS);
//            Permission permission = Repositories.permissionRepository.getOne(permissionId);
//            Role role = Repositories.roleRepository.getOne(roleId);
//            RolePermission rolePermission = new RolePermission();
//            rolePermission.setPermissionCode(permission.getCode());
//            rolePermission.setPermissionId(permission.getId());
//            rolePermission.setPermissionName(permission.getName());
//            rolePermission.setRoleCold(role.getCode());
//            rolePermission.setRoleId(role.getId());
//            rolePermission.setRoleName(role.getName());
//            Repositories.rolePermissionRepository.saveAndFlush(rolePermission);
//        } catch (Exception e) {
//            resultMap = new ResultMap(ResultCode.SERVER_ERROR);
//        }
//        return resultMap;
//    }

//    @PutMapping("/user/relation")
//    public ResponseBundle binduser(@RequestParam("userId") Integer userId, @RequestParam("roleId") Integer roleId,
//                                   @RequestParam("action") Integer action, @Identity String phoneNumber) {
//        Optional<User> user = Optional.ofNullable(Repositories.userRepository.findByPhoneNumber(phoneNumber));
//        return user.map(u -> {
//            List<RoleUser> roleUsers;
//            if (action == 0) {
//                RoleUser roleUser = new RoleUser();
//                roleUser.setRoleId(roleId);
//                roleUser.setUserId(userId);
//                Repositories.roleUserRepository.saveAndFlush(roleUser);
//                roleUsers = Repositories.roleUserRepository.findByUserId(userId);
//            } else {
//                RoleUser roleUser = Repositories.roleUserRepository.findByUserIdAndRoleId(userId, roleId);
//                Repositories.roleUserRepository.delete(roleUser.getId());
//                Repositories.roleUserRepository.flush();
//                roleUsers = Repositories.roleUserRepository.findByUserId(userId);
//            }
//            return new ResponseBundle().success(roleUsers);
//
//        }).orElseThrow(UnauthenticatedException::new);
//    }

//    /**
//     * <<<<<<< HEAD
//     * 绑定role和user TODO user只能有一个role，绑定之前需要先解绑别的
//     * <p>
//     * =======
//     * 绑定role和user ,返回，用户的role列表
//     * <p>
//     * >>>>>>> 28a0c122e22d267f274628cf24f463e01c8b1338
//     *
//     * @param userId
//     * @param roleId
//     * @return
//     */
//    @ResponseBody
//    @RequestMapping(value = "/binduser", method = {RequestMethod.POST, RequestMethod.GET})
//    public ResultMap binduser(@RequestParam("userId") Integer userId, @RequestParam("roleId") Integer roleId) {
//        ResultMap resultMap;
//        try {
//            resultMap = new ResultMap(ResultCode.SUCCESS);
//            RoleUser roleUser = new RoleUser();
//            roleUser.setRoleId(roleId);
//            roleUser.setUserId(userId);
//            Repositories.roleUserRepository.saveAndFlush(roleUser);
//            List<RoleUser> roleUsers = Repositories.roleUserRepository.findByUserId(userId);
//            resultMap.setData(roleUsers);
//        } catch (Exception e) {
//            resultMap = new ResultMap(ResultCode.SERVER_ERROR);
//        }
//        return resultMap;
//    }


//    /**
//     * 解绑role和user ,返回，用户的role列表
//     *
//     * @param userId
//     * @param roleId
//     * @return
//     */
//    @ResponseBody
//    @RequestMapping(value = "/unbinduser", method = {RequestMethod.POST, RequestMethod.GET})
//    public ResultMap unbinduser(@RequestParam("userId") Integer userId, @RequestParam("roleId") Integer roleId) {
//        ResultMap resultMap;
//        try {
//            resultMap = new ResultMap(ResultCode.SUCCESS);
//            RoleUser roleUser = Repositories.roleUserRepository.findByUserIdAndRoleId(userId, roleId);
//            Repositories.roleUserRepository.delete(roleUser.getId());
//            Repositories.roleUserRepository.flush();
//            List<RoleUser> roleUsers = Repositories.roleUserRepository.findByUserId(userId);
//            resultMap.setData(roleUsers);
//        } catch (Exception e) {
//            resultMap = new ResultMap(ResultCode.SERVER_ERROR);
//        }
//        return resultMap;
//    }

}
