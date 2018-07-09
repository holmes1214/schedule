package com.evtape.schedule.serivce.leave;

import com.evtape.schedule.domain.ScheduleLeave;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by holmes1214 on 2018/5/12.
 */
@Service("handler_leave2_sub8")
public class MaleAccompanyBearLeaveHandler extends AbstractLeaveHandler implements LeaveHandler {

    /**
     * 男方陪产假
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
