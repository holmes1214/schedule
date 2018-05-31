package com.evtape.schedule.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.evtape.schedule.consts.ResponseMeta;
import com.evtape.schedule.domain.DutyClass;
import com.evtape.schedule.domain.DutyPeriodChecking;
import com.evtape.schedule.domain.DutySuite;
import com.evtape.schedule.domain.vo.ResponseBundle;
import com.evtape.schedule.persistent.Repositories;


/**
 * TODO 权限没加，最后一块儿加，现在加上权限调接口费劲
 * 
 * @author ripper duty列表
 */
@Controller
@RequestMapping("/duty")
public class DutyController {

	/**
	 * 班制列表,普通班制或备班班制
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/suitelist", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseBundle suitlist(@RequestParam("districtId") Integer districtId,
			@RequestParam("stationId") Integer stationId, @RequestParam("positionId") Integer positionId,
			@RequestParam("backup") Integer backup) {
		// 备班班制，backup传1
		try {
			if (backup != null && backup == 1) {
				return new ResponseBundle()
						.success(Repositories.dutySuiteRepository.findByDistrictIdAndBackup(districtId, backup));
			}
			return new ResponseBundle().success(Repositories.dutySuiteRepository
					.findByDistrictIdAndStationIdAndPositionId(districtId, stationId, positionId));
		} catch (Exception e) {
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}

	/**
	 * 获取班次和班制及检查条件查询
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getsuite", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseBundle getsuite(@RequestParam("suiteId") Integer suiteId) {
		try {
			return new ResponseBundle().success(selectSuiteInfo(suiteId));
		} catch (Exception e) {
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}
	/**
	 * 新增suite
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/suiteadd", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseBundle suitadd(@RequestBody DutySuite dutySuite) {
		try {
			Repositories.dutySuiteRepository.saveAndFlush(dutySuite);
			return new ResponseBundle().success(selectSuiteInfo(dutySuite.getId()));
		} catch (Exception e) {
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}
	
	/**
	 * 激活某个suite，同时把同岗位的其他suite置成未激活，激活的suite前端可以特殊展示
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/suiteactive", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseBundle suitactive(@RequestParam("districtId") Integer districtId,
			@RequestParam("stationId") Integer stationId, @RequestParam("positionId") Integer positionId,
			@RequestParam("suiteId") Integer suiteId) {
		try {
			List<DutySuite> suitlist = Repositories.dutySuiteRepository
					.findByDistrictIdAndStationIdAndPositionId(districtId, stationId, positionId);
			for (DutySuite dutySuite : suitlist) {
				if (dutySuite.getId().equals(suiteId)) {
					dutySuite.setIsActive("1");
				} else {
					dutySuite.setIsActive("0");
				}
			}
			Repositories.dutySuiteRepository.save(suitlist);
			Repositories.dutySuiteRepository.flush();
			return new ResponseBundle().success(suitlist);
		} catch (Exception e) {
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}

	/**
	 * 班次增改
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/classupdate", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseBundle classupdate(@RequestBody DutyClass dutyClass) {
		try {
			Integer suiteId = dutyClass.getSuiteId();
			Repositories.dutyClassRepository.saveAndFlush(dutyClass);
			return new ResponseBundle().success(selectSuiteInfo(suiteId));
		} catch (Exception e) {
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}
	/**
	 * 班次删
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/classdelete", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseBundle classdelete(@RequestBody DutyClass dutyClass) {
		try {
			Integer suiteId = dutyClass.getSuiteId();
			Repositories.dutyClassRepository.delete(dutyClass.getId());
			Repositories.dutyClassRepository.flush();
			return new ResponseBundle().success(selectSuiteInfo(suiteId));
		} catch (Exception e) {
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}
	/**
	 * 检查条件增改
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/periodupdate", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseBundle periodupdate(@RequestBody DutyPeriodChecking dutyPeriodChecking) {
		try {
			Integer suiteId = dutyPeriodChecking.getSuiteId();
			Repositories.dutyPeriodCheckingRepository.saveAndFlush(dutyPeriodChecking);
			return new ResponseBundle().success(selectSuiteInfo(suiteId));
		} catch (Exception e) {
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}
	/**
	 * 检查条件删
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/perioddelete", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseBundle perioddelete(@RequestBody DutyPeriodChecking dutyPeriodChecking) {
		try {
			Integer suiteId = dutyPeriodChecking.getSuiteId();
			Repositories.dutyPeriodCheckingRepository.delete(dutyPeriodChecking.getId());
			Repositories.dutyPeriodCheckingRepository.flush();
			return new ResponseBundle().success(selectSuiteInfo(suiteId));
		} catch (Exception e) {
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}


	/**
	 * 删除班制，返回班制列表
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/suitedelete", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseBundle suitdelete(@RequestBody DutySuite dutySuite) {
		try {
			Integer suiteId = dutySuite.getId();
			DutySuite dutySuite1 = Repositories.dutySuiteRepository.findOne(suiteId);
			if (dutySuite1 == null || StringUtils.equals(dutySuite1.getIsActive(), "1")) {
				return new ResponseBundle().failure(ResponseMeta.SUITE_ISACTIVE);
			}
			List<DutyClass> dutyClasslist = Repositories.dutyClassRepository.findBySuiteId(suiteId);
			for (DutyClass dutyClass : dutyClasslist) {
				Repositories.dutyClassRepository.delete(dutyClass.getId());
			}
			Repositories.dutyClassRepository.flush();
			List<DutyPeriodChecking> periodCheckinglist = Repositories.dutyPeriodCheckingRepository
					.findBySuiteId(suiteId);
			for (DutyPeriodChecking dutyPeriodChecking : periodCheckinglist) {
				Repositories.dutyPeriodCheckingRepository.delete(dutyPeriodChecking.getId());
			}
			Repositories.dutyPeriodCheckingRepository.flush();
			Repositories.dutySuiteRepository.delete(suiteId);
			Repositories.dutySuiteRepository.flush();
			if (dutySuite1.getBackup() != null && dutySuite1.getBackup() == 1) {
				return new ResponseBundle().success(Repositories.dutySuiteRepository
						.findByDistrictIdAndBackup(dutySuite1.getDistrictId(), dutySuite1.getBackup()));
			}
			return new ResponseBundle()
					.success(Repositories.dutySuiteRepository.findByDistrictIdAndStationIdAndPositionId(
							dutySuite1.getDistrictId(), dutySuite1.getStationId(), dutySuite1.getPositionId()));
		} catch (Exception e) {
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}

	private Map<String, Object> selectSuiteInfo(Integer suiteId) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("dutysuite", Repositories.dutySuiteRepository.findOne(suiteId));
		map.put("dutyclass", Repositories.dutyClassRepository.findBySuiteId(suiteId));
		map.put("dutyperiodchecking", Repositories.dutyPeriodCheckingRepository.findBySuiteId(suiteId));
		return map;
	}
}
