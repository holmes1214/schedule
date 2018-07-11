package com.evtape.schedule.serivce.leave;

import com.evtape.schedule.domain.LeaveDaySet;
import com.evtape.schedule.domain.ScheduleInfo;
import com.evtape.schedule.domain.ScheduleLeave;
import com.evtape.schedule.persistent.Repositories;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by holmes1214 on 2018/5/12.
 */
@Service("handler_leave4_sub1")
public class TrainingTaskHandler extends AbstractLeaveHandler implements LeaveHandler {


    /**
     * 临时安排-培训
     *
     * @param scheduleInfoId
     * @param leaveCount
     * @param instead
     * @param content
     * @return
     */
    @Override
    public List<ScheduleLeave> processLeaveHours(Integer scheduleInfoId, Double leaveCount, Integer instead, String content,Integer type,Integer subType) {
        ScheduleInfo schedule = Repositories.scheduleInfoRepository.findOne(scheduleInfoId);
        Integer userId = schedule.getUserId();
        String startDate = schedule.getDateStr();
        LeaveDaySet conf = Repositories.leaveDaySetRepository.findByLeaveTypeAndSubType(type, subType);

        Date start = getLeaveDate(startDate);
        List<ScheduleLeave> result = new ArrayList<>();
        List<ScheduleInfo> modifiedSchedule = new ArrayList<>();
        String dateStr = getLeaveDateStr(start);
        ScheduleInfo info = Repositories.scheduleInfoRepository.findByUserIdAndDateStr(userId, dateStr);
        ScheduleLeave leave1 = getLeaveInfo(schedule.getDistrictId(), schedule.getUserId(), info.getId(), conf.getDescription(), content,type,subType);
        leave1.setLeaveHours(leaveCount);
        leave1.setCountOriginal(1);
        result.add(leave1);

        //将排班信息设置为修改，方便查询是否有请假数据
        info.setModified(1);
        modifiedSchedule.add(info);

        Repositories.scheduleLeaveRepository.save(result);
        Repositories.scheduleInfoRepository.save(modifiedSchedule);
        return result;
    }

}
