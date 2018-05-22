package com.evtape.schedule.serivce;

import com.evtape.schedule.domain.*;
import com.evtape.schedule.persistent.Repositories;
import com.evtape.schedule.util.DateUtil;
import org.apache.commons.lang3.time.DateUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by holmes1214 on 2018/5/16.
 */
public class ScheduleTemplateService {

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
            template.setOrderIndex(weekNum*7+dayNum);
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

    public void exchangeTemplate(Integer suiteId, Integer classId, Integer weekNum1, Integer dayNum1, Integer weekNum2, Integer dayNum2) {
        ScheduleTemplate template1 = Repositories.scheduleTemplateRepository.findBySuiteIdAndWeekNumAndDayNum(suiteId, weekNum1, dayNum1);
        ScheduleTemplate template2 = Repositories.scheduleTemplateRepository.findBySuiteIdAndWeekNumAndDayNum(suiteId, weekNum2, dayNum2);
        if (template1 == null && template2 == null) {
            return;
        } else if (template2 != null) {
            template2.setWeekNum(weekNum1);
            template2.setDayNum(dayNum1);
            template2.setOrderIndex(weekNum1*7+dayNum1);
            Repositories.scheduleTemplateRepository.save(template2);
        } else if (template1 != null) {
            template1.setWeekNum(weekNum2);
            template1.setDayNum(dayNum2);
            template2.setOrderIndex(weekNum2*7+dayNum2);
            Repositories.scheduleTemplateRepository.save(template1);
        }
    }

    public void setScheduleUser(Integer suiteId,Integer weekNum,Integer userId){
        ScheduleUser scheduleUser=new ScheduleUser();
        scheduleUser.setWeekNum(weekNum);
        scheduleUser.setSuiteId(suiteId);
        scheduleUser.setUserId(userId);
        DutySuite suite = Repositories.dutySuiteRepository.findOne(suiteId);
        scheduleUser.setDistrictId(suite.getDistrictId());
        scheduleUser.setStationId(suite.getStationId());
        scheduleUser.setPositionId(suite.getPositionId());
        Repositories.scheduleUserRepository.save(scheduleUser);
    }

    public void removeScheduleUser(Integer suiteId,Integer weekNum){
        ScheduleUser user=Repositories.scheduleUserRepository.findBySuiteIdAndWeekNum(suiteId,weekNum);
        Repositories.scheduleUserRepository.delete(user);
    }

    public List<ScheduleInfo> createScheduleInfoData(Integer suiteId,String dateStr) throws ParseException {
        DateFormat df =new SimpleDateFormat("yyyyMMdd");
        Date from = df.parse(dateStr);
        Date now=new Date();
        List<ScheduleUser> users=Repositories.scheduleUserRepository.findBySuiteIdOrderByWeekNum(suiteId);
        List<ScheduleTemplate> templates=Repositories.scheduleTemplateRepository.findBySuiteIdOrderByOrderIndex(suiteId);
        Repositories.scheduleInfoRepository.deleteBySuiteIdAndDateStr(suiteId,dateStr);
        List<ScheduleInfo> result=new ArrayList<>();
        users.forEach(u-> templates.forEach(t->{
            String dayStr=getDayStr(df ,from,u.getWeekNum(),t.getWeekNum(),t.getDayNum(),users.size());
            ScheduleInfo info=new ScheduleInfo();
            info.setDistrictId(t.getDistrictId());
            info.setUserId(u.getUserId());
            info.setCreateDate(now);
            info.setDateStr(dayStr);
            info.setDutyClassId(t.getClassId());
            DutyClass dutyClass=Repositories.dutyClassRepository.findOne(t.getClassId());
            if (t.getWorkflowId()!=null){
                ScheduleWorkflow workflow = Repositories.workflowRepository.findOne(t.getWorkflowId());
                info.setWorkflowId(t.getWorkflowId());
                info.setWorkflowCode(workflow.getCode());
            }
            info.setDutyName(dutyClass.getDutyName());
            info.setDutyCode(dutyClass.getDutyCode());
            info.setDutySuiteId(suiteId);
            info.setModified(0);
            info.setUserId(u.getUserId());
        }));
        Repositories.scheduleInfoRepository.save(result);
        return result;
    }

    private String getDayStr(DateFormat df,Date date,Integer weekNum, Integer weekNum1, Integer dayNum,int totalWeeks) {
        int days=((weekNum1+totalWeeks-weekNum)%totalWeeks)*7+dayNum;
        return df.format(DateUtils.addDays(date,days));
    }

    public static void main(String[] a ){
        System.out.print(-1%7);
    }
}

