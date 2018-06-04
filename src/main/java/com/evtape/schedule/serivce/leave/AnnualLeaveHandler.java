package com.evtape.schedule.serivce.leave;

import com.evtape.schedule.domain.ScheduleInfo;
import com.evtape.schedule.domain.ScheduleLeave;
import com.evtape.schedule.persistent.Repositories;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by holmes1214 on 2018/5/12.
 */
@Service("handler_leave1_sub1")
public class AnnualLeaveHandler implements LeaveHandler {

    @Override
    public List<ScheduleLeave> processLeaveHours(Integer scheduleInfoId, Integer leaveHours, Integer instead, String content) {
        ScheduleInfo schedule = Repositories.scheduleInfoRepository.findOne(scheduleInfoId);
        return null;
    }
}
