package com.evtape.schedule.serivce.leave;

import com.evtape.schedule.domain.ScheduleLeave;

import java.util.List;

/**
 * Created by holmes1214 on 2018/5/12.
 */
public interface LeaveHandler {

    List<ScheduleLeave> processLeaveHours(Integer scheduleInfoId,Integer leaveHours);
}
