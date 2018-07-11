package com.evtape.schedule.serivce.leave;

import com.evtape.schedule.domain.LeaveDaySet;
import com.evtape.schedule.domain.ScheduleInfo;
import com.evtape.schedule.domain.ScheduleLeave;
import com.evtape.schedule.domain.User;
import com.evtape.schedule.persistent.Repositories;
import com.evtape.schedule.persistent.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by holmes1214 on 2018/5/12.
 */
public abstract class AbstractLeaveHandler implements LeaveHandler {

    protected ScheduleLeave getLeaveInfo(Integer districtId, Integer userId, Integer scheduleInfoId, String desc, String content) {
        ScheduleLeave leave1 = new ScheduleLeave();
        leave1.setComment(content);
        leave1.setDistrictId(districtId);
        leave1.setScheduleInfoId(scheduleInfoId);
        leave1.setLeaveDesc(desc);
        leave1.setUserId(userId);
        leave1.setInstead(0);
        leave1.setLeaveHours(0d);
        leave1.setCountOriginal(0);
        return leave1;
    }

    protected ScheduleLeave getInsteadInfo(Integer districtId, Integer userId, Integer scheduleInfoId, double workingHours, String desc, String content) {
        ScheduleLeave leave1 = new ScheduleLeave();
        leave1.setComment(content);
        leave1.setDistrictId(districtId);
        leave1.setScheduleInfoId(scheduleInfoId);
        leave1.setLeaveDesc(desc);
        leave1.setUserId(userId);
        leave1.setInstead(1);
        leave1.setLeaveHours(workingHours);
        leave1.setCountOriginal(0);
        return leave1;
    }

    @Override
    public List<ScheduleLeave> processLeaveHours(Integer scheduleInfoId, Double leaveCount, Integer instead, String content, Integer type, Integer subType) {
        ScheduleInfo schedule = Repositories.scheduleInfoRepository.findOne(scheduleInfoId);
        Integer userId = schedule.getUserId();
        String startDate = schedule.getDateStr();
        LeaveDaySet conf = Repositories.leaveDaySetRepository.findByLeaveTypeAndSubType(type, subType);


        Date start = getLeaveDate(startDate);
        List<ScheduleLeave> result = new ArrayList<>();
        List<ScheduleInfo> modifiedSchedule = new ArrayList<>();
        String dateStr = getLeaveDateStr(start);
        ScheduleInfo info = Repositories.scheduleInfoRepository.findByUserIdAndDateStr(userId, dateStr);
        ScheduleInfo info2 = Repositories.scheduleInfoRepository.findByUserIdAndDateStr(instead, dateStr);
        User user=Repositories.userRepository.findOne(userId);
        User insteadUser=Repositories.userRepository.findOne(instead);
        info=completeScheduleInfo(info,user,start,dateStr);
        info2=completeScheduleInfo(info2,insteadUser,start,dateStr);
        ScheduleLeave leave1 = getLeaveInfo(schedule.getDistrictId(), schedule.getUserId(), info.getId(), conf.getDescription(), content);
        ScheduleLeave leave2 = getInsteadInfo(schedule.getDistrictId(), instead, info2.getId(), schedule.getWorkingHours(), conf.getDescription(), content);

        result.add(leave1);
        result.add(leave2);

        //将排班信息设置为修改，方便查询是否有请假数据
        info.setModified(1);
        info2.setModified(1);
        modifiedSchedule.add(info);
        modifiedSchedule.add(info2);

        Repositories.scheduleLeaveRepository.save(result);
        Repositories.scheduleInfoRepository.save(modifiedSchedule);
        return result;
    }

    private ScheduleInfo completeScheduleInfo(ScheduleInfo info, User user, Date start, String dateStr) {
        if (info!=null){
            return info;
        }
        info=new ScheduleInfo();
        info.setDistrictId(user.getDistrictId());
        info.setUserId(user.getId());
        info.setWorkingHours(0d);
        info.setDateStr(dateStr);
        info.setScheduleDate(start);
        info.setStationId(user.getStationId());
        info.setPositionId(user.getPositionId());
        info.setPositionName(user.getPositionName());
        Repositories.scheduleInfoRepository.save(info);
        return info;
    }

    public static Date getLeaveDate(String dateStr) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return df.parse(dateStr);
        } catch (ParseException e) {
        }
        return new Date(0);
    }

    public static String getLeaveDateStr(Date date) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(date);
    }
}
