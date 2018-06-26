package com.evtape.schedule.web;

import com.beust.jcommander.internal.Lists;
import com.evtape.schedule.domain.Role;
import com.evtape.schedule.domain.RoleUser;
import com.evtape.schedule.web.auth.Identity;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.evtape.schedule.consts.ResponseMeta;
import com.evtape.schedule.domain.User;
import com.evtape.schedule.domain.vo.ResponseBundle;
import com.evtape.schedule.persistent.Repositories;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Api(value = "用户接口")
@RestController
@RequestMapping("/user")
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @ApiOperation(value = "用户列表", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "employeeCard", value = "员工卡号", paramType = "query", dataType
                    = "string")})
    @ResponseBody
    @GetMapping
    @RequiresAuthentication
    public ResponseBundle userList(@RequestParam(required = false) String employeeCard, @Identity String phoneNumber) {
        Subject currentUser = SecurityUtils.getSubject();
        List<User> users = Lists.newArrayList();
        // 若用户是超级管理员角色则返回除自己以外所有用户
        if (currentUser.hasRole("role:admin")) {
            if (employeeCard == null) {
                users = Repositories.userRepository.findByPhoneNumberNot(phoneNumber);
            } else {
                users = Repositories.userRepository.findByPhoneNumberNotAndEmployeeCardStartingWith(phoneNumber,
                        employeeCard);
            }
        }
        // 若是站区管理员则返回本站区的用户列表
        if (currentUser.hasRole("role:district")) {
            User user = Repositories.userRepository.findByPhoneNumber(phoneNumber);
            if (employeeCard == null) {
                users = Repositories.userRepository.findByPhoneNumberNotAndDistrictId(phoneNumber,
                        user.getDistrictId());
            } else {
                users = Repositories.userRepository.findByPhoneNumberNotAndDistrictIdAndEmployeeCardStartingWith
                        (phoneNumber, user.getDistrictId(), employeeCard);
            }
        }
        return new ResponseBundle().success(users);
    }

    @ApiOperation(value = "新增用户", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "districtId", value = "站区id", required = true, paramType = "body", dataType =
                    "integer"),
            @ApiImplicitParam(name = "districtName", value = "站区名", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "stationId", value = "站点id", required = false, paramType = "body", dataType =
                    "integer"),
            @ApiImplicitParam(name = "stationName", value = "站点名", required = false, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "positionId", value = "岗位id", required = true, paramType = "body", dataType =
                    "integer"),
            @ApiImplicitParam(name = "positionName", value = "岗位名", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "userName", value = "用户名", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "phoneNumber", value = "用户电话号", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "password", value = "密码", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "idCardNumber", value = "身份证号", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "birthday", value = "生日", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "gender", value = "性别", required = true, paramType = "body", dataType = "integer"),
            @ApiImplicitParam(name = "entryDate", value = "入职时间", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "isMarried", value = "未婚已婚 0未婚 1已婚", required = true, paramType = "body",
                    dataType = "integer"),
            @ApiImplicitParam(name = "hasChild", value = "已育未育 0未育 1已育", required = true, paramType = "body",
                    dataType = "integer"),
            @ApiImplicitParam(name = "employeeCard", value = "员工卡号", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "employeeCode", value = "人员编码", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "roleId", value = "角色Id", required = true, paramType = "body", dataType =
                    "integer"),
            @ApiImplicitParam(name = "eduBackGround", value = "学历，高中以下，本科，专科，研究生，博士", required = true, paramType =
                    "body", dataType = "string"),
            @ApiImplicitParam(name = "partyMember", value = "群众，党员，团员，民主党派", required = true, paramType = "body",
                    dataType = "string"),
            @ApiImplicitParam(name = "joinDate", value = "入党入团时间", required = false, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "homeAddress", value = "家庭住址", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "certNo", value = "站务员证书编号", required = false, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "certLevel", value = "站务员证书等级，站务初级", required = false, paramType = "body",
                    dataType = "string"),
            @ApiImplicitParam(name = "xfzNo", value = "消防证书编号", required = false, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "zwyNo", value = "综控员证书编号", required = false, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "zwyLevel", value = "综控员证书级别", required = false, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "backup", value = "是否是补位人员 1是0不是，默认0", required = true, paramType = "body",
                    dataType = "integer"),})
    @ResponseBody
    @PostMapping
    public ResponseBundle addUser(@RequestBody @Validated User form, BindingResult bindingResult, @Identity String
            phoneNumber) {
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .sorted(Comparator.reverseOrder())
                    .collect(Collectors.toList());
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID, errors);
        }

        Optional<User> user = Optional.ofNullable(Repositories.userRepository.findByPhoneNumber(phoneNumber));
        return user.map(u -> {
            User newUser = Repositories.userRepository.saveAndFlush(form);
            RoleUser roleUser = new RoleUser();
            roleUser.setUserId(newUser.getId());
            roleUser.setRoleId(newUser.getRoleId());
            Repositories.roleUserRepository.save(roleUser);

            return new ResponseBundle().success();
        }).orElseThrow(UnauthenticatedException::new);

    }

    @ApiOperation(value = "更新用户", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "主键id", required = true, paramType = "body", dataType = "integer"),
            @ApiImplicitParam(name = "districtId", value = "站区id", required = true, paramType = "body", dataType =
                    "integer"),
            @ApiImplicitParam(name = "districtName", value = "站区名", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "stationId", value = "站点id", required = true, paramType = "body", dataType =
                    "integer"),
            @ApiImplicitParam(name = "stationName", value = "站点名", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "positionId", value = "岗位id", required = true, paramType = "body", dataType =
                    "integer"),
            @ApiImplicitParam(name = "positionName", value = "岗位名", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "userName", value = "用户名", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "phoneNumber", value = "用户电话号", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "password", value = "密码", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "idCardNumber", value = "身份证号", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "birthday", value = "生日", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "gender", value = "性别", required = true, paramType = "body", dataType = "integer"),
            @ApiImplicitParam(name = "entryDate", value = "入职时间", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "isMarried", value = "未婚已婚 0未婚 1已婚", required = true, paramType = "body",
                    dataType = "integer"),
            @ApiImplicitParam(name = "hasChild", value = "已育未育 0未育 1已育", required = true, paramType = "body",
                    dataType = "integer"),
            @ApiImplicitParam(name = "employeeCard", value = "员工卡号", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "eduBackGround", value = "学历，高中以下，本科，专科，研究生，博士", required = true, paramType =
                    "body", dataType = "string"),
            @ApiImplicitParam(name = "partyMember", value = " 群众共产党员，共青团员", required = true, paramType = "body",
                    dataType = "string"),
            @ApiImplicitParam(name = "joinDate", value = "入党入团时间", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "homeAddress", value = "家庭住址", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "certNo", value = "站务员证书编号", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "certLevel", value = "站务员证书等级，站务初级", required = true, paramType = "body",
                    dataType = "string"),
            @ApiImplicitParam(name = "xfzNo", value = "消防证书编号", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "zwyNo", value = "综控员证书编号", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "zwyLevel", value = "综控员证书级别", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "backup", value = "是否是补位人员 1是0不是，默认0", required = true, paramType = "body",
                    dataType = "integer"),})
    @ResponseBody
    @PutMapping
    public ResponseBundle updateuser(@RequestBody User user) {

        try {
            Repositories.userRepository.saveAndFlush(user);
            return new ResponseBundle().success(user);
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    @ApiOperation(value = "删除用户", produces = "application/json")
    @ApiImplicitParam(name = "userId", value = "userId", required = true, paramType = "path", dataType = "integer")
    @ResponseBody
    @DeleteMapping("/{userId}")
    public ResponseBundle deleteuser(@PathVariable("userId") Integer userId) {
        try {
            Repositories.userRepository.delete(userId);
            return new ResponseBundle().success(null);
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    @ApiOperation(value = "备班用户列表", produces = "application/json")
    @ApiImplicitParam(name = "districtId", value = "districtId", required = true, paramType = "path", dataType =
            "integer")
    @ResponseBody
    @GetMapping("/backuplist/{districtId}")
    public ResponseBundle backuplist(@PathVariable("districtId") Integer districtId) {
        try {
            return new ResponseBundle().success(Repositories.userRepository.findByDistrictIdAndBackup(districtId, 1));
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

}
