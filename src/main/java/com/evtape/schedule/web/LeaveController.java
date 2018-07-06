package com.evtape.schedule.web;

import com.evtape.schedule.consts.ResponseMeta;
import com.evtape.schedule.domain.ScheduleLeave;
import com.evtape.schedule.domain.vo.ResponseBundle;
import com.evtape.schedule.persistent.Repositories;
import com.evtape.schedule.serivce.leave.LeaveHandler;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
    @ApiOperation(value = "请假接口", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "scheduleInfoId", value = "请假当天排班id", required = true, paramType = "query",
                    dataType = "int"),
            @ApiImplicitParam(name = "leaveType", value = "请假字典类别", required = true, paramType = "query",
                    dataType = "int"),
            @ApiImplicitParam(name = "instead", value = "替换人/班 id", required = false, paramType = "query",
                    dataType = "int"),
            @ApiImplicitParam(name = "subType", value = "请假字典子类别", required = true, paramType = "query",
                    dataType = "int"),
            @ApiImplicitParam(name = "leaveCount", value = "离岗时间，天或小时", required = true, paramType = "query",
                    dataType = "int"),
            @ApiImplicitParam(name = "content", value = "备注", required = true, paramType = "query",
                    dataType = "String"),
    })
    @ResponseBody
    @PostMapping
    public Object leave(@RequestParam("scheduleInfoId") Integer scheduleInfoId,@RequestParam("leaveType") Integer leaveType,@RequestParam("instead") Integer instead,
                                      @RequestParam("subType") Integer subType,@RequestParam("leaveCount") Integer leaveCount,@RequestParam("content") String content){
        String handlerName="handler_leave"+leaveType+"_sub"+subType;
        try {

            List<ScheduleLeave> scheduleLeaves = handlerMap.get(handlerName).processLeaveHours(scheduleInfoId, leaveCount,instead,content,leaveType,subType);
            return new ResponseBundle().success(scheduleLeaves);
        }catch (Exception e){
            logger.error("error: ",e);
            return new ResponseBundle().failure(ResponseMeta.BAD_REQUEST,e.getMessage());
        }
    }

    @ApiOperation(value = "取消离岗", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "scheduleInfoId", value = "请假当天排班id", required = true, paramType = "body",
                    dataType = "int"),
    })
    @ResponseBody
    @PutMapping
    public Object leave(@RequestBody Integer scheduleInfoId){
        try {
            Repositories.scheduleLeaveRepository.deleteByScheduleInfoId(scheduleInfoId);
            return new ResponseBundle().success();
        }catch (Exception e){
            logger.error("error: ",e);
            return new ResponseBundle().failure(ResponseMeta.BAD_REQUEST,e.getMessage());
        }
    }
}
