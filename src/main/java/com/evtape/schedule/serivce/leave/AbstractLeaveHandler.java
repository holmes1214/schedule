package com.evtape.schedule.serivce.leave;

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
import java.util.Date;
import java.util.List;

/**
 * Created by holmes1214 on 2018/5/12.
 */
public abstract class AbstractLeaveHandler implements LeaveHandler {


    @Override
    public List<ScheduleLeave> processLeaveHours(Integer scheduleInfoId, Integer leaveHours, Integer instead, Integer leaveDays, String content) {
        return null;
    }

    public static Date getLeaveDate(String dateStr){
        DateFormat df =new SimpleDateFormat("yyyyMMdd");
        try {
            return df.parse(dateStr);
        } catch (ParseException e) {
        }
        return new Date(0);
    }

    public static String getLeaveDateStr(Date date){
        DateFormat df =new SimpleDateFormat("yyyyMMdd");
        return df.format(date);
    }
}
