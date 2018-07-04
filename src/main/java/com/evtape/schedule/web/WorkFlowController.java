package com.evtape.schedule.web;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.evtape.schedule.exception.ForbiddenException;
import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.springframework.web.bind.annotation.*;

import com.evtape.schedule.consts.ResponseMeta;
import com.evtape.schedule.domain.DutyClass;
import com.evtape.schedule.domain.ScheduleWorkflow;
import com.evtape.schedule.domain.ScheduleWorkflowContent;
import com.evtape.schedule.domain.vo.DutyClassVo;
import com.evtape.schedule.domain.vo.ResponseBundle;
import com.evtape.schedule.domain.vo.ScheduleWorkflowVo;
import com.evtape.schedule.persistent.Repositories;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@Api(value = "工作流程接口")
@RestController
@RequestMapping("/workflow")
public class WorkFlowController {

    @ApiOperation(value = "根据班制id查询工作流程", produces = "application/json")
    @ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "path", dataType = "integer")
    @ResponseBody
    @GetMapping("/{suiteId}")
    public ResponseBundle getallworkflowcontent(@PathVariable("suiteId") Integer suiteId) {
        try {

            List<DutyClassVo> dutyClassVolist = allworkflowContent(suiteId);
            return new ResponseBundle().success(dutyClassVolist);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

	private List<DutyClassVo> allworkflowContent(Integer suiteId) {
		List<DutyClassVo> dutyClassVolist = new ArrayList<>();
		//循环班次，拿到scheduleWorkflowlist
		List<DutyClass> classlist = Repositories.dutyClassRepository.findBySuiteId(suiteId);
		for (DutyClass dutyClass : classlist) {

		    DutyClassVo dutyClassVo = new DutyClassVo();
//                dutyClassVo = (DutyClassVo) dutyClass;

		    try {
		        BeanUtils.copyProperties(dutyClassVo, dutyClass);
		    } catch (IllegalAccessException | InvocationTargetException e) {
		        e.printStackTrace();
		    }

		    List<ScheduleWorkflow> scheduleWorkflowlist = Repositories.workflowRepository
		            .findBySuiteIdAndClassId(suiteId, dutyClass.getId());
		    // 第一次进来，先把班次对应的flow补全入库
		    if (scheduleWorkflowlist == null || scheduleWorkflowlist.size() == 0) {
		        scheduleWorkflowlist = new ArrayList<>();
		        for (int i = 0; i < dutyClass.getUserCount(); i++) {
		            ScheduleWorkflow scheduleWorkflow = new ScheduleWorkflow();
		            scheduleWorkflow.setClassId(dutyClass.getId());
		            scheduleWorkflow.setDistrictId(dutyClass.getDistrictId());
		            scheduleWorkflow.setPositionId(dutyClass.getPositionId());
		            scheduleWorkflow.setStationId(dutyClass.getStationId());
		            scheduleWorkflow.setSuiteId(suiteId);
		            scheduleWorkflowlist.add(scheduleWorkflow);
		        }
		        Repositories.workflowRepository.save(scheduleWorkflowlist);
		        Repositories.workflowRepository.flush();
		        scheduleWorkflowlist = Repositories.workflowRepository
		                .findBySuiteIdAndClassId(suiteId, dutyClass.getId());
		    }
		    List<ScheduleWorkflowVo> scheduleWorkflowVolist = new ArrayList<ScheduleWorkflowVo>();
		    for (int i = 0; i < scheduleWorkflowlist.size(); i++) {
		        ScheduleWorkflowVo scheduleWorkflowVo = new ScheduleWorkflowVo();
//                    scheduleWorkflowVo = (ScheduleWorkflowVo) scheduleWorkflowlist.get(i);

		        try {
		            BeanUtils.copyProperties(scheduleWorkflowVo, scheduleWorkflowlist.get(i));
		        } catch (IllegalAccessException | InvocationTargetException e) {
		            e.printStackTrace();
		        }


		        List<ScheduleWorkflowContent> contentlist = Repositories.contentRepository
		                .findByWorkFlowId(scheduleWorkflowVo.getId());
		        if (!(i < dutyClass.getUserCount())) {
		            // 此种情况，只发生在用户改小了这个班次的人数，删掉多余的content和flow
		            Repositories.contentRepository.delete(contentlist);
		            Repositories.contentRepository.flush();
		            Repositories.workflowRepository.delete(scheduleWorkflowVo.getId());
		            Repositories.workflowRepository.flush();
		            continue;
		        }
		        scheduleWorkflowVo.setContentlist(contentlist);
		        scheduleWorkflowVolist.add(scheduleWorkflowVo);
		    }
		    dutyClassVo.setScheduleWorkflowVolist(scheduleWorkflowVolist);
		    dutyClassVolist.add(dutyClassVo);
		}
		return dutyClassVolist;
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
            e.printStackTrace();
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
            e.printStackTrace();
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
    public ResponseBundle deletecontent(@PathVariable("contentId") Integer contentId) {
        try {
            Repositories.contentRepository.delete(contentId);
            return new ResponseBundle().success();
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    @Data
    private static class Content {
        private String content;
        private String color;
    }
}
