package com.evtape.schedule.web;

import com.evtape.schedule.consts.ResponseMeta;
import com.evtape.schedule.domain.*;
import com.evtape.schedule.domain.form.ScheduleForm;
import com.evtape.schedule.domain.form.ScheduleUserForm;
import com.evtape.schedule.domain.vo.DutyClassVo;
import com.evtape.schedule.domain.vo.ResponseBundle;
import com.evtape.schedule.exception.BaseException;
import com.evtape.schedule.persistent.Repositories;
import com.evtape.schedule.serivce.ScheduleTemplateService;
import com.evtape.schedule.serivce.WorkflowService;
import com.evtape.schedule.web.auth.Identity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            return returnTemplate(suiteId,templates);
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
            return returnTemplate(suiteId,templates);
        } catch (Exception e) {
            logger.error("error:", e);
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
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

    private ResponseBundle returnTemplate(Integer suiteId,List<ScheduleTemplate> templates) {
        Map<String, Object> result = new HashMap<>();
        DutySuite dutySuite = Repositories.dutySuiteRepository.findOne(suiteId);
        List<User> userList;
        List<ScheduleUser> users = Repositories.scheduleUserRepository.findBySuiteIdOrderByWeekNum(suiteId);
        if (dutySuite.getBackup() == 1) {
            userList = Repositories.userRepository.findByDistrictIdAndBackup(dutySuite.getDistrictId(), 1);
        } else {
            userList = Repositories.userRepository.findByDistrictIdAndStationId(dutySuite.getDistrictId(),
                    dutySuite.getStationId());
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
        result.put("weeks", templates.isEmpty()?0:templates.stream().mapToInt(i -> i.getWeekNum()).max().getAsInt());
        result.put("scheduleUsers", users);
        return new ResponseBundle().success(result);
    }

    @ApiOperation(value = "排班模板删除一周", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "query", dataType = "integer"),
            @ApiImplicitParam(name = "weekNum", value = "被删除的周数", required = true, paramType = "query", dataType = "integer"),})
    @ResponseBody
    @DeleteMapping("/deleteoneweek")
    public ResponseBundle deleteOneWeek(@RequestParam("suiteId") Integer suiteId, @RequestParam("weekNum") Integer weekNum) {
        try {
            ScheduleUser user = Repositories.scheduleUserRepository.findBySuiteIdAndWeekNum(suiteId, weekNum);
            if (user != null) {
                Repositories.scheduleUserRepository.delete(user);
                Repositories.scheduleUserRepository.flush();
            }
            List<ScheduleTemplate> todoList = Repositories.scheduleTemplateRepository
                    .findBySuiteIdOrderByOrderIndex(suiteId);
            List<ScheduleTemplate> updateList = new ArrayList<>();
            List<ScheduleTemplate> deleteList = new ArrayList<>();
            for (ScheduleTemplate scheduleTemplate : todoList) {
                if (weekNum > scheduleTemplate.getWeekNum()) {
                    continue;
                } else if (weekNum == scheduleTemplate.getWeekNum()) {
                    deleteList.add(scheduleTemplate);
                } else {
                    scheduleTemplate.setWeekNum(scheduleTemplate.getWeekNum() - 1);
                    scheduleTemplate.setOrderIndex(scheduleTemplate.getWeekNum() * 7 + scheduleTemplate.getDayNum());
                    updateList.add(scheduleTemplate);
                }
            }
            Repositories.scheduleTemplateRepository.delete(deleteList);
            Repositories.scheduleTemplateRepository.flush();
            Repositories.scheduleTemplateRepository.save(updateList);
            return new ResponseBundle().success();
        } catch (Exception e) {
            logger.error("error:", e);
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    /**
     * setscheduleuser方法接收参数类
     *
     * @author jsychen
     */
    @ApiOperation(value = "排班模板设置人员", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "query", dataType = "integer"),
            @ApiImplicitParam(name = "weekNum", value = "被设置的周数", required = true, paramType = "query", dataType = "integer"),
            @ApiImplicitParam(name = "userId", value = "被设置人的id", required = true, paramType = "query", dataType = "integer"),})
    @ResponseBody
    @PutMapping("/setscheduleuser")
    public ResponseBundle setScheduleUser(@RequestBody ScheduleUserForm form) {
        try {
            DutySuite dutySuite = Repositories.dutySuiteRepository.findOne(form.getSuiteId());
            User u = Repositories.userRepository.findOne(form.getUserId());
            ScheduleUser user2=Repositories.scheduleUserRepository.findBySuiteIdAndUserId(form.getSuiteId(),u.getId());
            if (user2!=null){
                Repositories.scheduleUserRepository.delete(user2.getId());
                Repositories.scheduleUserRepository.flush();
            }

            //先查待设置的周有没有user
            ScheduleUser user1 = Repositories.scheduleUserRepository.findBySuiteIdAndWeekNum(form.getSuiteId(),
                    form.getWeekNum());
            user1.setUserId(u.getId());
            if (user1 == null) {
                user1=new ScheduleUser();
                user1.setDistrictId(dutySuite.getDistrictId());
                user1.setPositionId(dutySuite.getPositionId());
                user1.setStationId(dutySuite.getStationId());
                user1.setSuiteId(form.getSuiteId());
                user1.setWeekNum(form.getWeekNum());
            }
            user1.setUserName(u.getUserName());
            Repositories.scheduleUserRepository.save(user1);

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
            ScheduleUser user = Repositories.scheduleUserRepository.findBySuiteIdAndWeekNum(form.getSuiteId(), form.getWeekNum());
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
    public ResponseBundle getscheduleinfo(@Identity String phoneNumber,@RequestParam("startDateStr") String startDateStr,
                                          @RequestParam("endDateStr") String endDateStr,@RequestParam(value = "stationId",required = false) Integer stationId,
                                          @RequestParam(value = "positionId",required = false) Integer positionId,@RequestParam(value = "userName",required = false) String userName) {
        User user = Repositories.userRepository.findByPhoneNumber(phoneNumber);
        try {
//            List<ScheduleInfo> scheduleInfo = Repositories.scheduleInfoRepository.findBySuiteId(suiteId);
            return new ResponseBundle().success();
        } catch (Exception e) {
            logger.error("error:", e);
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
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
     * 手动排班创建数据
     */
    @ApiOperation(value = "手动排班创建数据", produces = "application/json")
    @ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "path", dataType = "integer")
    @ResponseBody
    @PostMapping("/manualtemplate/{suiteId}")
    public ResponseBundle manualtemplate(@PathVariable("suiteId") Integer suiteId) {
        try {

            DutySuite dutySuite = Repositories.dutySuiteRepository.findOne(suiteId);
            List<User> userlist = Repositories.userRepository.findByDistrictIdAndBackup(dutySuite.getDistrictId(), 1);
            List<ScheduleUser> scheduleUsers = new ArrayList<ScheduleUser>();
            ScheduleUser scheduleUser;
            for (int i = 0; i < userlist.size(); i++) {
                scheduleUser = new ScheduleUser();
                scheduleUser.setDistrictId(dutySuite.getDistrictId());
                scheduleUser.setPositionId(dutySuite.getPositionId());
                scheduleUser.setStationId(dutySuite.getStationId());
                scheduleUser.setSuiteId(suiteId);
                scheduleUser.setWeekNum(i + 1);
                scheduleUser.setUserId(userlist.get(i).getId());
                scheduleUsers.add(scheduleUser);
            }
            Repositories.scheduleUserRepository.save(scheduleUsers);
            Repositories.scheduleUserRepository.flush();
            return new ResponseBundle().success(scheduleUsers);
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
            @ApiImplicitParam(name = "dayNum", value = "被设置的周天数", required = true, paramType = "body", dataType = "integer"),})
    @ResponseBody
    @PutMapping("/settemplateclass")
    public ResponseBundle settemplateclass(@RequestBody ScheduleUserForm form) {
        try {
            ScheduleTemplate scheduleTemplate = new ScheduleTemplate();
            DutySuite dutySuite = Repositories.dutySuiteRepository.findOne(form.getSuiteId());
            DutyClass dutyClass = Repositories.dutyClassRepository.findOne(form.getClassId());
            scheduleTemplate.setDistrictId(dutySuite.getDistrictId());
            scheduleTemplate.setSuiteId(form.getSuiteId());
            scheduleTemplate.setWeekNum(form.getWeekNum());
            scheduleTemplate.setDayNum(form.getDayNum());
            scheduleTemplate.setClassId(form.getClassId());
            scheduleTemplate.setCellColor(dutyClass.getClassColor());
            scheduleTemplate.setWorkingLength(dutyClass.getWorkingLength());
            scheduleTemplate.setDutyName(dutyClass.getDutyName());
            scheduleTemplate.setDutyCode(dutyClass.getDutyCode());
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
}
