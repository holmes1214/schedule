package com.evtape.schedule.web;

import com.beust.jcommander.internal.Lists;
import com.evtape.schedule.consts.Constants;
import com.evtape.schedule.consts.ResponseMeta;
import com.evtape.schedule.domain.*;
import com.evtape.schedule.domain.vo.ResponseBundle;
import com.evtape.schedule.persistent.Repositories;
import com.evtape.schedule.util.PoiUtil;
import com.evtape.schedule.web.auth.Identity;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Api(value = "用户接口")
@RestController
@RequestMapping("/user")
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    /**
     * 根据不同权限获取用户列表
     */
    private List<User> getUserList(Subject currentUser, String employeeCard, String phoneNumber) {
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
        return users;
    }

    @ApiOperation(value = "用户列表", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "employeeCard", value = "员工卡号", paramType = "query", dataType
                    = "string")})
    @ResponseBody
    @GetMapping
    @RequiresAuthentication
    public ResponseBundle userList(@RequestParam(required = false) String employeeCard, @Identity String phoneNumber) {
        Subject currentUser = SecurityUtils.getSubject();
        return new ResponseBundle().success(getUserList(currentUser, employeeCard, phoneNumber));
    }

    @ApiOperation(value = "新增用户", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "districtId", value = "站区id", required = true, paramType = "body", dataType =
                    "integer"),
            @ApiImplicitParam(name = "stationId", value = "站点id", paramType = "body", dataType =
                    "integer"),
            @ApiImplicitParam(name = "positionId", value = "岗位id", paramType = "body", dataType =
                    "integer"),
            @ApiImplicitParam(name = "userName", value = "用户名", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "phoneNumber", value = "用户电话号", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "password", value = "密码", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "idCardNumber", value = "身份证号", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "entryDate", value = "入职时间", paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "beginWorkDate", value = "参加工作时间", paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "isMarried", value = "未婚已婚 0未婚 1已婚", paramType = "body",
                    dataType = "String"),
            @ApiImplicitParam(name = "hasChild", value = "已育未育 0未育 1已育", paramType = "body",
                    dataType = "String"),
            @ApiImplicitParam(name = "employeeCard", value = "员工卡号", paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "employeeCode", value = "人员编码", paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "roleId", value = "角色Id", required = true, paramType = "body", dataType =
                    "integer"),
            @ApiImplicitParam(name = "eduBackGround", value = "学历，高中以下，本科，专科，研究生，博士", paramType =
                    "body", dataType = "string"),
            @ApiImplicitParam(name = "partyMember", value = "群众，党员，团员，民主党派", paramType = "body",
                    dataType = "string"),
            @ApiImplicitParam(name = "joinDate", value = "入党入团时间", paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "homeAddress", value = "家庭住址", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "certNo", value = "站务员证书编号", paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "certLevel", value = "站务员证书等级，站务初级", paramType = "body",
                    dataType = "string"),
            @ApiImplicitParam(name = "xfzNo", value = "消防证书编号", paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "zwyNo", value = "综控员证书编号", paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "zwyLevel", value = "综控员证书级别", paramType = "body", dataType =
                    "string"),})
    @ResponseBody
    @PostMapping
    @RequiresRoles(value = {"role:admin", "role:district"}, logical = Logical.OR)
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
            District district = Repositories.districtRepository.findOne(form.getDistrictId());
            if (form.getStationId() != null) {
                Station station = Repositories.stationRepository.findOne(form.getStationId());
                form.setStationName(station.getStationName());
            }
            if (form.getPositionId() != null) {
                Position position = Repositories.positionRepository.findOne(form.getPositionId());
                form.setPositionName(position.getPositionName());
                form.setBackup(position.getBackupPosition());
            } else {
                form.setBackup(0);
            }
            setBirthdayAndGender(form);
            form.setDistrictName(district.getDistrictName());
            Repositories.userRepository.saveAndFlush(form);
            Subject currentUser = SecurityUtils.getSubject();
            return new ResponseBundle().success(getUserList(currentUser, null, phoneNumber));
        }).orElseThrow(UnauthenticatedException::new);

    }

    private void setBirthdayAndGender(User form) {
        try {
            String birthday = form.getIdCardNumber().substring(6, 14);
            DateFormat df = new SimpleDateFormat("yyyyMMdd");
            DateFormat standard = new SimpleDateFormat(Constants.DATE_FORMAT);
            form.setBirthday(standard.format(df.parse(birthday)));
            char c = form.getIdCardNumber().charAt(16);
            if (c == '1') {
                form.setGender("男");
            } else {
                form.setGender("女");
            }
        } catch (Exception e) {
            LOGGER.error("set birthday error: ", e);
        }
    }

    @ApiOperation(value = "更新用户", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "主键id", required = true, paramType = "body", dataType = "integer"),
            @ApiImplicitParam(name = "districtId", value = "站区id", required = true, paramType = "body", dataType =
                    "integer"),
            @ApiImplicitParam(name = "stationId", value = "站点id", paramType = "body", dataType =
                    "integer"),
            @ApiImplicitParam(name = "positionId", value = "岗位id", paramType = "body", dataType =
                    "integer"),
            @ApiImplicitParam(name = "roleId", value = "角色id", required = true, paramType = "body", dataType =
                    "integer"),
            @ApiImplicitParam(name = "userName", value = "用户名", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "phoneNumber", value = "用户电话号", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "password", value = "密码", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "idCardNumber", value = "身份证号", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "entryDate", value = "入职时间", paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "isMarried", value = "未婚已婚 0未婚 1已婚", paramType = "body",
                    dataType = "string"),
            @ApiImplicitParam(name = "hasChild", value = "已育未育 0未育 1已育", paramType = "body",
                    dataType = "string"),
            @ApiImplicitParam(name = "employeeCard", value = "员工卡号", paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "employeeCode", value = "人员编码", paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "eduBackGround", value = "学历，高中以下，本科，专科，研究生，博士", paramType =
                    "body", dataType = "string"),
            @ApiImplicitParam(name = "partyMember", value = " 群众共产党员，共青团员", paramType = "body",
                    dataType = "string"),
            @ApiImplicitParam(name = "joinDate", value = "入党入团时间", paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "beginWorkDate", value = "参加工作时间", paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "homeAddress", value = "家庭住址", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "certNo", value = "站务员证书编号", paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "certLevel", value = "站务员证书等级，站务初级", paramType = "body",
                    dataType = "string"),
            @ApiImplicitParam(name = "xfzNo", value = "消防证书编号", paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "zwyNo", value = "综控员证书编号", paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "zwyLevel", value = "综控员证书级别", paramType = "body", dataType =
                    "string"),})
    @ResponseBody
    @PutMapping
    @RequiresRoles(value = {"role:admin", "role:district"}, logical = Logical.OR)
    public ResponseBundle updateuser(@RequestBody User user, @Identity String phoneNumber) {

        try {
            District district = Repositories.districtRepository.findOne(user.getDistrictId());
            user.setDistrictName(district.getDistrictName());
            if (user.getStationId() != null) {
                Station station = Repositories.stationRepository.findOne(user.getStationId());
                user.setStationName(station.getStationName());
            }
            if (user.getPositionId() != null) {
                Position position = Repositories.positionRepository.findOne(user.getPositionId());
                user.setPositionName(position.getPositionName());
                user.setBackup(position.getBackupPosition());
            }
            setBirthdayAndGender(user);
            Repositories.userRepository.saveAndFlush(user);
            Subject currentUser = SecurityUtils.getSubject();
            return new ResponseBundle().success(getUserList(currentUser, null, phoneNumber));
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    @ApiOperation(value = "删除用户", produces = "application/json")
    @ApiImplicitParam(name = "userId", value = "userId", required = true, paramType = "path", dataType = "integer")
    @ResponseBody
    @DeleteMapping("/{userId}")
    @RequiresRoles(value = {"role:admin", "role:district"}, logical = Logical.OR)
    public ResponseBundle deleteuser(@PathVariable("userId") Integer userId, @Identity String phoneNumber) {
        try {
            Repositories.userRepository.delete(userId);
            Subject currentUser = SecurityUtils.getSubject();
            return new ResponseBundle().success(getUserList(currentUser, null, phoneNumber));
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    @ApiOperation(value = "备班用户列表", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "districtId", value = "districtId", required = true, paramType = "path", dataType =
                    "integer"),
            @ApiImplicitParam(name = "scheduleInfoId", value = "排班id", paramType = "query", dataType =
                    "integer"),
    })
    @ResponseBody
    @GetMapping("/backuplist/{districtId}")
    public ResponseBundle backuplist(@PathVariable("districtId") Integer districtId,
                                     @RequestParam(value = "scheduleInfoId", required = false) Integer scheduleInfoId) {
        try {
            List<User> backupList = Repositories.userRepository.findByDistrictIdAndBackup(districtId, 1);
            if (scheduleInfoId == null) {
                return new ResponseBundle().success(backupList);
            }
            ScheduleInfo info = Repositories.scheduleInfoRepository.findOne(scheduleInfoId);
            List<ScheduleLeave> list = Repositories.scheduleLeaveRepository.findByUserIdsAndDateStr(backupList.stream().map(User::getId).collect(Collectors.toList()), info.getDateStr());
            Set<Integer> userSet = list.stream().map(ScheduleLeave::getUserId).collect(Collectors.toSet());
            return new ResponseBundle().success(backupList.stream().filter(u -> !userSet.contains(u.getId())).collect(Collectors.toList()));
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    @ApiOperation(value = "导入用户列表", produces = "application/json")
    @ResponseBody
    @PostMapping("/import")
    public ResponseBundle backuplist(@Identity String phoneNumber,
                                     @ApiParam(value = "上传的文件", required = true) MultipartFile file) {
        try {
            User admin = Repositories.userRepository.findByPhoneNumber(phoneNumber);
            Integer roleId = admin.getRoleId();
            if (!file.getOriginalFilename().endsWith("xlsx")) {
                return new ResponseBundle().failure(ResponseMeta.BAD_FILE_FORMAT);
            }
            List<Map<String, String>> users = PoiUtil.readExcelContent(file, 0, 0);
            List<District> districts = Repositories.districtRepository.findAll();
            Map<String, District> districtMap = districts.stream().collect(Collectors.toMap(District::getDistrictName, d -> d));
            Map<String, Station> stationMap = Repositories.stationRepository.findAll().stream().collect(Collectors.toMap(Station::getStationName, s -> s));
            Map<String, Position> positionMap = Repositories.positionRepository.findAll().stream().collect(Collectors.toMap(p -> p.getDistrictId() + p.getPositionName(), t -> t));
            List<User> newUsers = new ArrayList<>();
            DateFormat df = new SimpleDateFormat("yyyyMMdd");
            DateFormat standard = new SimpleDateFormat(Constants.DATE_FORMAT);
            Map<String, String> error = new HashMap<>();
            users.forEach(map -> {
                try {
                    String empNo = map.get("员工卡号");
//                    if (empNo == null) {
//                        LOGGER.error("员工卡号为空");
//                        return;
//                    }
                    String code = map.get("人员编码");
//                    if (code == null) {
//                        LOGGER.error("人员编码为空");
//                        return;
//                    }
                    String name = map.get("姓名");
                    if (name == null) {
                        LOGGER.error("姓名为空");
                        error.put("error", "姓名为空");
                        return;
                    }
                    String district = map.get("站区");
                    if (district == null) {
                        LOGGER.error("站区为空{}", name);
                        error.put("error", "站区为空");
                        return;
                    }
                    if (roleId == 2) {
                        if (!district.equals(admin.getDistrictName())) {
                            LOGGER.error("站区长不能导入其它站区人员");
                            error.put("error", "站区长不能导入其它站区人员");
                            return;
                        }
                    }
                    if (!districtMap.containsKey(district)) {
                        error.put("error", "没有这个站区!");
                        return;
                    }
                    String position = map.get("岗位");
//                    if (position == null) {
//                        LOGGER.error("position为空{}", name);
//                        return;
//                    }
                    String phone = map.get("手机号");
                    if (phone == null) {
                        phone = map.get("电话");
                        if (phone == null) {
                            LOGGER.error("phone number为空{}", name);
                            error.put("error","电话号码为空");
                            return;
                        }
                    }
                    String station = map.get("站点");
                    User user = Repositories.userRepository.findByPhoneNumber(phone);
                    if (user == null) {
                        user = new User();
                        user.setPassword("abcd1234");
                        user.setRoleId(3);
                    }
                    user.setEmployeeCard(empNo);
                    user.setUserName(name);
                    user.setEmployeeCode(code);
                    District d = districtMap.get(district);
                    if (d == null) {
                        error.put("error", "站区为空");
                        return;
                    }
                    user.setDistrictId(d.getId());
                    user.setDistrictName(d.getDistrictName());
                    Station s = stationMap.get(station);
                    if (s != null) {
                        user.setStationId(s.getId());
                        user.setStationName(s.getStationName());
                    }

                    if (position != null) {
                        Position p = positionMap.get(d.getId() + position);
                        if (p == null) {
                            LOGGER.error("position为空{}", name);
                            error.put("error", "position为空");
                            return;
                        }
                        user.setPositionId(p.getId());
                        user.setPositionName(p.getPositionName());
                        user.setBackup(p.getBackupPosition());
                        if (p.getPositionName().equals("站区长")) {
                            user.setRoleId(2);
                        }
                    }


                    user.setPhoneNumber(phone);
                    user.setHomeAddress(map.get("住址"));
                    user.setIdCardNumber(map.get("身份证号码"));
                    setBirthdayAndGender(user);
                    user.setIsMarried(map.get("婚否"));
                    user.setHasChild(map.get("子女"));
                    user.setEduBackGround(map.get("学历"));
                    user.setXfzNo(map.get("消防证书编号"));
                    user.setZwyNo(map.get("综控员证书编号"));
                    user.setZwyLevel(map.get("综控员证书级别"));
                    user.setCertNo(map.get("站务员证书编号"));
                    user.setCertLevel(map.get("站务员证书等级"));
                    user.setPartyMember(map.get("政治面貌"));
                    user.setBeginWorkDate(map.get("参加工作时间"));
                    user.setJoinDate(map.get("入党\\团时间"));
                    user.setEntryDate(map.get("入职时间"));
                    newUsers.add(user);
                } catch (Exception e) {
                    LOGGER.error("import error: ", e);
                    return;
                }
            });
            if (error.size() > 0) {
                return new ResponseBundle().failure(ResponseMeta.BUSINESS_ERROR,error.get("error"));
            }else {
                Repositories.userRepository.save(newUsers);
                return new ResponseBundle().success();
            }
        } catch (Exception e) {
            LOGGER.error("error:", e);
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

}
