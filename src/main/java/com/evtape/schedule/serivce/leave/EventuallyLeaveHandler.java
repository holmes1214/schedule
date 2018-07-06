package com.evtape.schedule.serivce.leave;

import com.evtape.schedule.domain.ScheduleLeave;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by holmes1214 on 2018/5/12.
 */
@Service("handler_leave2_sub2")
public class EventuallyLeaveHandler extends AbstractLeaveHandler implements LeaveHandler {

    /**
     * 事假
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
