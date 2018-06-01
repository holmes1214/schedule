package com.evtape.schedule.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.evtape.schedule.consts.ResultCode;
import com.evtape.schedule.consts.ResultMap;
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
	public ResultMap createtemplate(@RequestParam("suiteId") Integer suiteId) {
		ResultMap resultMap;
		try {
			if (Repositories.dutySuiteRepository.exists(suiteId)) {
				resultMap = new ResultMap(ResultCode.SUCCESS);
				resultMap.setData(scheduleTemplateService.removeAndSaveTemplates(suiteId));
			} else {
				resultMap = new ResultMap(ResultCode.NOTEXISTS);
			}
		} catch (Exception e) {
			resultMap = new ResultMap(ResultCode.SERVER_ERROR);
		}
		return resultMap;
	}

}
