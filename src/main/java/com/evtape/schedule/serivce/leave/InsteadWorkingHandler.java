package com.evtape.schedule.serivce.leave;

import com.evtape.schedule.domain.ScheduleLeave;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by holmes1214 on 2018/5/12.
 */
@Service("handler_leave7_sub1")
public class InsteadWorkingHandler extends AbstractLeaveHandler implements LeaveHandler {

    /**
     * 替班
     * @param scheduleInfoId
     * @param leaveCount
     * @param instead
     * @param content
     * @return
     */
    @Override
    public List<ScheduleLeave> processLeaveHours(Integer scheduleInfoId, Double leaveCount, Integer instead, String content,Integer type,Integer subType) {
        return super.processLeaveHours(scheduleInfoId,leaveCount,instead,content,type,subType);
    }
}
