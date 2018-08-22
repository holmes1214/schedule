package com.evtape.schedule.serivce;

import com.evtape.schedule.consts.Constants;
import com.evtape.schedule.domain.*;
import com.evtape.schedule.domain.vo.DutyClassVo;
import com.evtape.schedule.domain.vo.ScheduleWorkflowVo;
import com.evtape.schedule.persistent.Repositories;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by holmes1214 on 2018/7/6.
 */
@Service
public class WorkflowService {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowService.class);

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

        @Scheduled(cron = "0 0 0 1 * ?")
//    @Scheduled(cron = "0 0/1 * * * ?")
    public void calcWorkLoad() throws ParseException {
        Date now = new Date();
        Date lastDay = DateUtils.addDays(now, -1);
        Date begin = DateUtils.ceiling(DateUtils.addDays(lastDay, -31), Calendar.MONTH);
        DateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT);
        String[] format = df.format(lastDay).split("-");

        boolean seasonly = false, yearly = false;
        if (format[1].equals("12")) {
            seasonly = true;
            yearly = true;
        } else if (format[1].equals("03") || format[1].equals("06") || format[1].equals("09")) {
            seasonly = true;
        }

        Map<Integer, District> districtMap = Repositories.districtRepository.findAll().stream()
                .collect(Collectors.toMap(District::getId, d -> d));
        calcData(districtMap, begin, now, format[0], null, format[1]);
        if (seasonly) {
            Date b = DateUtils.ceiling(DateUtils.addDays(lastDay, -93), Calendar.MONTH);
            int season = (int) Math.ceil(Integer.parseInt(format[1]) / 3);
            calcData(districtMap, b, now, format[0], season + "", null);
        }
        if (yearly) {
            Date b = DateUtils.ceiling(DateUtils.addDays(lastDay, -366), Calendar.YEAR);
            calcData(districtMap, b, now, format[0], null, null);
        }
    }

    private void calcData(Map<Integer, District> districtMap, Date begin, Date now, String year, String season, String month) throws ParseException {
        List<ScheduleInfo> list = Repositories.scheduleInfoRepository.findByDate(begin, now);
        Map<Integer, List<ScheduleInfo>> collect = list.stream().collect(Collectors.groupingBy(ScheduleInfo::getDistrictId));

        for (Integer districtId :
                collect.keySet()) {
            WorkLoadReport r = new WorkLoadReport();
            District d = districtMap.get(districtId);
            r.setLineNumber(d.getLineNumber());
            r.setDistrictId(d.getId());
            r.setDistrictName(d.getDistrictName());
            r.setYearStr(year);
            r.setSeasonStr(season);
            r.setMonthStr(month);
            double planned = 0d;
            double actual = 0d;
            double offWorkTimes = 0d;
            Set<Integer> userSet = new HashSet<>();
            for (ScheduleInfo info :
                    collect.get(districtId)) {
                userSet.add(info.getUserId());
                planned += info.getWorkingHours();
                actual += info.getWorkingHours();
                if (info.getModified() == 1) {
                    List<ScheduleLeave> leaveList = Repositories.scheduleLeaveRepository.findByScheduleInfoId(info.getId());

                    for (ScheduleLeave leave :
                            leaveList) {
                        if (leave.getInstead() == 0 && leave.getLeaveHours() < 0) {
                            actual += leave.getLeaveHours();
                            offWorkTimes += leave.getLeaveHours();
                        }

                    }
                }
            }
            r.setAverWorkerCount(userSet.size());
            r.setPlannedHours(planned);
            r.setActualHours(actual);
            DecimalFormat df = new DecimalFormat("######0.00");
            r.setOffWorkRate(Double.parseDouble(df.format(Math.abs(offWorkTimes) / collect.get(districtId).size())));
            double extra = (actual - planned) / planned;
            if (extra < 0) {
                r.setExtraHours(0d);
            } else {
                r.setExtraHours(extra);
            }
            r.setWorkedRate(Double.parseDouble(df.format(actual / planned)));
            logger.info(WorkflowService.class.getName() + "定时任务生成工时报表");
            Repositories.workLoadRepository.save(r);
        }
    }
}
