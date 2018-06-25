package com.evtape.schedule.serivce.leave;

import com.evtape.schedule.domain.LeaveDaySet;
import com.evtape.schedule.domain.ScheduleInfo;
import com.evtape.schedule.domain.ScheduleLeave;
import com.evtape.schedule.domain.User;
import com.evtape.schedule.persistent.Repositories;
import com.evtape.schedule.persistent.UserRepository;
import com.evtape.schedule.util.DateUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by holmes1214 on 2018/5/12.
 */
@Service("handler_leave1_sub1")
public class AnnualLeaveHandler extends AbstractLeaveHandler implements LeaveHandler {

    @Autowired
    private UserRepository userRepository;

    /**
     * 年假实现，将时间段内每天生成两条请假数据，一条是请假人，另一条是替班人，请假人工时计负数，替班人工时计正数
     * @param scheduleInfoId
     * @param leaveHours
     * @param instead
     * @param leaveDays
     * @param content
     * @return
     */
    @Override
    public List<ScheduleLeave> processLeaveHours(Integer scheduleInfoId, Integer leaveHours, Integer instead, Integer leaveDays, String content) {
        ScheduleInfo schedule = Repositories.scheduleInfoRepository.findOne(scheduleInfoId);
        Integer userId = schedule.getUserId();
        User leaveUser = userRepository.findOne(userId);
        User insteadUser = userRepository.findOne(instead);
        String startDate = schedule.getDateStr();
        LeaveDaySet conf = Repositories.leaveDaySetRepository.findByLeaveTypeAndSubType(1, 1);


        Date start=getLeaveDate(startDate);
        List<ScheduleLeave> result=new ArrayList<>();
        for(int i=0;i<leaveDays;i++){
            Date date= DateUtils.addDays(start,i);
            String dateStr=getLeaveDateStr(date);
            ScheduleInfo info=Repositories.scheduleInfoRepository.findByUserIdAndDateStr(leaveUser,dateStr);
            ScheduleInfo info2=Repositories.scheduleInfoRepository.findByUserIdAndDateStr(insteadUser,dateStr);
            ScheduleLeave leave1=new ScheduleLeave();
            leave1.setComment(content);
            leave1.setDistrictId(schedule.getDistrictId());
            leave1.setScheduleInfoId(info.getId());
            leave1.setLeaveDesc(conf.getDescription());
            leave1.setUserId(userId);
            leave1.setInstead(0);
            //年假计算8小时，所以设置不计算原工时，工时计为8
            leave1.setLeaveHours(8);
            leave1.setCountOriginal(0);
            ScheduleLeave leave2=new ScheduleLeave();
            leave2.setComment(content);
            leave2.setDistrictId(schedule.getDistrictId());
            leave2.setScheduleInfoId(info2.getId());
            leave2.setLeaveDesc(conf.getDescription());
            leave2.setUserId(instead);
            leave2.setInstead(1);
            //年假替班公司计算为被替人当天的原工时，不计算自己原工时
            leave2.setLeaveHours(schedule.getWorkingHours());
            leave2.setCountOriginal(0);
            result.add(leave1);
            result.add(leave2);
        }

        Repositories.scheduleLeaveRepository.save(result);
        return result;
    }
}
