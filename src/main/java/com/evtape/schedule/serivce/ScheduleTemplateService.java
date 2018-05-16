package com.evtape.schedule.serivce;

import com.evtape.schedule.domain.DutyClass;
import com.evtape.schedule.domain.ScheduleTemplate;
import com.evtape.schedule.domain.ScheduleWorkFlow;
import com.evtape.schedule.persistent.Repositories;

import java.util.List;

/**
 * Created by holmes1214 on 2018/5/16.
 */
public class ScheduleTemplateService {

    public void saveTemplate(Integer dutySuiteId,Integer dutyClassId,Integer weekNum,Integer dayNum){
        ScheduleTemplate template=Repositories.scheduleTemplateRepository.findBySuiteIdAndWeekNumAndDayNum(dutySuiteId,dutyClassId,weekNum,dayNum);
        long count=Repositories.scheduleTemplateRepository.countBySuiteIdAndClassId(dutySuiteId,dutyClassId);
        List<ScheduleWorkFlow> workFlows=Repositories.workflowRepository.findBySuiteIdAndClassId(dutySuiteId,dutyClassId);
        DutyClass dutyClass = Repositories.dutyClassRepository.findOne(dutyClassId);
        if (template==null){
            template=new ScheduleTemplate();
            template.setSuiteId(dutySuiteId);
            template.setWeekNum(weekNum);
            template.setDayNum(dayNum);
            template.setDistrictId(dutyClass.getDistrictId());
            template.setPositionId(dutyClass.getPositionId());
            template.setStationId(dutyClass.getStationId());
        }
        template.setClassId(dutyClassId);
        template.setCellColor(dutyClass.getClassColor());
        template.setOrderIndex(weekNum*7+dayNum);
        template.setWorkingLength(dutyClass.getWorkingLength());
        if (workFlows.size()>0){
            int index= (int) (count%dutyClass.getUserCount());
            String code=dutyClass.getDutyCode()+workFlows.get(index).getWorkFlowCode();
            template.setCellCode(code);
        }else {
            template.setCellCode(dutyClass.getDutyName());
        }
        Repositories.scheduleTemplateRepository.save(template);
    }


}
