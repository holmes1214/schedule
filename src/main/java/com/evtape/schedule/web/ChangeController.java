package com.evtape.schedule.web;

import com.evtape.schedule.consts.Constants;
import com.evtape.schedule.consts.ResponseMeta;
import com.evtape.schedule.domain.ScheduleInfo;
import com.evtape.schedule.domain.User;
import com.evtape.schedule.domain.vo.ResponseBundle;
import com.evtape.schedule.persistent.Repositories;
import com.evtape.schedule.web.auth.Identity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Created by zhaoshuai on 2018/8/8.
 */
@Api(value = "换班接口")
@RestController
@RequestMapping(value = "/change", produces = "application/json;charset=UTF-8")
public class ChangeController {

    private static Logger logger = LoggerFactory.getLogger(ChangeController.class);

    @ApiOperation(value = "交换排班")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startDateStr", value = "开始时间", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "employeeCard", value = "姓名编号", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "changeEmployeeCard", value = "交换人的姓名编号", required = true, paramType = "query", dataType = "String"),
    })
    @ResponseBody
    @GetMapping("/scheduleinfo")
    @RequiresAuthentication
    public ResponseBundle changeWork(@Identity String phoneNumber, @RequestParam("startDateStr") String startDateStr,
                                     @RequestParam(value = "employeeCard") String employeeCard,
                                     @RequestParam(value = "changeEmployeeCard") String changeEmployeeCard) throws Exception {

//        User u = Repositories.userRepository.findByPhoneNumber(phoneNumber);
//        //1 管理员权限 2 站区长权限
//        if (u.getRoleId() == 1 || u.getRoleId() == 2) {
//
//        }
//        //4 站长权限
//        if (u.getRoleId() == 4){
//
//            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
//            Date dt1 = df.parse(startDateStr);
//            Date dt2 = new Date();
//            if (dt1.getTime() < dt2.getTime()){
//                return new ResponseBundle().success("没有权限更换班制");
//            }
//        }
//        //3 普通权限
//        if (u.getRoleId() == 3){
//            return new ResponseBundle().success("没有权限更换假期");
//        }

        DateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT);
        try {
            List<ScheduleInfo> userList = null;
            List<User> users = Repositories.userRepository.findByUserNameOrEmployeeCard(employeeCard, employeeCard);
            if (users.size() > 0) {
                userList = Repositories.scheduleInfoRepository.findByUserIdsNoEndDate(df.parse(startDateStr), users.stream().map(User::getId).collect(Collectors.toList()));
            }

            List<ScheduleInfo> changeList = null;
            List<User> changes = Repositories.userRepository.findByUserNameOrEmployeeCard(changeEmployeeCard, changeEmployeeCard);
            if (changes.size() > 0) {
                changeList = Repositories.scheduleInfoRepository.findByUserIdsNoEndDate(df.parse(startDateStr), changes.stream().map(User::getId).collect(Collectors.toList()));
            }
            //求循环次数
            int size = userList.size() > changeList.size() ? (userList.size() - changeList.size()) : (changeList.size() - userList.size());
            if (userList.size() > changeList.size()) {
                ScheduleInfo scheduleInfo = new ScheduleInfo();
                scheduleInfo.setUserId(users.get(0).getId());
                scheduleInfo.setUserName(users.get(0).getUserName());
                scheduleInfo.setVersion(0);
                for (int i = 0; i < size; i++) {
                    changeList.add(scheduleInfo);
                }
            } else {
                ScheduleInfo scheduleInfo = new ScheduleInfo();
                scheduleInfo.setUserId(changes.get(0).getId());
                scheduleInfo.setUserName(changes.get(0).getUserName());
                scheduleInfo.setVersion(0);
                for (int i = 0; i < size; i++) {
                    userList.add(scheduleInfo);
                }
            }

            if (userList.size() == changeList.size()) {
                for (int i = 0; i < userList.size(); i++) {
                    ScheduleInfo change = new ScheduleInfo();
                    BeanUtils.copyProperties(changeList.get(i), change);
                    ScheduleInfo user = new ScheduleInfo();
                    BeanUtils.copyProperties(userList.get(i), user);
                    user.setDistrictId(changeList.get(i).getDistrictId());
                    user.setDutyName(changeList.get(i).getDutyName());
                    user.setDutyCode(changeList.get(i).getDutyCode());
                    user.setDutyClassId(changeList.get(i).getDutyClassId());
                    user.setPositionId(changeList.get(i).getPositionId());
                    user.setPositionName(changeList.get(i).getPositionName());
                    user.setStationId(changeList.get(i).getStationId());
                    user.setSuiteId(changeList.get(i).getSuiteId());
                    user.setWorkflowId(changeList.get(i).getWorkflowId());
                    user.setWorkingHours(changeList.get(i).getWorkingHours());
                    user.setWorkflowCode(changeList.get(i).getWorkflowCode());
                    user.setDateStr(changeList.get(i).getDateStr());
                    user.setScheduleDate(changeList.get(i).getScheduleDate());
                    user.setScheduleWeek(changeList.get(i).getScheduleWeek());
                    if (user.getVersion() == null) {
                        user.setVersion(0);
                    } else {
                        user.setVersion(user.getVersion() + 1);
                    }


                    change.setDistrictId(userList.get(i).getDistrictId());
                    change.setDutyName(userList.get(i).getDutyName());
                    change.setDutyCode(userList.get(i).getDutyCode());
                    change.setDutyClassId(userList.get(i).getDutyClassId());
                    change.setPositionId(userList.get(i).getPositionId());
                    change.setPositionName(userList.get(i).getPositionName());
                    change.setStationId(userList.get(i).getStationId());
                    change.setSuiteId(userList.get(i).getSuiteId());
                    change.setWorkflowId(userList.get(i).getWorkflowId());
                    change.setWorkingHours(userList.get(i).getWorkingHours());
                    change.setWorkflowCode(userList.get(i).getWorkflowCode());
                    change.setDateStr(userList.get(i).getDateStr());
                    change.setScheduleDate(userList.get(i).getScheduleDate());
                    change.setScheduleWeek(userList.get(i).getScheduleWeek());
                    if (change.getVersion() == null) {
                        change.setVersion(0);
                    } else {
                        change.setVersion(change.getVersion() + 1);
                    }

                    if (StringUtils.isNotBlank(user.getDateStr())) {
                        Repositories.scheduleInfoRepository.save(user);
                    }
                    if (StringUtils.isNotBlank(change.getDateStr())) {
                        Repositories.scheduleInfoRepository.save(change);
                    }
                }
            }

            return new ResponseBundle().success();
        } catch (Exception e) {
            logger.error("error:", e);
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

}
