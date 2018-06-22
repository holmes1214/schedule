package com.evtape.schedule.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

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
@RequestMapping("workflow")
public class WorkFlowController {
	
	@ApiOperation(value = "根据班制id查询工作流程", produces = "application/json")
	@ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "path", dataType = "integer")
    @ResponseBody
    @GetMapping("/getallworkflowcontent/{suiteId}")
    public ResponseBundle getallworkflowcontent(@PathVariable("suiteId") Integer suiteId) {
        try {

            List<DutyClassVo> dutyClassVolist = new ArrayList<DutyClassVo>();
            DutyClassVo dutyClassVo;
            ScheduleWorkflowVo scheduleWorkflowVo;
            //循环班次，拿到scheduleWorkflowlist
            List<DutyClass> classlist = Repositories.dutyClassRepository.findBySuiteId(suiteId);
            for (DutyClass dutyClass : classlist) {
                dutyClassVo = (DutyClassVo) dutyClass;
                List<ScheduleWorkflow> scheduleWorkflowlist = Repositories.workflowRepository
                        .findBySuiteIdAndClassId(suiteId, dutyClass.getId());
                // 第一次进来，先把班次对应的flow补全入库
                if (scheduleWorkflowlist == null || scheduleWorkflowlist.size() == 0) {
                    scheduleWorkflowlist = new ArrayList<ScheduleWorkflow>();
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
                    scheduleWorkflowVo = (ScheduleWorkflowVo) scheduleWorkflowlist.get(i);
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
            return new ResponseBundle().success(dutyClassVolist);
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

	@ApiOperation(value = "新增工作流程", produces = "application/json")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "districtId", value = "站区id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "stationId", value = "站点id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "positionId", value = "岗位id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "classId", value = "班次id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "code", value = "流程code", required = true, paramType = "body", dataType = "string"),
	})
    @ResponseBody
    @PostMapping("/addworkflow")
    public ResponseBundle addWorkflow(@RequestBody ScheduleWorkflow scheduleWorkflow) {
        try {
            Repositories.workflowRepository.saveAndFlush(scheduleWorkflow);
            return new ResponseBundle().success(scheduleWorkflow);
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }
	
	@ApiOperation(value = "更新工作流程", produces = "application/json")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "id", value = "工作流程id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "districtId", value = "站区id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "stationId", value = "站点id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "positionId", value = "岗位id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "classId", value = "班次id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "code", value = "流程code", required = true, paramType = "body", dataType = "string"),
	})
    @ResponseBody
    @PutMapping("/updateworkflow")
    public ResponseBundle updateWorkflow(@RequestBody ScheduleWorkflow scheduleWorkflow) {
        try {
            Repositories.workflowRepository.saveAndFlush(scheduleWorkflow);
            return new ResponseBundle().success(scheduleWorkflow);
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

	
	@ApiOperation(value = "新增工作流程描述", produces = "application/json")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "id", value = "工作流程id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "districtId", value = "站区id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "stationId", value = "站点id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "positionId", value = "岗位id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "classId", value = "班次id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "workFlowId", value = "流程id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "startTime", value = "开始时间", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "endTime", value = "结束时间", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "content", value = "描述", required = true, paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "color", value = "颜色", required = true, paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "lineNumber", value = "第几行", required = true, paramType = "body", dataType = "integer"),
	})

    @ResponseBody
    @PostMapping("/addcontent")
    public ResponseBundle addContent(@RequestBody ScheduleWorkflowContent scheduleWorkflowContent) {
        try {
            Repositories.contentRepository.saveAndFlush(scheduleWorkflowContent);
            return new ResponseBundle().success(scheduleWorkflowContent);
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

	@ApiOperation(value = "更新工作流程描述", produces = "application/json")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "id", value = "工作流程id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "districtId", value = "站区id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "stationId", value = "站点id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "positionId", value = "岗位id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "classId", value = "班次id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "workFlowId", value = "流程id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "startTime", value = "开始时间", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "endTime", value = "结束时间", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "content", value = "描述", required = true, paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "color", value = "颜色", required = true, paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "lineNumber", value = "第几行", required = true, paramType = "body", dataType = "integer"),
	})

    @ResponseBody
    @PutMapping("/updatecontent")
    public ResponseBundle updateContent(@RequestBody ScheduleWorkflowContent scheduleWorkflowContent) {
        try {
            Repositories.contentRepository.saveAndFlush(scheduleWorkflowContent);
            return new ResponseBundle().success(scheduleWorkflowContent);
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

	@ApiOperation(value = "删除工作流程描述", produces = "application/json")
	@ApiImplicitParam(name = "contentId", value = "contentId", required = true, paramType = "path", dataType = "integer")
    @ResponseBody
    @DeleteMapping("/deletecontent/{contentId}")
    public ResponseBundle deletecontent(@PathVariable("contentId") Integer contentId) {
        try {
            Repositories.contentRepository.delete(contentId);
            return new ResponseBundle().success(contentId);
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }
}
