package com.evtape.schedule.web;

import com.alibaba.fastjson.JSONObject;
import com.evtape.schedule.consts.ResponseMeta;
import com.evtape.schedule.domain.*;
import com.evtape.schedule.domain.form.LoginForm;
import com.evtape.schedule.domain.vo.ResponseBundle;
import com.evtape.schedule.domain.vo.UserVo;
import com.evtape.schedule.persistent.Repositories;
import com.evtape.schedule.util.JWTUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author ripper 用戶列表
 */
@Api(value = "用户登录接口")
@RestController
@RequestMapping(value = "/login", produces = "application/json;charset=UTF-8")
public class LoginController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

    @ApiOperation(value = "用户登录", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "用户名", required = true, paramType = "body",
                    dataType = "string"),
            @ApiImplicitParam(name = "password", value = "密码", required = true, paramType = "body",
                    dataType = "string"),
    })
    @PostMapping
    public ResponseBundle login(@RequestBody @Validated LoginForm form, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .sorted(Comparator.reverseOrder())
                    .collect(Collectors.toList());
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID, errors);
        }
        Optional<User> user = Optional.ofNullable(Repositories.userRepository.findByUserName(form.getUserName()));
        return user.map(u -> {
            if (ObjectUtils.notEqual(form.getPassword(), u.getPassword())) {
                return new ResponseBundle().failure(ResponseMeta.ADMIN_PASSWD_NOT_ERROR);
            }

            List<RoleUser> roleUsers = Repositories.roleUserRepository.findByUserId(u.getId());

            List<String> roles = Lists.newArrayListWithCapacity(roleUsers.size());
            Set<String> permissions = Sets.newHashSet();

            roleUsers.forEach(roleUser -> {
                Role role = Repositories.roleRepository.findOne(roleUser.getRoleId());
                roles.add(role.getCode());  // 添加角色编码
                List<RolePermission> rolePermissions = Repositories.rolePermissionRepository.findByRoleId(role.getId());
                rolePermissions.forEach(rolePermission -> {
                    Permission permission = Repositories.permissionRepository.findOne(rolePermission.getPermissionId());
                    permissions.add(permission.getCode());  // 添加权限列表编码
                });
            });

            String token = JWTUtil.sign(u.getPhoneNumber(), u.getPassword());
            UserVo vo = new UserVo();
            vo.setRoles(roles);
            vo.setPermissions(permissions);
            try {
                BeanUtils.copyProperties(vo, u);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            JSONObject response = new JSONObject();
            response.put("id", u.getId());
            response.put("token", token);
            response.put("user", vo);
            return new ResponseBundle().success(response);
        }).orElse(new ResponseBundle().failure(ResponseMeta.ADMIN_ACCOUNT_NOT_EXISTE));
    }

}
