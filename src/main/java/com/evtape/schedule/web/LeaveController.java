package com.evtape.schedule.web;

import com.evtape.schedule.domain.ScheduleLeave;
import com.evtape.schedule.domain.User;
import com.evtape.schedule.persistent.Repositories;
import com.evtape.schedule.serivce.leave.LeaveHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * Created by holmes1214 on 2018/5/12.
 */
@Controller
@RequestMapping("/leave")
public class LeaveController {

    @Autowired
    private Map<String, LeaveHandler> handlerMap;

    public @ResponseBody Object leave(@RequestParam("scheduleInfoId") Integer scheduleInfoId,@RequestParam("leaveType") Integer leaveType,
                                      @RequestParam("subType") Integer subType,@RequestParam("leaveHours") Integer leaveHours){
        String handlerName="handler_leave"+leaveType+"_sub"+subType;
        List<ScheduleLeave> scheduleLeaves = handlerMap.get(handlerName).processLeaveHours(scheduleInfoId, leaveHours);
        List<User> users = Repositories.userRepository.findByDistrictId(scheduleInfoId);
        List<User> users1=Repositories.userRepository.findByDistrictIdAndStationId(scheduleInfoId,leaveHours);
        return null;
    }
}
