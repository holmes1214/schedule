package com.evtape.schedule.web;

import com.evtape.schedule.consts.ResponseMeta;
import com.evtape.schedule.domain.ScheduleInfo;
import com.evtape.schedule.domain.ScheduleLeave;
import com.evtape.schedule.domain.form.LeaveForm;
import com.evtape.schedule.domain.vo.ResponseBundle;
import com.evtape.schedule.persistent.Repositories;
import com.evtape.schedule.serivce.leave.LeaveHandler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by holmes1214 on 2018/5/12.
 */
@Api(value = "请假接口")
@RestController
@RequestMapping(value = "/leave", produces = "application/json;charset=UTF-8")
public class LeaveController {

    private static Logger logger = LoggerFactory.getLogger(LeaveController.class);

    @Autowired
    private Map<String, LeaveHandler> handlerMap;

    @ApiOperation(value = "请假", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "scheduleInfoId", value = "请假当天排班id", required = true, paramType = "body",
                    dataType = "int"),
            @ApiImplicitParam(name = "leaveType", value = "请假字典类别", required = true, paramType = "body",
                    dataType = "int"),
            @ApiImplicitParam(name = "instead", value = "替换人/班 id", paramType = "body",
                    dataType = "int"),
            @ApiImplicitParam(name = "subType", value = "请假字典子类别", required = true, paramType = "body",
                    dataType = "int"),
            @ApiImplicitParam(name = "leaveCount", value = "离岗时间，天或小时", required = true, paramType = "body",
                    dataType = "double"),
            @ApiImplicitParam(name = "content", value = "备注", paramType = "body",
                    dataType = "String"),
    })
    @PostMapping
    public ResponseBundle leave(@RequestBody LeaveForm form ) {
        String handlerName = "handler_leave" + form.getLeaveType() + "_sub" + form.getSubType();
        try {
            List<ScheduleLeave> scheduleLeaves = handlerMap.get(handlerName).processLeaveHours(form.getScheduleInfoId(),
                    form.getLeaveCount(), form.getInstead(), form.getContent(), form.getLeaveType(), form.getSubType());
            return new ResponseBundle().success(scheduleLeaves);
        } catch (Exception e) {
            logger.error("error: ", e);
            return new ResponseBundle().failure(ResponseMeta.BAD_REQUEST, e.getMessage());
        }
    }

    @ApiOperation(value = "取消离岗", produces = "application/json")
    @ApiImplicitParam(name = "scheduleInfoId", value = "请假当天排班id", required = true, paramType = "body",
            dataType = "int")
    @PutMapping
    @Transactional
    public ResponseBundle cancelLeave(@RequestBody LeaveForm form) {
        try {
            List<ScheduleInfo> updateList=new ArrayList<>();
            ScheduleInfo info = Repositories.scheduleInfoRepository.findOne(form.getScheduleInfoId());
            updateList.add(info);
            List<ScheduleLeave> leaveList = Repositories.scheduleLeaveRepository.findByScheduleInfoId(info.getId());

            for (ScheduleLeave leave : leaveList) {
                Repositories.scheduleLeaveRepository.delete(leave);
            }

            List<Integer> userIds = leaveList.stream().filter(l -> l.getExchangeUserId() != null)
                    .map(ScheduleLeave::getExchangeUserId).collect(Collectors.toList());
            List<ScheduleLeave> others = Repositories.scheduleLeaveRepository.findByUserIdsAndDateStr(userIds, info.getDateStr());

            for (ScheduleLeave other : others) {
                Repositories.scheduleLeaveRepository.delete(other);
                long c=Repositories.scheduleLeaveRepository.countByScheduleInfoId(other.getScheduleInfoId());
                if (c==0){
                    ScheduleInfo i = Repositories.scheduleInfoRepository.findOne(other.getScheduleInfoId());
                    updateList.add(i);
                }
            }

            updateList.forEach(i->i.setModified(0));
            Repositories.scheduleInfoRepository.save(updateList);
            Repositories.scheduleLeaveRepository.deleteByScheduleInfoId(form.getScheduleInfoId());
            return new ResponseBundle().success();
        } catch (Exception e) {
            logger.error("error: ", e);
            return new ResponseBundle().failure(ResponseMeta.BAD_REQUEST, e.getMessage());
        }
    }
}
