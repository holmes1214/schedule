package com.evtape.schedule.web;

import com.evtape.schedule.consts.ResponseMeta;
import com.evtape.schedule.domain.ScheduleWorkflow;
import com.evtape.schedule.domain.ScheduleWorkflowContent;
import com.evtape.schedule.domain.vo.DutyClassVo;
import com.evtape.schedule.domain.vo.ResponseBundle;
import com.evtape.schedule.exception.ForbiddenException;
import com.evtape.schedule.persistent.Repositories;
import com.evtape.schedule.serivce.WorkflowService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "工作流程接口")
@RestController
@RequestMapping("/workflow")
public class WorkflowController {

    private static Logger logger = LoggerFactory.getLogger(WorkflowController.class);
    @Autowired
    private WorkflowService workflowService;

    @ApiOperation(value = "根据班制id查询工作流程", produces = "application/json")
    @ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "path", dataType = "integer")
    @ResponseBody
    @GetMapping("/{suiteId}")
    public ResponseBundle getAllWorkflowContent(@PathVariable("suiteId") Integer suiteId) {
        try {
            List<DutyClassVo> dutyClassVolist = workflowService.allWorkflowContent(suiteId);
            return new ResponseBundle().success(dutyClassVolist);
        } catch (Exception e) {
            logger.error("error:",e );
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    @ApiOperation(value = "更新工作流程", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "工作流程id", required = true, paramType = "body", dataType = "integer"),
            @ApiImplicitParam(name = "code", value = "流程code", required = true, paramType = "body", dataType =
                    "string"),
    })
    @ResponseBody
    @PutMapping
    public ResponseBundle updateWorkflow(@RequestBody ScheduleWorkflow scheduleWorkflow) {
        try {
            ScheduleWorkflow oldbean = Repositories.workflowRepository.findOne(scheduleWorkflow.getId());
            oldbean.setCode(scheduleWorkflow.getCode());
            oldbean = Repositories.workflowRepository.saveAndFlush(oldbean);
            return new ResponseBundle().success(oldbean);
        } catch (Exception e) {
            logger.error("error:",e );
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    @ApiOperation(value = "新增工作流程描述", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "workFlowId", value = "流程id", required = true, paramType = "body", dataType =
                    "integer"),
            @ApiImplicitParam(name = "startTime", value = "开始时间", required = true, paramType = "body", dataType =
                    "integer"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", required = true, paramType = "body", dataType =
                    "integer"),
            @ApiImplicitParam(name = "content", value = "描述", required = true, paramType = "body", dataType = "string"),
            @ApiImplicitParam(name = "color", value = "颜色", required = true, paramType = "body", dataType = "string"),
            @ApiImplicitParam(name = "lineNumber", value = "第几行", required = true, paramType = "body", dataType =
                    "integer"),
    })

    @ResponseBody
    @PostMapping("/content")
    public ResponseBundle addContent(@RequestBody ScheduleWorkflowContent scheduleWorkflowContent) {
        try {
            ScheduleWorkflow scheduleWorkflow = Repositories.workflowRepository.findOne(scheduleWorkflowContent
                    .getWorkFlowId());
            scheduleWorkflowContent.setClassId(scheduleWorkflow.getClassId());
            scheduleWorkflowContent.setDistrictId(scheduleWorkflow.getDistrictId());
            scheduleWorkflowContent.setPositionId(scheduleWorkflow.getPositionId());
            scheduleWorkflowContent.setStationId(scheduleWorkflow.getStationId());
            scheduleWorkflowContent.setSuiteId(scheduleWorkflow.getSuiteId());
            Repositories.contentRepository.saveAndFlush(scheduleWorkflowContent);
            return new ResponseBundle().success(scheduleWorkflowContent);
        } catch (Exception e) {
            logger.error("error:",e );
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    @ApiOperation(value = "更新工作流程描述", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true, paramType = "path", dataType =
                    "integer"),
            @ApiImplicitParam(name = "content", value = "内容", required = true, paramType = "query", dataType =
                    "string"),
            @ApiImplicitParam(name = "color", value = "颜色值", required = true, paramType = "query", dataType =
                    "string")
    })
    @ResponseBody
    @PutMapping("/content/{id}")
    public ResponseBundle updateContent(@PathVariable Integer id, @RequestBody Content form) {
        Subject currentUser = SecurityUtils.getSubject();
        if (currentUser.hasRole("role:district")) {
            ScheduleWorkflowContent scheduleWorkflowContent = Repositories.contentRepository.findOne(id);
            scheduleWorkflowContent.setContent(form.getContent());
            scheduleWorkflowContent.setColor(form.getColor());
            Repositories.contentRepository.saveAndFlush(scheduleWorkflowContent);
            return new ResponseBundle().success();
        } else {
            throw new ForbiddenException();
        }
    }

    @ApiOperation(value = "删除工作流程描述", produces = "application/json")
    @ApiImplicitParam(name = "contentId", value = "contentId", required = true, paramType = "path", dataType =
            "integer")
    @ResponseBody
    @DeleteMapping("/content/{contentId}")
    public ResponseBundle deleteContent(@PathVariable("contentId") Integer contentId) {
        try {
            Repositories.contentRepository.delete(contentId);
            return new ResponseBundle().success();
        } catch (Exception e) {
            logger.error("error:",e );
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    @Data
    private static class Content {
        private String content;
        private String color;
    }
}
