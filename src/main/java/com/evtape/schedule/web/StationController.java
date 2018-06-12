package com.evtape.schedule.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.evtape.schedule.consts.ResponseMeta;
import com.evtape.schedule.domain.Station;
import com.evtape.schedule.domain.vo.ResponseBundle;
import com.evtape.schedule.persistent.Repositories;

/**
 * @author ripper 站列表接口,增刪改查
 */
@Controller
@RequestMapping("/station")
public class StationController {

	/**
	 * Station 列表
	 */
	@ResponseBody
	@RequestMapping(value = "/list", method = { RequestMethod.GET })
	public ResponseBundle stationList(@RequestParam("districtId") Integer districtId) {
		try {
			return new ResponseBundle().success(Repositories.stationRepository.findByDistrictId(districtId));
		} catch (Exception e) {
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/add", method = { RequestMethod.POST })
	public ResponseBundle addStation(@RequestBody Station station) {
		try {
			Repositories.stationRepository.saveAndFlush(station);
			return new ResponseBundle()
					.success(Repositories.stationRepository.findByDistrictId(station.getDistrictId()));
		} catch (Exception e) {
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/update", method = { RequestMethod.PUT })
	public ResponseBundle updateStation(@RequestBody Station station) {
		try {
			Repositories.stationRepository.saveAndFlush(station);
			return new ResponseBundle()
					.success(Repositories.stationRepository.findByDistrictId(station.getDistrictId()));
		} catch (Exception e) {
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/delete", method = { RequestMethod.DELETE })
	public ResponseBundle deleteStation(@RequestBody Station station) {
		try {
			Repositories.stationRepository.delete(station.getId());
			return new ResponseBundle()
					.success(Repositories.stationRepository.findByDistrictId(station.getDistrictId()));
		} catch (Exception e) {
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}

}
