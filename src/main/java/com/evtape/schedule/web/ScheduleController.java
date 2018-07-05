package com.evtape.schedule.web;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.evtape.schedule.consts.ResponseMeta;
import com.evtape.schedule.domain.DutyClass;
import com.evtape.schedule.domain.DutySuite;
import com.evtape.schedule.domain.ScheduleInfo;
import com.evtape.schedule.domain.ScheduleTemplate;
import com.evtape.schedule.domain.ScheduleUser;
import com.evtape.schedule.domain.ScheduleWorkflow;
import com.evtape.schedule.domain.ScheduleWorkflowContent;
import com.evtape.schedule.domain.User;
import com.evtape.schedule.domain.form.ScheduleForm;
import com.evtape.schedule.domain.form.ScheduleUserForm;
import com.evtape.schedule.domain.vo.DutyClassVo;
import com.evtape.schedule.domain.vo.ResponseBundle;
import com.evtape.schedule.domain.vo.ScheduleWorkflowVo;
import com.evtape.schedule.exception.BaseException;
import com.evtape.schedule.persistent.Repositories;
import com.evtape.schedule.serivce.ScheduleTemplateService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * 排班
 */
@Api(value = "排班接口")
@RestController
@RequestMapping("/schedule")
public class ScheduleController {

	@Autowired
	private ScheduleTemplateService scheduleTemplateService;

