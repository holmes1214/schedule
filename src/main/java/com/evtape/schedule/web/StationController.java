package com.evtape.schedule.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.evtape.schedule.consts.ResultCode;
import com.evtape.schedule.consts.ResultMap;
import com.evtape.schedule.domain.Station;
import com.evtape.schedule.persistent.Repositories;

/**
 * @author ripper 站列表接口,增刪改查
 */
@Controller
@RequestMapping("/station")
public class StationController {

	@ResponseBody
	@RequestMapping(value = "/list", method = { RequestMethod.POST, RequestMethod.GET })
	public ResultMap stationList(@RequestParam("districtId") Integer districtId) {
		ResultMap resultMap;
		try {
			resultMap = new ResultMap(ResultCode.SUCCESS);
			resultMap.setData(Repositories.stationRepository.findByDistrictId(districtId));
		} catch (Exception e) {
			resultMap = new ResultMap(ResultCode.SERVER_ERROR);
		}
		return resultMap;
	}

//	@ResponseBody
//	@RequestMapping(value = "/allStation", method = { RequestMethod.POST, RequestMethod.GET })
//	public ResultMap allStation() {
//		ResultMap resultMap;
//		try {
//			resultMap = new ResultMap(ResultCode.SUCCESS);
//			Repositories.stationRepository.findAll();
//		} catch (Exception e) {
//			resultMap = new ResultMap(ResultCode.SERVER_ERROR);
//		}
//		return resultMap;
//	}

	@ResponseBody
	@RequestMapping(value = "/addStation", method = { RequestMethod.POST, RequestMethod.GET })
	public ResultMap addStation(@RequestParam("stationName") String stationName,
			@RequestParam("districtId") Integer districtId) {

		ResultMap resultMap;
		Station station = new Station();
		try {
			resultMap = new ResultMap(ResultCode.SUCCESS);
			station.setDistrictId(districtId);
			station.setStationName(stationName);
			Repositories.stationRepository.saveAndFlush(station);
		} catch (Exception e) {
			resultMap = new ResultMap(ResultCode.SERVER_ERROR);
		}
		return resultMap;
	}

	@ResponseBody
	@RequestMapping(value = "/updateStation", method = { RequestMethod.POST, RequestMethod.GET })
	public ResultMap updateStation(@RequestParam("stationName") String stationName,
			@RequestParam("districtId") Integer districtId, @RequestParam("id") Integer id) {
		ResultMap resultMap;
		Station station = new Station();
		try {
			resultMap = new ResultMap(ResultCode.SUCCESS);
			station.setId(id);
			station.setDistrictId(districtId);
			station.setStationName(stationName);
			Repositories.stationRepository.saveAndFlush(station);
		} catch (Exception e) {
			resultMap = new ResultMap(ResultCode.SERVER_ERROR);
		}
		return resultMap;
	}

//	@ResponseBody
//	@RequestMapping(value = "/deleteStation", method = { RequestMethod.POST, RequestMethod.GET })
//	public ResultMap deleteStation(@RequestParam("id") Integer id) {
//		ResultMap resultMap;
//		try {
//			resultMap = new ResultMap(ResultCode.SUCCESS);
//			Repositories.stationRepository.delete(id);
//		} catch (Exception e) {
//			resultMap = new ResultMap(ResultCode.SERVER_ERROR);
//		}
//		return resultMap;
//	}
}
