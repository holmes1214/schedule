package com.evtape.schedule.serivce.leave;

import com.evtape.schedule.domain.LeaveDaySet;
import com.evtape.schedule.domain.ScheduleInfo;
import com.evtape.schedule.domain.ScheduleLeave;
import com.evtape.schedule.domain.User;
import com.evtape.schedule.persistent.Repositories;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by holmes1214 on 2018/5/12.
 */
@Service("handler_leave8_sub1")
public class WorkerExchangeHandler extends TrainingTaskHandler implements LeaveHandler {


    /**
     * 调离
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

        Date start = getLeaveDate(startDate);
        List<ScheduleLeave> result = new ArrayList<>();
        List<ScheduleInfo> infoList = Repositories.scheduleInfoRepository.findByUserWorkLeft(userId, start);
        infoList.forEach(i->i.setUserId(instead));


        Repositories.scheduleLeaveRepository.save(result);
        Repositories.scheduleInfoRepository.save(infoList);
        return result;
    }

}
