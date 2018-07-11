package com.evtape.schedule.serivce;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import javax.swing.*;
import javax.transaction.Transactional;

import com.evtape.schedule.consts.ResponseMeta;
import com.evtape.schedule.domain.*;
import com.evtape.schedule.exception.BaseException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.evtape.schedule.persistent.Repositories;
import com.evtape.schedule.support.service.ScheduleCalculator;

/**
 * Created by holmes1214 on 2018/5/16.
 */
@Service
public class ScheduleTemplateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleTemplateService.class);

    /**
     * 生成模板，先清库再入库
     */
    @Transactional
    public List<ScheduleTemplate> removeAndSaveTemplates(Integer suiteId) {
        DutySuite dutySuite = Repositories.dutySuiteRepository.findOne(suiteId);
        List<DutyClass> classList = Repositories.dutyClassRepository.findBySuiteId(suiteId);
        Callable<List<ScheduleTemplate>> onlineShopping = () ->
                ScheduleCalculator.calculate(classList, dutySuite);
        FutureTask<List<ScheduleTemplate>> task = new FutureTask<>(onlineShopping);
        Thread t = new Thread(task);
        t.start();
        try {
            List<ScheduleTemplate> templates = task.get(5l, TimeUnit.SECONDS);
            Repositories.scheduleUserRepository.deleteInBatch(Repositories.scheduleUserRepository.findBySuiteId(suiteId));
            Repositories.scheduleTemplateRepository.deleteInBatch(Repositories.scheduleTemplateRepository.findBySuiteId(suiteId));
            Repositories.scheduleTemplateRepository.flush();
            List<ScheduleWorkflow> workflows = Repositories.workflowRepository.findBySuiteId(suiteId);
            if (workflows != null && workflows.size() > 0) {
                Map<Integer, List<ScheduleWorkflow>> workflowMap = workflows.stream().collect(Collectors.groupingBy(ScheduleWorkflow::getClassId));
                Map<Integer, List<ScheduleTemplate>> map = templates.stream().collect(Collectors.groupingBy(ScheduleTemplate::getDayNum));
                for (Integer day : map.keySet()) {
                    List<ScheduleTemplate> list = map.get(day);
                    Map<Integer, Integer> indexMap = new HashMap<>();
                    for (ScheduleTemplate template :
                            list) {
                        indexMap.computeIfAbsent(template.getClassId(), k -> 0);
                        int index = indexMap.get(template.getClassId());
                        ScheduleWorkflow wf = workflowMap.get(template.getClassId()).get(index);
                        template.setWorkflowId(wf.getId());
                        template.setWorkflowCode(wf.getCode());
                        indexMap.put(template.getClassId(), index + 1);
                    }
                }
            }
            Repositories.scheduleTemplateRepository.save(templates);
            return templates;
        } catch (Exception e) {
            ScheduleCalculator.stopCalculate(t.getId());
            throw new BaseException(ResponseMeta.DUTY_PLANNING_ERROR);
        }
    }

    /**
     *
     */
    public ScheduleTemplate saveTemplate(Integer dutySuiteId, Integer weekNum, Integer dayNum) {
        ScheduleTemplate template = Repositories.scheduleTemplateRepository.findBySuiteIdAndWeekNumAndDayNum(dutySuiteId, weekNum, dayNum);
        long count = Repositories.scheduleTemplateRepository.countBySuiteIdAndClassId(dutySuiteId, template.getClassId());
        List<ScheduleWorkflow> workflows = Repositories.workflowRepository.findBySuiteIdAndClassId(dutySuiteId, template.getClassId());
        DutyClass dutyClass = Repositories.dutyClassRepository.findOne(template.getClassId());
        if (template == null) {
            template = new ScheduleTemplate();
            template.setSuiteId(dutySuiteId);
            template.setWeekNum(weekNum);
            template.setDayNum(dayNum);
            template.setDistrictId(dutyClass.getDistrictId());
            template.setPositionId(dutyClass.getPositionId());
            template.setStationId(dutyClass.getStationId());
            template.setOrderIndex(weekNum * 7 + dayNum);
        }
        template.setClassId(template.getClassId());
        template.setCellColor(dutyClass.getClassColor());
        template.setOrderIndex(weekNum * 7 + dayNum);
        template.setWorkingLength(dutyClass.getWorkingLength());
        if (workflows.size() > 0) {
            int index = (int) (count % dutyClass.getUserCount());
            template.setWorkflowId(workflows.get(index).getId());
        }
        Repositories.scheduleTemplateRepository.save(template);
        return template;
    }

    /**
     * 排班模板交换任务
     */
    public void exchangeTemplate(Integer suiteId, Integer weekNum1, Integer dayNum1, Integer weekNum2,
                                 Integer dayNum2) {
        ScheduleTemplate template1 = Repositories.scheduleTemplateRepository.
                findBySuiteIdAndWeekNumAndDayNum(suiteId, weekNum1, dayNum1);
        ScheduleTemplate template2 = Repositories.scheduleTemplateRepository.
                findBySuiteIdAndWeekNumAndDayNum(suiteId, weekNum2, dayNum2);

        if (template1 != null) {
            setDateInfo(template1, weekNum2, dayNum2);
            Repositories.scheduleTemplateRepository.saveAndFlush(template1);
        }
        if (template2 != null) {
            setDateInfo(template2, weekNum1, dayNum1);
            Repositories.scheduleTemplateRepository.saveAndFlush(template2);
        }
    }

    public void setDateInfo(ScheduleTemplate template, Integer week, Integer day) {
        template.setWeekNum(week);
        template.setDayNum(day);
        template.setOrderIndex(week * 7 + day);
    }

    /**
     * 排班模板设置人员
     */
    public void setScheduleUser(Integer suiteId, Integer weekNum, Integer userId) {
        ScheduleUser scheduleUser = new ScheduleUser();
        scheduleUser.setWeekNum(weekNum);
        scheduleUser.setSuiteId(suiteId);
        scheduleUser.setUserId(userId);
        DutySuite suite = Repositories.dutySuiteRepository.findOne(suiteId);
        scheduleUser.setDistrictId(suite.getDistrictId());
        scheduleUser.setStationId(suite.getStationId());
        scheduleUser.setPositionId(suite.getPositionId());
        Repositories.scheduleUserRepository.save(scheduleUser);
    }

    /**
     * 排班模板取消人员设置
     */
    public void removeScheduleUser(Integer suiteId, Integer weekNum) {
        ScheduleUser user = Repositories.scheduleUserRepository.findBySuiteIdAndWeekNum(suiteId, weekNum);
        Repositories.scheduleUserRepository.delete(user);
    }

    /**
     * 生成排班计划
     */
    @Transactional
    public List<ScheduleInfo> createScheduleInfoData(Integer suiteId, String dateStr) throws ParseException {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date from = df.parse(dateStr);
        Date now = new Date();
        List<ScheduleUser> users = Repositories.scheduleUserRepository.findBySuiteIdOrderByWeekNum(suiteId);
        List<ScheduleTemplate> templates = Repositories.scheduleTemplateRepository.findBySuiteIdOrderByOrderIndex(suiteId);
        Repositories.scheduleInfoRepository.deleteBySuiteId(suiteId);
        List<ScheduleInfo> result = new ArrayList<>();
        int weeks=templates.stream().mapToInt(t->t.getWeekNum()).max().getAsInt();
        users.forEach(u -> templates.forEach(t -> {
            Date date = getDayStr( from, u.getWeekNum(), t.getWeekNum(), t.getDayNum(), weeks);
            ScheduleInfo info = new ScheduleInfo();
            info.setDistrictId(t.getDistrictId());
            info.setSuiteId(t.getSuiteId());
            info.setPositionId(t.getPositionId());
            info.setStationId(t.getStationId());
            info.setDutyClassId(t.getClassId());
            info.setUserId(u.getUserId());
            info.setCellColor(t.getCellColor());
            info.setWorkingHours(t.getWorkingLength().doubleValue()/60);
            info.setUserName(u.getUserName());
            info.setCreateDate(now);
            info.setScheduleDate(date);
            info.setDateStr(df.format(date));
            DutyClass dutyClass = Repositories.dutyClassRepository.findOne(t.getClassId());
            if (t.getWorkflowId() != null) {
                ScheduleWorkflow workflow = Repositories.workflowRepository.findOne(t.getWorkflowId());
                info.setWorkflowId(t.getWorkflowId());
                info.setWorkflowCode(workflow.getCode());
            }
            info.setDutyName(dutyClass.getDutyName());
            info.setDutyCode(dutyClass.getDutyCode());
            info.setModified(0);
            info.setUserId(u.getUserId());
            result.add(info);
        }));
        Repositories.scheduleInfoRepository.save(result);
        return result;
    }

    private Date getDayStr( Date date, Integer weekNum, Integer weekNum1, Integer dayNum, int totalWeeks) {
        int days = ((weekNum1 + totalWeeks - weekNum) % totalWeeks) * 7 + dayNum;
        return DateUtils.addDays(date, days);
    }

    public List<ScheduleInfo> searchScheduleInfo(String startDateStr, String endDateStr, Integer districtId, Integer stationId, Integer positionId, String userName) throws ParseException {
        List<ScheduleInfo> result;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        if (StringUtils.isNotBlank(userName)) {
            List<User> users = Repositories.userRepository.findByUserNameOrEmployeeCard(userName, userName);
            result = Repositories.scheduleInfoRepository.findByUserIds(df.parse(startDateStr), df.parse(endDateStr), users.stream().map(User::getId).collect(Collectors.toList()));
        } else {
            result = Repositories.scheduleInfoRepository.findByCondition(df.parse(startDateStr), df.parse(endDateStr), districtId);
            LOGGER.debug("start date {}, end date{}, districtId {}, size {}",startDateStr,endDateStr,districtId,result.size());
            if (stationId != null) {
                result = result.stream().filter(i -> stationId.equals(i.getStationId())).collect(Collectors.toList());
            }
            if (positionId != null) {
                result = result.stream().filter(i -> positionId.equals(i.getPositionId())).collect(Collectors.toList());
            }
        }
        result.stream().filter(i -> i.getModified() > 0).forEach(info -> info.setLeaveList(Repositories.scheduleLeaveRepository.findByScheduleInfoId(info.getId())));
        return result;
    }
}

