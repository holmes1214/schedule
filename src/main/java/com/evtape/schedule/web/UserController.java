package com.evtape.schedule.web;

import com.beust.jcommander.internal.Lists;
import com.evtape.schedule.consts.Constants;
import com.evtape.schedule.consts.ResponseMeta;
import com.evtape.schedule.domain.District;
import com.evtape.schedule.domain.Position;
import com.evtape.schedule.domain.Station;
import com.evtape.schedule.domain.User;
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
            @ApiImplicitParam(name = "stationId", value = "站点id", required = false, paramType = "body", dataType =
                    "integer"),
            @ApiImplicitParam(name = "positionId", value = "岗位id", required = true, paramType = "body", dataType =
                    "integer"),
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
            @ApiImplicitParam(name = "gender", value = "性别", required = true, paramType = "body", dataType = "String"),
            @ApiImplicitParam(name = "entryDate", value = "入职时间", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "beginWorkDate", value = "参加工作时间", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "isMarried", value = "未婚已婚 0未婚 1已婚", required = true, paramType = "body",
                    dataType = "String"),
            @ApiImplicitParam(name = "hasChild", value = "已育未育 0未育 1已育", required = true, paramType = "body",
                    dataType = "String"),
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
            Station station = Repositories.stationRepository.findOne(form.getStationId());
            Position position = Repositories.positionRepository.findOne(form.getPositionId());
            form.setDistrictName(district.getDistrictName());
            form.setStationName(station.getStationName());
            form.setPositionName(position.getPositionName());

            Repositories.userRepository.saveAndFlush(form);
            Subject currentUser = SecurityUtils.getSubject();
            return new ResponseBundle().success(getUserList(currentUser, null, phoneNumber));
        }).orElseThrow(UnauthenticatedException::new);

    }

    @ApiOperation(value = "更新用户", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "主键id", required = true, paramType = "body", dataType = "integer"),
            @ApiImplicitParam(name = "districtId", value = "站区id", required = true, paramType = "body", dataType =
                    "integer"),
            @ApiImplicitParam(name = "stationId", value = "站点id", required = true, paramType = "body", dataType =
                    "integer"),
            @ApiImplicitParam(name = "positionId", value = "岗位id", required = true, paramType = "body", dataType =
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
            @ApiImplicitParam(name = "birthday", value = "生日", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "gender", value = "性别", required = true, paramType = "body", dataType = "string"),
            @ApiImplicitParam(name = "entryDate", value = "入职时间", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "isMarried", value = "未婚已婚 0未婚 1已婚", required = true, paramType = "body",
                    dataType = "string"),
            @ApiImplicitParam(name = "hasChild", value = "已育未育 0未育 1已育", required = true, paramType = "body",
                    dataType = "string"),
            @ApiImplicitParam(name = "employeeCard", value = "员工卡号", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "eduBackGround", value = "学历，高中以下，本科，专科，研究生，博士", required = true, paramType =
                    "body", dataType = "string"),
            @ApiImplicitParam(name = "partyMember", value = " 群众共产党员，共青团员", required = true, paramType = "body",
                    dataType = "string"),
            @ApiImplicitParam(name = "joinDate", value = "入党入团时间", required = true, paramType = "body", dataType =
                    "string"),
            @ApiImplicitParam(name = "beginWorkDate", value = "参加工作时间", required = true, paramType = "body", dataType =
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
    @RequiresRoles(value = {"role:admin", "role:district"}, logical = Logical.OR)
    public ResponseBundle updateuser(@RequestBody User user, @Identity String phoneNumber) {

        try {
            District district = Repositories.districtRepository.findOne(user.getDistrictId());
            user.setDistrictName(district.getDistrictName());
            Station station = Repositories.stationRepository.findOne(user.getStationId());
            user.setStationName(station.getStationName());
            Position position = Repositories.positionRepository.findOne(user.getPositionId());
            user.setPositionName(position.getPositionName());
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

    @ApiOperation(value = "导入用户列表", produces = "application/json")
    @ResponseBody
    @PostMapping("/import")
    public ResponseBundle backuplist(@ApiParam(value = "上传的文件", required = true) MultipartFile file) {
        try {
            List<Map<String, String>> users = PoiUtil.readExcelContent(file, 0, 1);
            List<District> districts = Repositories.districtRepository.findAll();
            Map<String, District> districtMap = districts.stream().collect(Collectors.toMap(District::getDistrictName, d -> d));
            Map<String, Station> stationMap = Repositories.stationRepository.findAll().stream().collect(Collectors.toMap(Station::getStationName, s -> s));
            Map<String, Position> positionMap = Repositories.positionRepository.findAll().stream().collect(Collectors.toMap(p -> p.getDistrictId() + p.getPositionName(), t -> t));
            List<User> newUsers = new ArrayList<>();
            DateFormat df = new SimpleDateFormat("yyyyMMdd");
            DateFormat standard = new SimpleDateFormat(Constants.DATE_FORMAT);
            users.forEach(map -> {
                try {
                    String empNo = map.get("员工卡号");
                    if (empNo == null) {
                        return;
                    }
                    String code = map.get("人员编码");
                    if (code == null) {
                        return;
                    }
                    String name = map.get("姓名");
                    if (name == null) {
                        return;
                    }
                    String district = map.get("站区");
                    if (district == null) {
                        return;
                    }
                    if (!districtMap.containsKey(district)) {
                        return;
                    }
                    String position = map.get("岗位");
                    if (position == null) {
                        return;
                    }
                    String phone = map.get("手机号");
                    if (phone == null) {
                        phone = map.get("电话");
                        if (phone == null) {
                            return;
                        }
                    }
                    String station = map.get("站点");
                    User user = Repositories.userRepository.findByEmployeeCode(code);
                    if (user==null){
                        user=new User();
                        user.setPassword("abcd1234");
                    }
                    user.setEmployeeCard(empNo);
                    user.setUserName(name);
                    user.setEmployeeCode(code);
                    District d = districtMap.get(district);
                    if (d == null) {
                        return;
                    }
                    user.setDistrictId(d.getId());
                    user.setDistrictName(d.getDistrictName());
                    Station s = stationMap.get(station);
                    if (s != null) {
                        user.setStationId(s.getId());
                        user.setStationName(s.getStationName());
                    }
                    Position p = positionMap.get(d.getId() + position);
                    if (p == null) {
                        return;
                    }
                    user.setPositionId(p.getId());
                    user.setPositionName(p.getPositionName());
                    user.setPhoneNumber(phone);
                    user.setGender(map.get("性别"));
                    user.setBirthday(map.get("出生日期"));
                    user.setHomeAddress(map.get("住址"));
                    user.setIdCardNumber(map.get("身份证号码"));
                    if (StringUtils.isBlank(user.getBirthday()) && StringUtils.isNotBlank(user.getIdCardNumber())) {
                        String birthday = user.getIdCardNumber().substring(6, 14);
                        user.setBirthday(standard.format(df.parse(birthday)));
                    }
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
                    return;
                }
            });
            Repositories.userRepository.save(newUsers);
            return new ResponseBundle().success();
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

}
