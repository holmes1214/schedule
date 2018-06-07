package com.evtape.schedule.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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

/**
 * 排班
 */
@Controller
@RequestMapping("/schedule")
public class ScheduleController {

	@Autowired
	private ScheduleTemplateService scheduleTemplateService;

	/**
	 * 创建ScheduleTemplate,删除旧的，并入库
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/createtemplate", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseBundle createtemplate(@RequestParam("suiteId") Integer suiteId) {
		try {
			if (Repositories.dutySuiteRepository.exists(suiteId)) {
				return new ResponseBundle().success(scheduleTemplateService.removeAndSaveTemplates(suiteId));
			}
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		} catch (Exception e) {
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}

	/**
	 * 加载排班模板
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/templatelist", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseBundle templatelist(@RequestParam("suiteId") Integer suiteId) {
		try {
			return returntemplete(suiteId);
		} catch (Exception e) {
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}

	/**
	 * 排班模板交换任务
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/exchangeTemplate", method = { RequestMethod.POST, RequestMethod.GET })
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

	/**
	 * 排班模板删除一周
	 */
	@ResponseBody
	@RequestMapping(value = "/deleteoneweek", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseBundle deleteoneweek(@RequestParam("suiteId") Integer suiteId,
			@RequestParam("weekNum") Integer weekNum) {
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
	 * 排班模板设置人员
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/setscheduleuser", method = { RequestMethod.POST, RequestMethod.GET })
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
	@ResponseBody
	@RequestMapping(value = "/removescheduleuser", method = { RequestMethod.POST, RequestMethod.GET })
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
	@ResponseBody
	@RequestMapping(value = "/createscheduleinfo", method = { RequestMethod.POST, RequestMethod.GET })
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
	@ResponseBody
	@RequestMapping(value = "/getscheduleinfo", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseBundle getscheduleinfo(@RequestParam("suiteId") Integer suiteId) {
		try {
			List<ScheduleInfo> scheduleInfos = Repositories.scheduleInfoRepository.findBySuiteId(suiteId);
			return new ResponseBundle().success(scheduleInfos);
		} catch (Exception e) {
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}

	/**
	 * 手动排班
	 */
	@ResponseBody
	@RequestMapping(value = "/manualtemplate", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseBundle manualtemplate(@RequestParam("suiteId") Integer suiteId) {
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
	 * 手动排班
	 */
	@ResponseBody
	@RequestMapping(value = "/settemplateclass", method = { RequestMethod.POST, RequestMethod.GET })
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
