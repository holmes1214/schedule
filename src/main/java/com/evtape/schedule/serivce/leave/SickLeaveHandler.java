package com.evtape.schedule.serivce.leave;

import com.evtape.schedule.domain.LeaveDaySet;
import com.evtape.schedule.domain.ScheduleInfo;
import com.evtape.schedule.domain.ScheduleLeave;
import com.evtape.schedule.domain.User;
import com.evtape.schedule.persistent.Repositories;
import com.evtape.schedule.persistent.UserRepository;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by holmes1214 on 2018/5/12.
 */
@Service("handler_leave2_sub1")
public class SickLeaveHandler extends AbstractLeaveHandler implements LeaveHandler {

    /**
     * 病假
     * @param scheduleInfoId
     * @param leaveCount
     * @param instead
     * @param content
     * @return
     */
    @Override
    public List<ScheduleLeave> processLeaveHours(Integer scheduleInfoId, Integer leaveCount, Integer instead, String content,Integer type,Integer subType) {
        return super.processLeaveHours(scheduleInfoId,leaveCount,instead,content,type,subType);
    }
}
