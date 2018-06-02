package com.evtape.schedule.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.evtape.schedule.consts.ResponseMeta;
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
	 * templatelist
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/templatelist", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseBundle templatelist(@RequestParam("suiteId") Integer suiteId) {
		try {
			if (Repositories.dutySuiteRepository.exists(suiteId)) {
				return new ResponseBundle().success(scheduleTemplateService.removeAndSaveTemplates(suiteId));
			}
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		} catch (Exception e) {
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}
	
}
