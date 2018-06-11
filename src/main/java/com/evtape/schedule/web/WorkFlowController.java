package com.evtape.schedule.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.evtape.schedule.consts.ResponseMeta;
import com.evtape.schedule.domain.DutyClass;
import com.evtape.schedule.domain.ScheduleWorkflow;
import com.evtape.schedule.domain.ScheduleWorkflowContent;
import com.evtape.schedule.domain.vo.DutyClassVo;
import com.evtape.schedule.domain.vo.ResponseBundle;
import com.evtape.schedule.domain.vo.ScheduleWorkflowVo;
import com.evtape.schedule.persistent.Repositories;

/**
 * @author ripper 站列表接口,增刪改查
 */
@Controller
@RequestMapping("workflow")
public class WorkFlowController {

	@ResponseBody
	@RequestMapping(value = "/getallworkflowcontent", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseBundle getallworkflowcontent(@RequestParam("suiteId") Integer suiteId) {
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
						ScheduleWorkflow scheduleWorkflow= new ScheduleWorkflow();
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
	@ResponseBody
	@RequestMapping(value = "/updateworkflow", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseBundle updateworkflow(@RequestBody ScheduleWorkflow scheduleWorkflow) {
		try {
			Repositories.workflowRepository.saveAndFlush(scheduleWorkflow);
			return new ResponseBundle().success(scheduleWorkflow);
		} catch (Exception e) {
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/updatecontent", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseBundle updatecontent(@RequestBody ScheduleWorkflowContent scheduleWorkflowContent) {
		try {
			Repositories.contentRepository.saveAndFlush(scheduleWorkflowContent);
			return new ResponseBundle().success(scheduleWorkflowContent);
		} catch (Exception e) {
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}
	@ResponseBody
	@RequestMapping(value = "/deletecontent", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseBundle deletecontent(@RequestBody ScheduleWorkflowContent scheduleWorkflowContent) {
		try {
			Repositories.contentRepository.delete(scheduleWorkflowContent);
			return new ResponseBundle().success(scheduleWorkflowContent);
		} catch (Exception e) {
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}
}
