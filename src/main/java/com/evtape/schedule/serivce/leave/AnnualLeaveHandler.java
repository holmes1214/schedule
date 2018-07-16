package com.evtape.schedule.serivce.leave;

import com.evtape.schedule.domain.LeaveDaySet;
import com.evtape.schedule.domain.ScheduleInfo;
import com.evtape.schedule.domain.ScheduleLeave;
import com.evtape.schedule.domain.User;
import com.evtape.schedule.persistent.Repositories;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by holmes1214 on 2018/5/12.
 */
@Service("handler_leave1_sub1")
public class AnnualLeaveHandler extends AbstractLeaveHandler implements LeaveHandler {


    /**
     * 年假实现，将时间段内每天生成两条请假数据，一条是请假人，另一条是替班人，请假人工时计负数，替班人工时计正数
     * @param scheduleInfoId
     * @param leaveCount
     * @param instead
     * @param content
     * @return
     */
    @Override
    public List<ScheduleLeave> processLeaveHours(Integer scheduleInfoId, Double leaveCount, Integer instead,  String content,Integer type,Integer subType) {
        ScheduleInfo schedule = Repositories.scheduleInfoRepository.findOne(scheduleInfoId);
        Integer userId = schedule.getUserId();
        String startDate = schedule.getDateStr();
        LeaveDaySet conf = Repositories.leaveDaySetRepository.findByLeaveTypeAndSubType(type, subType);


        Date start=getLeaveDate(startDate);
        List<ScheduleLeave> result=new ArrayList<>();
        List<ScheduleInfo> modifiedSchedule=new ArrayList<>();
        for(int i=0;i<leaveCount;i++){
            Date date= DateUtils.addDays(start,i);
            String dateStr=getLeaveDateStr(date);
            ScheduleInfo info=Repositories.scheduleInfoRepository.findByUserIdAndDateStr(userId,dateStr);
            ScheduleInfo info2=Repositories.scheduleInfoRepository.findByUserIdAndDateStr(instead,dateStr);
            User user=Repositories.userRepository.findOne(userId);
            User insteadUser=Repositories.userRepository.findOne(instead);
            info=completeScheduleInfo(info,user,date,dateStr);
            info2=completeScheduleInfo(info2,insteadUser,date,dateStr);
            ScheduleLeave leave1 = getLeaveInfo(schedule.getDistrictId(), schedule.getUserId(), info, conf.getDescription(), content,type,subType,insteadUser);
            leave1.setLeaveHours(8d);
            ScheduleLeave leave2=getInsteadInfo(schedule.getDistrictId(),instead,info2,schedule.getWorkingHours(),conf.getDescription(),content,type,subType,user);
            result.add(leave1);
            result.add(leave2);

            //将排班信息设置为修改，方便查询是否有请假数据
            info.setModified(1);
            info2.setModified(1);
            modifiedSchedule.add(info);
            modifiedSchedule.add(info2);
        }

        Repositories.scheduleLeaveRepository.save(result);
        Repositories.scheduleInfoRepository.save(modifiedSchedule);
        return result;
    }
}
