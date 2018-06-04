package com.evtape.schedule.web;

import com.evtape.schedule.consts.ResponseMeta;
import com.evtape.schedule.domain.ScheduleLeave;
import com.evtape.schedule.domain.vo.ResponseBundle;
import com.evtape.schedule.serivce.leave.LeaveHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static Logger logger = LoggerFactory.getLogger(LeaveController.class);
    @Autowired
    private Map<String, LeaveHandler> handlerMap;

    public @ResponseBody Object leave(@RequestParam("scheduleInfoId") Integer scheduleInfoId,@RequestParam("leaveType") Integer leaveType,@RequestParam("insteadUser") Integer insteadUser,
                                      @RequestParam("subType") Integer subType,@RequestParam("leaveHours") Integer leaveHours,@RequestParam("content") String content){
        String handlerName="handler_leave"+leaveType+"_sub"+subType;
        try {

            List<ScheduleLeave> scheduleLeaves = handlerMap.get(handlerName).processLeaveHours(scheduleInfoId, leaveHours,insteadUser,content);
            return new ResponseBundle().success(scheduleLeaves);
        }catch (Exception e){
            logger.error("error: ",e);
            return new ResponseBundle().failure(ResponseMeta.BAD_REQUEST,e.getMessage());
        }
    }
}
