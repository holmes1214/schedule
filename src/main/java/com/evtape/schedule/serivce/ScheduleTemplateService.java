package com.evtape.schedule.serivce;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

import javax.transaction.Transactional;

import com.evtape.schedule.consts.ResponseMeta;
import com.evtape.schedule.exception.BaseException;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.evtape.schedule.domain.DutyClass;
import com.evtape.schedule.domain.DutySuite;
import com.evtape.schedule.domain.ScheduleInfo;
import com.evtape.schedule.domain.ScheduleTemplate;
import com.evtape.schedule.domain.ScheduleUser;
import com.evtape.schedule.domain.ScheduleWorkflow;
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
        Thread t=new Thread(task);
        t.start();
        try {
            List<ScheduleTemplate> templates=task.get(5l, TimeUnit.SECONDS);
            Repositories.scheduleUserRepository.deleteInBatch(Repositories.scheduleUserRepository.findBySuiteId(suiteId));
            Repositories.scheduleTemplateRepository.deleteInBatch(Repositories.scheduleTemplateRepository.findBySuiteId(suiteId));
            Repositories.scheduleTemplateRepository.flush();
            Repositories.scheduleTemplateRepository.save(templates);
            return templates;
        } catch (Exception e) {
            //TODO STOP calculate
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

	/**
	 * 排班模板交换任务
	 */
	public void exchangeTemplate(Integer suiteId, Integer weekNum1, Integer dayNum1, Integer weekNum2,
			Integer dayNum2) {
		ScheduleTemplate template1 = Repositories.scheduleTemplateRepository.
				findBySuiteIdAndWeekNumAndDayNum(suiteId, weekNum1, dayNum1);
		ScheduleTemplate template2 = Repositories.scheduleTemplateRepository.
				findBySuiteIdAndWeekNumAndDayNum(suiteId, weekNum2, dayNum2);
		if (template1 == null && template2 == null) {
			return;
		} 
		if (template1 != null) {
			template1.setWeekNum(-1);
			template1.setDayNum(-1);
			template1.setOrderIndex(-1);
			Repositories.scheduleTemplateRepository.saveAndFlush(template1);
		}
		if (template2 != null) {
			template2.setWeekNum(weekNum1);
			template2.setDayNum(dayNum1);
			template2.setOrderIndex(weekNum1 * 7 + dayNum1);
			Repositories.scheduleTemplateRepository.saveAndFlush(template2);
		}
		if (template1 != null) {
			template1.setWeekNum(weekNum2);
			template1.setDayNum(dayNum2);
			//////////
			template1.setOrderIndex(weekNum2 * 7 + dayNum2);
			Repositories.scheduleTemplateRepository.saveAndFlush(template1);
		}
	}
	/**
	 * 排班模板设置人员
	 */
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
    
	/**
	 * 排班模板取消人员设置
	 */
    public void removeScheduleUser(Integer suiteId,Integer weekNum){
        ScheduleUser user=Repositories.scheduleUserRepository.findBySuiteIdAndWeekNum(suiteId,weekNum);
        Repositories.scheduleUserRepository.delete(user);
    }
	/**
	 * 生成排班计划
	 */
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

}