    @ApiOperation(value = "删除旧排班模板，创建新模板", produces = "application/json")
	@ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "path", dataType = "integer")
	@ResponseBody
    @PostMapping("/createtemplate/{suiteId}")
	public ResponseBundle createtemplate(@PathVariable("suiteId") Integer suiteId) {
		try {
			if (Repositories.dutySuiteRepository.exists(suiteId)) {
				scheduleTemplateService.removeAndSaveTemplates(suiteId);
				return returntemplete(suiteId);
			}
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		} catch (BaseException e) {
			return new ResponseBundle().failure(e.getErrorCode());
		} catch (Exception e) {
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}

    @ApiOperation(value = "加载排班模板", produces = "application/json")
	@ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "path", dataType = "integer")
	@ResponseBody
	@GetMapping("/templatelist/{suiteId}")
	public ResponseBundle templatelist(@PathVariable("suiteId") Integer suiteId) {
		try {
			return returntemplete(suiteId);
		} catch (Exception e) {
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}

	@ApiOperation(value = "排班模板交换任务", produces = "application/json")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "query", dataType = "integer"),
			@ApiImplicitParam(name = "weekNum1", value = "任务1的周数", required = true, paramType = "query", dataType = "integer"),
			@ApiImplicitParam(name = "dayNum1", value = "任务1的天数", required = true, paramType = "query", dataType = "integer"),
			@ApiImplicitParam(name = "weekNum2", value = "任务2的周数", required = true, paramType = "query", dataType = "integer"),
			@ApiImplicitParam(name = "dayNum2", value = "任务2的天数", required = true, paramType = "query", dataType = "integer"),})
	@ResponseBody
	@PutMapping("/exchangeTemplate")
	public ResponseBundle exchangeTemplate(@RequestBody ScheduleForm form) {
		try {
			scheduleTemplateService.exchangeTemplate(form.getSuiteId(), form.getWeekNum1(), form.getDayNum1(), form.getWeekNum2(), form.getDayNum2());
			return returntemplete(form.getSuiteId());
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}

	private ResponseBundle returntemplete(Integer suiteId) {
		Map<String, Object> result = new HashMap<String, Object>();
		DutySuite dutySuite = Repositories.dutySuiteRepository.findOne(suiteId);
		List<ScheduleTemplate> templatelist = Repositories.scheduleTemplateRepository
				.findBySuiteIdOrderByOrderIndex(suiteId);
		List<User> userlist;
		List<ScheduleUser> users = Repositories.scheduleUserRepository.findBySuiteIdOrderByWeekNum(suiteId);
		if (dutySuite.getBackup() == 1) {
			userlist = Repositories.userRepository.findByDistrictIdAndBackup(dutySuite.getDistrictId(), 1);
		} else {
			userlist = Repositories.userRepository.findByDistrictIdAndStationId(dutySuite.getDistrictId(),
					dutySuite.getStationId());
		}
		// List<ScheduleUser> scheduleUserlist =
		// Repositories.scheduleUserRepository.findBySuiteIdOrderByWeekNum(suiteId);
		// result.put("scheduleUserlist", scheduleUserlist);
		result.put("templatelist", templatelist);
		result.put("userlist", userlist);
		List<DutyClass> list = Repositories.dutyClassRepository.findBySuiteId(suiteId);
		list.forEach(l -> {
			if (l.getRelevantClassId() != null) {
				l.setRelevant(Repositories.dutyClassRepository.findOne(l.getRelevantClassId()));
			}
		});
		result.put("dutyclass", list);
		result.put("weeks", templatelist.get(templatelist.size() - 1).getWeekNum());
		List<DutyClassVo> dutyClassVolist = allworkflowContent(suiteId);
		result.put("dutyClassVolist", dutyClassVolist);
		result.put("scheduleUsers", users);
		return new ResponseBundle().success(result);
	}

	@ApiOperation(value = "排班模板删除一周", produces = "application/json")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "query", dataType = "integer"),
			@ApiImplicitParam(name = "weekNum", value = "被删除的周数", required = true, paramType = "query", dataType = "integer"),})
	@ResponseBody
	@DeleteMapping("/deleteoneweek")
	public ResponseBundle deleteoneweek(@RequestParam("suiteId") Integer suiteId, @RequestParam("weekNum") Integer weekNum) {
		try {
			ScheduleUser user = Repositories.scheduleUserRepository.findBySuiteIdAndWeekNum(suiteId, weekNum);
			Repositories.scheduleUserRepository.delete(user);
			Repositories.scheduleUserRepository.flush();
			List<ScheduleTemplate> todolist = Repositories.scheduleTemplateRepository
					.findBySuiteIdOrderByOrderIndex(suiteId);
			List<ScheduleTemplate> updatelist = new ArrayList<ScheduleTemplate>();
			for (ScheduleTemplate scheduleTemplate : todolist) {
				if (weekNum > scheduleTemplate.getWeekNum()) {
					continue;
				} else if (weekNum == scheduleTemplate.getWeekNum()) {
					Repositories.scheduleTemplateRepository.delete(scheduleTemplate);
				} else {
					scheduleTemplate.setWeekNum(scheduleTemplate.getWeekNum() - 1);
					updatelist.add(scheduleTemplate);
				}
			}
			Repositories.scheduleTemplateRepository.save(updatelist);
			Repositories.scheduleTemplateRepository.flush();
			return returntemplete(suiteId);
		} catch (Exception e) {
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}
	
	/**
	 * setscheduleuser方法接收参数类
	 * @author jsychen
	 *
	 */
	@ApiOperation(value = "排班模板设置人员", produces = "application/json")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "query", dataType = "integer"),
			@ApiImplicitParam(name = "weekNum", value = "被设置的周数", required = true, paramType = "query", dataType = "integer"),
			@ApiImplicitParam(name = "userId", value = "被设置人的id", required = true, paramType = "query", dataType = "integer"),})
	@ResponseBody
	@PutMapping("/setscheduleuser")
	public ResponseBundle setscheduleuser(@RequestBody ScheduleUserForm form) {
		try {
			DutySuite dutySuite = Repositories.dutySuiteRepository.findOne(form.getSuiteId());
			User u = Repositories.userRepository.findOne(form.getUserId());
			
			//先查待设置的周有没有user
			ScheduleUser user1 = Repositories.scheduleUserRepository.findBySuiteIdAndWeekNum(form.getSuiteId(),
					form.getWeekNum());
			if (user1 != null) {
				Repositories.scheduleUserRepository.delete(user1);
				Repositories.scheduleUserRepository.flush();
			}
			
			// 再查这个人有没有之前被设置过
			ScheduleUser user2 = Repositories.scheduleUserRepository.findBySuiteIdAndUserId(form.getSuiteId(),
					form.getUserId());
			if (user2 == null) {
				user2 = new ScheduleUser();
			}
			user2.setDistrictId(dutySuite.getDistrictId());
			user2.setPositionId(dutySuite.getPositionId());
			user2.setStationId(dutySuite.getStationId());
			user2.setSuiteId(form.getSuiteId());
			user2.setWeekNum(form.getWeekNum());
			user2.setUserId(form.getUserId());
			user2.setUserName(u.getUserName());
			Repositories.scheduleUserRepository.saveAndFlush(user2);
			return new ResponseBundle()
					.success(Repositories.scheduleUserRepository.findBySuiteIdOrderByWeekNum(form.getSuiteId()));

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}

	/**
	 * 排班模板取消人员设置
	 */
	@ApiOperation(value = "排班模板取消人员设置", produces = "application/json")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "query", dataType = "integer"),
			@ApiImplicitParam(name = "weekNum", value = "被取消设置的周数", required = true, paramType = "query", dataType = "integer"),
			})
	@ResponseBody
	@PutMapping(value = "/removescheduleuser")
	public ResponseBundle removescheduleuser(@RequestBody ScheduleUserForm form) {
		try {
			ScheduleUser user = Repositories.scheduleUserRepository.findBySuiteIdAndWeekNum(form.getSuiteId(), form.getWeekNum());
			Repositories.scheduleUserRepository.delete(user);
			return returntemplete(form.getSuiteId());
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}

	/**
	 * 生成排班计划
	 */
	@ApiOperation(value = "生成排班计划", produces = "application/json")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "query", dataType = "integer"),
			@ApiImplicitParam(name = "dateStr", value = "dateStr,问一下昊哥具体含义", required = true, paramType = "query", dataType = "integer"),})
	@ResponseBody
	@PostMapping("/createscheduleinfo")
	public ResponseBundle createscheduleinfo(@RequestParam("suiteId") Integer suiteId,
			@RequestParam("dateStr") String dateStr) {
		try {
			List<ScheduleInfo> scheduleInfos = scheduleTemplateService.createScheduleInfoData(suiteId, dateStr);
			return new ResponseBundle().success(scheduleInfos);
		} catch (Exception e) {
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}

	/**
	 * 查询排班计划
	 */

	@ApiOperation(value = "查询排班计划", produces = "application/json")
	@ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "path", dataType = "integer")
	@ResponseBody
	@GetMapping("/getscheduleinfo/{suiteId}")
	public ResponseBundle getscheduleinfo(@PathVariable("suiteId") Integer suiteId) {
		try {
			List<ScheduleInfo> scheduleInfos = Repositories.scheduleInfoRepository.findBySuiteId(suiteId);
			return new ResponseBundle().success(scheduleInfos);
		} catch (Exception e) {
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}

	/**
	 * 手动排班创建数据
	 */
	@ApiOperation(value = "手动排班创建数据", produces = "application/json")
	@ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "path", dataType = "integer")
	@ResponseBody
	@PostMapping("/manualtemplate/{suiteId}")
	public ResponseBundle manualtemplate(@PathVariable("suiteId") Integer suiteId) {
		try {

			DutySuite dutySuite = Repositories.dutySuiteRepository.findOne(suiteId);
			List<User> userlist = Repositories.userRepository.findByDistrictIdAndBackup(dutySuite.getDistrictId(), 1);
			List<ScheduleUser> scheduleUsers = new ArrayList<ScheduleUser>();
			ScheduleUser scheduleUser;
			for (int i = 0; i < userlist.size(); i++) {
				scheduleUser = new ScheduleUser();
				scheduleUser.setDistrictId(dutySuite.getDistrictId());
				scheduleUser.setPositionId(dutySuite.getPositionId());
				scheduleUser.setStationId(dutySuite.getStationId());
				scheduleUser.setSuiteId(suiteId);
				scheduleUser.setWeekNum(i + 1);
				scheduleUser.setUserId(userlist.get(i).getId());
				scheduleUsers.add(scheduleUser);
			}
			Repositories.scheduleUserRepository.save(scheduleUsers);
			Repositories.scheduleUserRepository.flush();
			return new ResponseBundle().success(scheduleUsers);
		} catch (Exception e) {
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}

	/**
	 * 手动排班设置班次
	 */
	@ApiOperation(value = "手动排班设置班次", produces = "application/json")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "query", dataType = "integer"),
			@ApiImplicitParam(name = "classId", value = "班次id", required = true, paramType = "query", dataType = "integer"),
			@ApiImplicitParam(name = "weekNum", value = "被设置的周数", required = true, paramType = "query", dataType = "integer"),
			@ApiImplicitParam(name = "dayNum", value = "被设置的周天数", required = true, paramType = "query", dataType = "integer"), })
	@ResponseBody
	@PutMapping("/settemplateclass")
	public ResponseBundle settemplateclass(@RequestParam("suiteId") Integer suiteId,
			@RequestParam("classId") Integer classId, @RequestParam("weekNum") Integer weekNum,
			@RequestParam("dayNum") Integer dayNum) {
		try {
			ScheduleTemplate scheduleTemplate = new ScheduleTemplate();
			DutySuite dutySuite = Repositories.dutySuiteRepository.findOne(suiteId);
			DutyClass dutyClass = Repositories.dutyClassRepository.findOne(classId);
			scheduleTemplate.setDistrictId(dutySuite.getDistrictId());
			scheduleTemplate.setSuiteId(suiteId);
			scheduleTemplate.setWeekNum(weekNum);
			scheduleTemplate.setDayNum(dayNum);
			scheduleTemplate.setClassId(classId);
			scheduleTemplate.setCellColor(dutyClass.getClassColor());
			scheduleTemplate.setWorkingLength(dutyClass.getWorkingLength());
			scheduleTemplate.setDutyName(dutyClass.getDutyName());
			scheduleTemplate.setDutyCode(dutyClass.getDutyCode());
			Repositories.scheduleTemplateRepository.saveAndFlush(scheduleTemplate);
			return new ResponseBundle().success(scheduleTemplate);
		} catch (Exception e) {
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
}
