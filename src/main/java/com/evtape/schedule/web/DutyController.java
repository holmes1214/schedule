package com.evtape.schedule.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.evtape.schedule.consts.ResultCode;
import com.evtape.schedule.consts.ResultMap;
import com.evtape.schedule.domain.DutyClass;
import com.evtape.schedule.domain.DutySuite;
import com.evtape.schedule.entity.DutyEntity;
import com.evtape.schedule.persistent.Repositories;

/**
 * @author ripper 用戶列表
 */
@Controller
@RequestMapping("/duty")
public class DutyController {

	@ResponseBody
	@RequestMapping(value = "/suitlist", method = { RequestMethod.POST, RequestMethod.GET })
	public ResultMap suitlist(@RequestParam("districtId") Integer districtId,
			@RequestParam("stationId") Integer stationId, @RequestParam("positionId") Integer positionId) {
		ResultMap resultMap;
		try {
			resultMap = new ResultMap(ResultCode.SUCCESS);
			resultMap.setData(Repositories.dutySuiteRepository.findByDistrictIdAndStationIdAndPositionId(districtId,
					stationId, positionId));
		} catch (Exception e) {
			resultMap = new ResultMap(ResultCode.SERVER_ERROR);
		}
		return resultMap;
	}

	/**
	 * 班次和班制查询
	 * 
	 * @param districtId
	 * @param stationId
	 * @param positionId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/classlist", method = { RequestMethod.POST, RequestMethod.GET })
	public ResultMap classlist(@RequestParam("suitid") Integer suitid) {
		ResultMap resultMap;
		try {
			resultMap = new ResultMap(ResultCode.SUCCESS);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("dutysuite", Repositories.dutySuiteRepository.getOne(suitid));
			map.put("dutyclass", Repositories.dutyClassRepository.findBySuitId(suitid));
			resultMap.setData(map);
		} catch (Exception e) {
			resultMap = new ResultMap(ResultCode.SERVER_ERROR);
		}
		return resultMap;
	}

	@ResponseBody
	@RequestMapping(value = "/suitactive", method = { RequestMethod.POST, RequestMethod.GET })
	public ResultMap suitactive(@RequestParam("districtId") Integer districtId,
			@RequestParam("stationId") Integer stationId, @RequestParam("positionId") Integer positionId,
			@RequestParam("suitid") Integer suitid) {
		ResultMap resultMap;
		try {
			resultMap = new ResultMap(ResultCode.SUCCESS);
			List<DutySuite> suitlist = Repositories.dutySuiteRepository
					.findByDistrictIdAndStationIdAndPositionId(districtId, stationId, positionId);
			for (DutySuite dutySuite : suitlist) {
				if (dutySuite.getId().equals(suitid)) {
					dutySuite.setIsActive("1");
				} else {
					dutySuite.setIsActive("0");
				}
			}
			Repositories.dutySuiteRepository.save(suitlist);
		} catch (Exception e) {
			resultMap = new ResultMap(ResultCode.SERVER_ERROR);
		}
		return resultMap;
	}

	/**
	 * 班次更新,新增，更新，刪除
	 * 
	 * @param districtId
	 * @param stationId
	 * @param positionId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/classupdate", method = { RequestMethod.POST, RequestMethod.GET })
	public ResultMap classupdate(@RequestBody DutyEntity dutyEntity) {
		ResultMap resultMap;
		try {
			resultMap = new ResultMap(ResultCode.SUCCESS);
			Repositories.dutyClassRepository.save(dutyEntity.getUpdatelist());
			for (Integer integer : dutyEntity.getDeletelist()) {
				Repositories.dutyClassRepository.delete(integer);
			}
		} catch (Exception e) {
			resultMap = new ResultMap(ResultCode.SERVER_ERROR);
		}
		return resultMap;
	}

	/**
	 * 新增suit
	 * 
	 * @param districtId
	 * @param stationId
	 * @param positionId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/suitadd", method = { RequestMethod.POST, RequestMethod.GET })
	public ResultMap suitadd(@RequestBody DutySuite dutySuite) {
		ResultMap resultMap;
		try {
			resultMap = new ResultMap(ResultCode.SUCCESS);
			Repositories.dutySuiteRepository.saveAndFlush(dutySuite);
		} catch (Exception e) {
			resultMap = new ResultMap(ResultCode.SERVER_ERROR);
		}
		return resultMap;
	}

	/**
	 * 删除班次
	 * 
	 * @param districtId
	 * @param stationId
	 * @param positionId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/suitdelete", method = { RequestMethod.POST, RequestMethod.GET })
	public ResultMap suitdelete(@RequestParam("suitid") Integer suitid) {
		ResultMap resultMap;
		try {
			resultMap = new ResultMap(ResultCode.SUCCESS);
			Repositories.dutySuiteRepository.delete(suitid);
			List<DutyClass> dutylist = Repositories.dutyClassRepository.findBySuitId(suitid);
			for (DutyClass dutyClass : dutylist) {
				Repositories.dutyClassRepository.delete(dutyClass.getId());
			}
		} catch (Exception e) {
			resultMap = new ResultMap(ResultCode.SERVER_ERROR);
		}
		return resultMap;
	}
}
