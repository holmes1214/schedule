package com.evtape.schedule.serivce;

import com.evtape.schedule.domain.DutyClass;
import com.evtape.schedule.domain.ScheduleWorkflow;
import com.evtape.schedule.domain.ScheduleWorkflowContent;
import com.evtape.schedule.domain.vo.DutyClassVo;
import com.evtape.schedule.domain.vo.ScheduleWorkflowVo;
import com.evtape.schedule.persistent.Repositories;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by holmes1214 on 2018/7/6.
 */
@Service
public class WorkflowService {

    public List<DutyClassVo> allWorkflowContent(Integer suiteId) {
        List<DutyClassVo> dutyClassVolist = new ArrayList<>();
        //循环班次，拿到scheduleWorkflowlist
        List<DutyClass> classlist = Repositories.dutyClassRepository.findBySuiteId(suiteId);
        for (DutyClass dutyClass : classlist) {

            DutyClassVo dutyClassVo = new DutyClassVo();
//                dutyClassVo = (DutyClassVo) dutyClass;

            try {
                BeanUtils.copyProperties(dutyClassVo, dutyClass);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }

            List<ScheduleWorkflow> scheduleWorkflowlist = Repositories.workflowRepository
                    .findBySuiteIdAndClassId(suiteId, dutyClass.getId());
            // 第一次进来，先把班次对应的flow补全入库
            if (scheduleWorkflowlist == null || scheduleWorkflowlist.size() == 0) {
                scheduleWorkflowlist = new ArrayList<>();
                for (int i = 0; i < dutyClass.getUserCount(); i++) {
                    ScheduleWorkflow scheduleWorkflow = new ScheduleWorkflow();
                    scheduleWorkflow.setClassId(dutyClass.getId());
                    scheduleWorkflow.setDistrictId(dutyClass.getDistrictId());
                    scheduleWorkflow.setPositionId(dutyClass.getPositionId());
                    scheduleWorkflow.setStationId(dutyClass.getStationId());
                    scheduleWorkflow.setSuiteId(suiteId);
                    scheduleWorkflowlist.add(scheduleWorkflow);
                }
                Repositories.workflowRepository.save(scheduleWorkflowlist);
                Repositories.workflowRepository.flush();
                scheduleWorkflowlist = Repositories.workflowRepository
                        .findBySuiteIdAndClassId(suiteId, dutyClass.getId());
            }
            List<ScheduleWorkflowVo> scheduleWorkflowVolist = new ArrayList<ScheduleWorkflowVo>();
            for (int i = 0; i < scheduleWorkflowlist.size(); i++) {
                ScheduleWorkflowVo scheduleWorkflowVo = new ScheduleWorkflowVo();
//                    scheduleWorkflowVo = (ScheduleWorkflowVo) scheduleWorkflowlist.get(i);

                try {
                    BeanUtils.copyProperties(scheduleWorkflowVo, scheduleWorkflowlist.get(i));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }


                List<ScheduleWorkflowContent> contentlist = Repositories.contentRepository
                        .findByWorkFlowId(scheduleWorkflowVo.getId());
                if (!(i < dutyClass.getUserCount())) {
                    // 此种情况，只发生在用户改小了这个班次的人数，删掉多余的content和flow
                    Repositories.contentRepository.delete(contentlist);
                    Repositories.contentRepository.flush();
                    Repositories.workflowRepository.delete(scheduleWorkflowVo.getId());
                    Repositories.workflowRepository.flush();
                    continue;
                }
                scheduleWorkflowVo.setContentlist(contentlist);
                scheduleWorkflowVolist.add(scheduleWorkflowVo);
            }
            dutyClassVo.setScheduleWorkflowVolist(scheduleWorkflowVolist);
            dutyClassVolist.add(dutyClassVo);
        }
        return dutyClassVolist;
    }
}
