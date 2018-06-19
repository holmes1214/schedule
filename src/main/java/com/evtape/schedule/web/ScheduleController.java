package com.evtape.schedule.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
import com.evtape.schedule.domain.User;
import com.evtape.schedule.domain.vo.ResponseBundle;
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
				return new ResponseBundle().success(scheduleTemplateService.removeAndSaveTemplates(suiteId));
			}
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
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
	public ResponseBundle exchangeTemplate(@RequestParam("suiteId") Integer suiteId,
			@RequestParam("weekNum1") Integer weekNum1, @RequestParam("dayNum1") Integer dayNum1,
			@RequestParam("weekNum2") Integer weekNum2, @RequestParam("dayNum2") Integer dayNum2) {
		try {
			scheduleTemplateService.exchangeTemplate(suiteId, weekNum1, dayNum1, weekNum2, dayNum2);
			return returntemplete(suiteId);
		} catch (Exception e) {
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}

	private ResponseBundle returntemplete(Integer suiteId) {
		// TODO 页面刷新需不需要重查一遍数据库？
		Map<String, Object> result = new HashMap<String, Object>();
		List<ScheduleTemplate> templatelist = Repositories.scheduleTemplateRepository
				.findBySuiteIdOrderByOrderIndex(suiteId);
		List<ScheduleUser> scheduleUserlist = Repositories.scheduleUserRepository.findBySuiteIdOrderByWeekNum(suiteId);
		result.put("templatelist", templatelist);
		result.put("scheduleUserlist", scheduleUserlist);
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

	@ApiOperation(value = "排班模板设置人员", produces = "application/json")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "query", dataType = "integer"),
			@ApiImplicitParam(name = "weekNum", value = "被设置的周数", required = true, paramType = "query", dataType = "integer"),
			@ApiImplicitParam(name = "userId", value = "被设置人的id", required = true, paramType = "query", dataType = "integer"),})
	@ResponseBody
	@PutMapping("/setscheduleuser")
	public ResponseBundle setscheduleuser(@RequestParam("suiteId") Integer suiteId,
			@RequestParam("weekNum") Integer weekNum, @RequestParam("userId") Integer userId) {
		try {
			DutySuite dutySuite = Repositories.dutySuiteRepository.findOne(suiteId);
			ScheduleUser user = new ScheduleUser();
			user.setDistrictId(dutySuite.getDistrictId());
			user.setPositionId(dutySuite.getPositionId());
			user.setStationId(dutySuite.getStationId());
			user.setSuiteId(suiteId);
			user.setWeekNum(weekNum);
			user.setUserId(userId);
			Repositories.scheduleUserRepository.saveAndFlush(user);

			return new ResponseBundle().success(user);
		} catch (Exception e) {
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
			@ApiImplicitParam(name = "userId", value = "被取消设置人的id", required = true, paramType = "query", dataType = "integer"),})
	@ResponseBody
	@PutMapping(value = "/removescheduleuser")
	public ResponseBundle removescheduleuser(@RequestParam("suiteId") Integer suiteId,
			@RequestParam("weekNum") Integer weekNum, @RequestParam("userId") Integer userId) {
		try {
			ScheduleUser user = Repositories.scheduleUserRepository.findBySuiteIdAndWeekNum(suiteId, weekNum);
			Repositories.scheduleUserRepository.delete(user);
			return returntemplete(suiteId);
		} catch (Exception e) {
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
}
