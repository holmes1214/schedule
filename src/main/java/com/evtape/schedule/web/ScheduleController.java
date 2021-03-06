package com.evtape.schedule.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import io.swagger.models.auth.In;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.evtape.schedule.domain.DutyClass;
import com.evtape.schedule.domain.DutySuite;
import com.evtape.schedule.domain.ScheduleInfo;
import com.evtape.schedule.domain.ScheduleTemplate;
import com.evtape.schedule.domain.ScheduleUser;
import com.evtape.schedule.domain.ScheduleWorkflow;
import com.evtape.schedule.domain.ScheduleWorkflowContent;
import com.evtape.schedule.domain.User;
import com.evtape.schedule.domain.form.ScheduleForm;
import com.evtape.schedule.domain.form.ScheduleUserForm;
import com.evtape.schedule.domain.vo.ResponseBundle;
import com.evtape.schedule.exception.BaseException;
import com.evtape.schedule.persistent.Repositories;
import com.evtape.schedule.serivce.ScheduleTemplateService;
import com.evtape.schedule.serivce.WorkflowService;
import com.evtape.schedule.util.PictureUtil;
import com.evtape.schedule.web.auth.Identity;
import sun.security.krb5.SCDynamicStoreConfig;

/**
 * 排班
 */
@Api(value = "排班接口")
@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    private static Logger logger = LoggerFactory.getLogger(ScheduleController.class);
    @Autowired
    private ScheduleTemplateService scheduleTemplateService;
    @Autowired
    private WorkflowService workflowService;

    @ApiOperation(value = "删除旧排班模板，创建新模板", produces = "application/json")
    @ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "path", dataType = "integer")
    @ResponseBody
    @PostMapping("/createtemplate/{suiteId}")
    public ResponseBundle createTemplate(@PathVariable("suiteId") Integer suiteId) {
        try {
            List<ScheduleTemplate> templates = scheduleTemplateService.removeAndSaveTemplates(suiteId);
            return returnTemplate(suiteId, templates);
        } catch (BaseException e) {
            logger.error("error:", e);
            return new ResponseBundle().failure(e.getErrorCode());
        } catch (Exception e) {
            logger.error("error:", e);
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    @ApiOperation(value = "加载排班模板", produces = "application/json")
    @ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "path", dataType = "integer")
    @ResponseBody
    @GetMapping("/templatelist/{suiteId}")
    public ResponseBundle templateList(@PathVariable("suiteId") Integer suiteId) {
        try {
            List<ScheduleTemplate> templates = Repositories.scheduleTemplateRepository
                    .findBySuiteIdOrderByOrderIndex(suiteId);
            return returnTemplate(suiteId, templates);
        } catch (Exception e) {
            logger.error("error:", e);
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    @ApiOperation(value = "删除单个排班人员", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "path", dataType = "integer"),
            @ApiImplicitParam(name = "userId",value = "用户id",required = true,paramType = "path")})
    @ResponseBody
    @DeleteMapping("/delete/{suiteId}/{userId}")
    public ResponseBundle deleteOne(@PathVariable("suiteId") Integer suiteId,
                                    @PathVariable("userId") Integer userId){
        List<ScheduleUser> users = Repositories.scheduleUserRepository.findBySuiteIdOrderByWeekNum(suiteId);
        List<ScheduleUser> delUser = users.stream().filter(u -> u.getUserId().equals(userId)).collect(Collectors.toList());
        Repositories.scheduleUserRepository.delete(delUser);
        return new ResponseBundle().success(ResponseMeta.SUCCESS);
    }

    @ApiOperation(value = "排班模板交换任务", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "query", dataType = "integer"),
            @ApiImplicitParam(name = "weekNum1", value = "任务1的周数", required = true, paramType = "query", dataType = "integer"),
            @ApiImplicitParam(name = "dayNum1", value = "任务1的天数", required = true, paramType = "query", dataType = "integer"),
            @ApiImplicitParam(name = "weekNum2", value = "任务2的周数", required = true, paramType = "query", dataType = "integer"),
            @ApiImplicitParam(name = "dayNum2", value = "任务2的天数", required = true, paramType = "query", dataType = "integer"),})
    @ResponseBody
    @PutMapping("/exchangeTemplate")
    public ResponseBundle exchangeTemplate(@RequestBody ScheduleForm form) {
        try {
            scheduleTemplateService.exchangeTemplate(form.getSuiteId(), form.getWeekNum1(), form.getDayNum1(), form.getWeekNum2(), form.getDayNum2());
            return new ResponseBundle().success();
        } catch (Exception e) {
            logger.error("error:", e);
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    private ResponseBundle returnTemplate(Integer suiteId, List<ScheduleTemplate> templates) {
        Map<String, Object> result = new HashMap<>();
        DutySuite dutySuite = Repositories.dutySuiteRepository.findOne(suiteId);
        List<User> userList;
        List<ScheduleUser> users = Repositories.scheduleUserRepository.findBySuiteIdOrderByWeekNum(suiteId);
        if (dutySuite.getBackup() == 1) {
            userList = Repositories.userRepository.findByDistrictIdAndBackup(dutySuite.getDistrictId(), 1);
        } else {
            userList = Repositories.userRepository.findByDistrictIdAndStationIdAndPositionId(dutySuite.getDistrictId(), dutySuite.getStationId(),
                    dutySuite.getPositionId());
        }
        result.put("templatelist", templates);
        result.put("userlist", userList);
        List<DutyClass> list = Repositories.dutyClassRepository.findBySuiteId(suiteId);
        list.forEach(l -> {
            if (l.getRelevantClassId() != null) {
                l.setRelevant(Repositories.dutyClassRepository.findOne(l.getRelevantClassId()));
            }
        });

        result.put("dutyclass", list);
        result.put("weeks", templates.isEmpty() ? 0 : templates.stream().mapToInt(i -> i.getWeekNum()).max().getAsInt());
        result.put("scheduleUsers", users);
        return new ResponseBundle().success(result);
    }

//    @ApiOperation(value = "排班模板删除一周", produces = "application/json")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "query", dataType = "integer"),
//            @ApiImplicitParam(name = "weekNum", value = "被删除的周数", required = true, paramType = "query", dataType = "integer"),})
//    @ResponseBody
//    @DeleteMapping("/deleteoneweek")
//    public ResponseBundle deleteOneWeek(@RequestParam("suiteId") Integer suiteId, @RequestParam("weekNum") Integer weekNum) {
//        try {
//            ScheduleUser user = Repositories.scheduleUserRepository.findBySuiteIdAndWeekNum(suiteId, weekNum);
//            if (user != null) {
//                Repositories.scheduleUserRepository.delete(user);
//                Repositories.scheduleUserRepository.flush();
//            }
//            List<ScheduleTemplate> todoList = Repositories.scheduleTemplateRepository
//                    .findBySuiteIdOrderByOrderIndex(suiteId);
//            List<ScheduleTemplate> updateList = new ArrayList<>();
//            List<ScheduleTemplate> deleteList = new ArrayList<>();
//            for (ScheduleTemplate scheduleTemplate : todoList) {
//                if (weekNum > scheduleTemplate.getWeekNum()) {
//                    continue;
//                } else if (weekNum == scheduleTemplate.getWeekNum()) {
//                    deleteList.add(scheduleTemplate);
//                } else {
//                    scheduleTemplate.setWeekNum(scheduleTemplate.getWeekNum() - 1);
//                    scheduleTemplate.setOrderIndex(scheduleTemplate.getWeekNum() * 7 + scheduleTemplate.getDayNum());
//                    updateList.add(scheduleTemplate);
//                }
//            }
//            Repositories.scheduleTemplateRepository.delete(deleteList);
//            Repositories.scheduleTemplateRepository.flush();
//            Repositories.scheduleTemplateRepository.save(updateList);
//            return new ResponseBundle().success();
//        } catch (Exception e) {
//            logger.error("error:", e);
//            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
//        }
//    }

    /**
     * setscheduleuser方法接收参数类
     *
     * @author jsychen
     */
    @ApiOperation(value = "排班模板设置人员", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "query", dataType = "integer"),
            @ApiImplicitParam(name = "weekNum", value = "被设置的周数", required = true, paramType = "query", dataType = "integer"),
            @ApiImplicitParam(name = "userIds", value = "被设置人的id", required = true, paramType = "query", dataType = "String"),})
    @ResponseBody
    @PutMapping("/setscheduleuser")
    public ResponseBundle setScheduleUser(@RequestBody ScheduleUserForm form) {
        try {

            DutySuite dutySuite = Repositories.dutySuiteRepository.findOne(form.getSuiteId());
            List<User> user = new ArrayList<>();
            if (StringUtils.isNotBlank(form.getUserIds())) {
                String[] ids = form.getUserIds().split(",");
                List<Integer> userList = new ArrayList<>();
                for (int i = 0; i < ids.length; i++) {
                    userList.add(Integer.parseInt(ids[i]));
                }
                user = Repositories.userRepository.findByIds(userList);
            }
            for (int i = 0; i < user.size(); i++) {
                ScheduleUser user2 = Repositories.scheduleUserRepository.findBySuiteIdAndUserId(form.getSuiteId(), user.get(i).getId());
                if (user2 != null) {
                    Repositories.scheduleUserRepository.delete(user2.getId());
                    Repositories.scheduleUserRepository.flush();
                }
            }

//            //先查待设置的周有没有user
//            List<ScheduleUser> user1 = Repositories.scheduleUserRepository.findBySuiteIdAndWeekNum(form.getSuiteId(),
//                    form.getWeekNum());

            List<ScheduleUser> saveUser = new ArrayList<>();
            for (int i = 0; i < user.size(); i++) {
                ScheduleUser user3 = new ScheduleUser();
                user3.setDistrictId(dutySuite.getDistrictId());
                user3.setPositionId(dutySuite.getPositionId());
                user3.setStationId(dutySuite.getStationId());
                user3.setSuiteId(form.getSuiteId());
                user3.setWeekNum(form.getWeekNum());
                user3.setUserId(user.get(i).getId());
                user3.setUserName(user.get(i).getUserName());
                saveUser.add(user3);
            }
            Repositories.scheduleUserRepository.save(saveUser);

            return new ResponseBundle().success();
        } catch (Exception e) {
            logger.error("error:", e);
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    /**
     * 排班模板取消人员设置
     */
    @ApiOperation(value = "排班模板取消人员设置", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "query", dataType = "integer"),
            @ApiImplicitParam(name = "weekNum", value = "被取消设置的周数", required = true, paramType = "query", dataType = "integer"),
    })
    @ResponseBody
    @PutMapping(value = "/removescheduleuser")
    public ResponseBundle removescheduleuser(@RequestBody ScheduleUserForm form) {
        try {
            List<ScheduleUser> user = Repositories.scheduleUserRepository.findBySuiteIdAndWeekNum(form.getSuiteId(), form.getWeekNum());
            Repositories.scheduleUserRepository.delete(user);
            return new ResponseBundle().success();
        } catch (Exception e) {
            logger.error("error:", e);
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    /**
     * 查询排班计划
     */

    @ApiOperation(value = "查询排班计划", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startDateStr", value = "开始时间", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "endDateStr", value = "结束时间", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "stationId", value = "站点id", required = false, paramType = "query", dataType = "Integer"),
            @ApiImplicitParam(name = "positionId", value = "岗位id", required = false, paramType = "query", dataType = "Integer"),
            @ApiImplicitParam(name = "userName", value = "姓名编号", required = false, paramType = "query", dataType = "String"),
    })
    @ResponseBody
    @GetMapping("/scheduleinfo")
    @RequiresAuthentication
    public ResponseBundle getscheduleinfo(@Identity String phoneNumber, @RequestParam("startDateStr") String startDateStr,
                                          @RequestParam("endDateStr") String endDateStr, @RequestParam(value = "stationId", required = false) Integer stationId,
                                          @RequestParam(value = "positionId", required = false) Integer positionId, @RequestParam(value = "userName", required = false) String userName) {
        User user = Repositories.userRepository.findByPhoneNumber(phoneNumber);
        try {
            List<ScheduleInfo> list = scheduleTemplateService.searchScheduleInfo(startDateStr, endDateStr, user.getDistrictId(), user.getStationId(), positionId, userName);
            //通过version对返回数据进行过滤
            List<ScheduleInfo> ver = list.stream().filter(scheduleInfo -> scheduleInfo.getVersion() != null && scheduleInfo.getVersion() > 0).collect(Collectors.toList());
            List<ScheduleInfo> resList = list.stream().filter(scheduleInfo -> !ver.contains(scheduleInfo)).collect(Collectors.toList());
            HashMap<String, ScheduleInfo> hashMap = new HashMap<>();
            for (ScheduleInfo s : ver) {
                hashMap.put(s.getDateStr() + s.getUserName(), s);
            }
            hashMap.forEach((k, v) -> resList.add(v));

            List<User> userList = Repositories.userRepository.findByDistrictId(user.getDistrictId());
            Map<Integer, User> userMap = userList.stream().collect(Collectors.toMap(User::getId, u -> u));
            Set<User> result = resList.stream().map(u -> userMap.get(u.getUserId())).collect(Collectors.toSet());
            list.forEach(i -> {
                User u = userMap.get(i.getUserId());
                if (u.getScheduleInfoList() == null) {
                    u.setScheduleInfoList(new ArrayList<>());
                }
                u.getScheduleInfoList().add(i);
            });

            List<User> res = null;
            if (stationId != null) {
                res = result.stream().filter(i -> stationId.equals(i.getStationId())).collect(Collectors.toList());
            }

            if (positionId != null) {
                res = result.stream().filter(user1 -> user1.getPositionId().equals(positionId)).collect(Collectors.toList());
            }
            if (stationId == null && positionId == null) {
                res = result.stream().collect(Collectors.toList());
            }
            res.forEach(u -> {
                u.getScheduleInfoList().stream().sorted(Comparator.comparing(ScheduleInfo::getDateStr));
            });
            return new ResponseBundle().success(res.stream().sorted(Comparator.comparing(User::getId)).collect(Collectors.toList()));
        } catch (Exception e) {
            logger.error("error:", e);
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    @ApiOperation(value = "导出个人排班计划", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startDateStr", value = "开始时间", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "endDateStr", value = "结束时间", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "stationId", value = "站点id", required = false, paramType = "query", dataType = "Integer"),
            @ApiImplicitParam(name = "positionId", value = "岗位id", required = false, paramType = "query", dataType = "Integer"),
            @ApiImplicitParam(name = "userName", value = "姓名编号", required = false, paramType = "query", dataType = "String"),
    })
    @GetMapping("/scheduleinfo/export/img")
    @RequiresAuthentication
    public void exportImage(@Identity String phoneNumber, @RequestParam("startDateStr") String startDateStr,
                            @RequestParam("endDateStr") String endDateStr, @RequestParam(value = "stationId", required = false) Integer stationId,
                            @RequestParam(value = "positionId", required = false) Integer positionId,
                            @RequestParam(value = "userName", required = false) String userName, HttpServletResponse response) {
        User user = Repositories.userRepository.findByPhoneNumber(phoneNumber);
        try {
            List<ScheduleInfo> list = scheduleTemplateService.searchScheduleInfo(startDateStr, endDateStr, user.getDistrictId(), user.getStationId(), positionId, userName);
            List<User> userList = Repositories.userRepository.findByDistrictId(user.getDistrictId());
            Map<Integer, User> userMap = userList.stream().collect(Collectors.toMap(User::getId, u -> u));
            Map<User, List<ScheduleInfo>> scheduleMap = new HashMap<>();
            list.forEach(i -> {
                User u = userMap.get(i.getUserId());
                if (u.getScheduleInfoList() == null) {
                    u.setScheduleInfoList(new ArrayList<>());
                    scheduleMap.put(u, u.getScheduleInfoList());
                }
                u.getScheduleInfoList().add(i);
            });
            scheduleMap.values().forEach(l -> {
                l.stream().sorted(Comparator.comparing(ScheduleInfo::getDateStr));
            });
            List<DutyClass> classList = Repositories.dutyClassRepository.findByDistrictId(user.getDistrictId());
            Map<Integer, DutyClass> shiftMap = classList.stream().collect(Collectors.toMap(DutyClass::getId, d -> d));
            List<ScheduleWorkflow> workflowList = Repositories.workflowRepository.findByDistrictId(user.getDistrictId());
            Map<Integer, List<ScheduleWorkflow>> workflowMap = workflowList.stream().collect(Collectors.groupingBy(ScheduleWorkflow::getClassId));
            List<ScheduleWorkflowContent> contentList = Repositories.contentRepository.findByDistrictId(user.getDistrictId());
            Map<Integer, List<ScheduleWorkflowContent>> contentMap = contentList.stream().collect(Collectors.groupingBy(ScheduleWorkflowContent::getWorkFlowId));
            response.setHeader("Content-Disposition", "attachment; filename=\"schedule.zip\"");
            response.setContentType("application/octet-stream;charset=UTF-8");
            PictureUtil.createUserSchedulePicture(scheduleMap, shiftMap, workflowMap, contentMap, response.getOutputStream());
        } catch (Exception e) {
            logger.error("error:", e);
        }
    }

    /**
     * 生成排班计划
     */
    @ApiOperation(value = "生成排班计划", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "query", dataType = "integer"),
            @ApiImplicitParam(name = "dateStr", value = "dateStr", required = true, paramType = "query", dataType = "integer"),})
    @ResponseBody
    @PostMapping("/scheduleinfo")
    public ResponseBundle createscheduleinfo(@RequestParam("suiteId") Integer suiteId,
                                             @RequestParam("dateStr") String dateStr) {
        try {
            List<ScheduleInfo> scheduleInfos = scheduleTemplateService.createScheduleInfoData(suiteId, dateStr);
            return new ResponseBundle().success(scheduleInfos);
        } catch (Exception e) {
            logger.error("error:", e);
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    /**
     * 手动排班设置班次
     */
    @ApiOperation(value = "手动排班设置班次", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "body", dataType = "integer"),
            @ApiImplicitParam(name = "classId", value = "班次id", required = true, paramType = "body", dataType = "integer"),
            @ApiImplicitParam(name = "weekNum", value = "被设置的周数", required = true, paramType = "body", dataType = "integer"),
            @ApiImplicitParam(name = "dayNum", value = "被设置的周天数", required = true, paramType = "body", dataType = "integer"),
            @ApiImplicitParam(name = "relevance", value = "关联下夜班（0：没有关联），1：一周内关联下夜，2：两周（隔行）关联", required = true, paramType = "body", dataType = "integer"),
            @ApiImplicitParam(name = "reClassId", value = "关联的id", required = false, paramType = "body", dataType = "integer")})
    @ResponseBody
    @PutMapping("/settemplateclass")
    public ResponseBundle settemplateclass(@RequestBody ScheduleUserForm form) {
        try {
            if (form.getRelevance() == 1) {
                if (form.getReClassId() == null) {
                    return new ResponseBundle().success("没有要关联的班次!");
                }
                ScheduleUserForm form1 = new ScheduleUserForm();
                BeanUtils.copyProperties(form, form1);
                form1.setClassId(form.getReClassId());
                form1.setDayNum(form1.getDayNum() + 1);
                settemplate(form1);
            }
            if (form.getRelevance() == 2) {
                if (form.getReClassId() == null) {
                    return new ResponseBundle().success("没有要关联的班次!");
                }
                ScheduleUserForm form1 = new ScheduleUserForm();
                BeanUtils.copyProperties(form, form1);
                form1.setClassId(form.getReClassId());
                form1.setWeekNum(form1.getWeekNum() + 1);
                form1.setDayNum(0);
                settemplate(form1);
            }
            ScheduleTemplate scheduleTemplate = Repositories.scheduleTemplateRepository.findBySuiteIdAndWeekNumAndDayNum(form.getSuiteId(), form.getWeekNum(), form.getDayNum());
            if (scheduleTemplate == null) {
                scheduleTemplate = new ScheduleTemplate();
                DutySuite dutySuite = Repositories.dutySuiteRepository.findOne(form.getSuiteId());
                scheduleTemplate.setDistrictId(dutySuite.getDistrictId());
                scheduleTemplate.setSuiteId(form.getSuiteId());
                scheduleTemplate.setWeekNum(form.getWeekNum());
                scheduleTemplate.setDayNum(form.getDayNum());
                scheduleTemplate.setOrderIndex(form.getWeekNum() * 7 + form.getDayNum());
            }
            DutyClass dutyClass = Repositories.dutyClassRepository.findOne(form.getClassId());
            scheduleTemplate.setCellColor(dutyClass.getClassColor());
            scheduleTemplate.setWorkingLength(dutyClass.getWorkingLength());
            scheduleTemplate.setDutyName(dutyClass.getDutyName());
            scheduleTemplate.setDutyCode(dutyClass.getDutyCode());
            scheduleTemplate.setClassId(form.getClassId());
            Repositories.scheduleTemplateRepository.saveAndFlush(scheduleTemplate);
            return new ResponseBundle().success(scheduleTemplate);
        } catch (Exception e) {
            logger.error("error:", e);
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    @ApiOperation(value = "手动排班删除班次", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "templateId", value = "模板id", required = true, paramType = "path", dataType = "integer"),})
    @ResponseBody
    @DeleteMapping("/template/{templateId}")
    public ResponseBundle settemplateclass(@PathVariable Integer templateId) {
        try {
            Repositories.scheduleTemplateRepository.delete(templateId);
            return new ResponseBundle().success();
        } catch (Exception e) {
            logger.error("error:", e);
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    /**
     * @Description: 插入排班模板
     * @Param: ScheduleUserForm
     * @return:
     * @Author: sickle
     * @Date: 2018-8-20
     */
    public void settemplate(ScheduleUserForm form) {
        ScheduleTemplate scheduleTemplate = Repositories.scheduleTemplateRepository.findBySuiteIdAndWeekNumAndDayNum(form.getSuiteId(), form.getWeekNum(), form.getDayNum());
        if (scheduleTemplate == null) {
            scheduleTemplate = new ScheduleTemplate();
            DutySuite dutySuite = Repositories.dutySuiteRepository.findOne(form.getSuiteId());
            scheduleTemplate.setDistrictId(dutySuite.getDistrictId());
            scheduleTemplate.setSuiteId(form.getSuiteId());
            scheduleTemplate.setWeekNum(form.getWeekNum());
            scheduleTemplate.setDayNum(form.getDayNum());
            scheduleTemplate.setOrderIndex(form.getWeekNum() * 7 + form.getDayNum());
        }
        DutyClass dutyClass = Repositories.dutyClassRepository.findOne(form.getClassId());
        scheduleTemplate.setCellColor(dutyClass.getClassColor());
        scheduleTemplate.setWorkingLength(dutyClass.getWorkingLength());
        scheduleTemplate.setDutyName(dutyClass.getDutyName());
        scheduleTemplate.setDutyCode(dutyClass.getDutyCode());
        scheduleTemplate.setClassId(form.getClassId());
        Repositories.scheduleTemplateRepository.saveAndFlush(scheduleTemplate);
    }
}
