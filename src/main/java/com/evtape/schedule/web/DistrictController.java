package com.evtape.schedule.web;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.evtape.schedule.consts.ResponseMeta;
import com.evtape.schedule.domain.District;
import com.evtape.schedule.domain.Station;
import com.evtape.schedule.domain.vo.ResponseBundle;
import com.evtape.schedule.persistent.Repositories;

/**
 * @author ripper 站区接口,增刪改查
 */
@Controller
@RequestMapping("/district")
public class DistrictController {
	/**
	 * district列表
	 */
	@ResponseBody
	@RequestMapping(value = "/list", method = { RequestMethod.GET })
	public ResponseBundle districtList() {
		return new ResponseBundle().success(Repositories.districtRepository.findAll());
	}

	/**
	 * district增
	 */
	@ResponseBody
	@RequestMapping(value = "/add", method = { RequestMethod.POST })
	public ResponseBundle addDistrict(@RequestBody District district) {
		try {
			Repositories.districtRepository.saveAndFlush(district);
			return new ResponseBundle().success(Repositories.districtRepository.findAll());
		} catch (Exception e) {
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}

	/**
	 * district改
	 */
	@ResponseBody
	@RequestMapping(value = "/update", method = { RequestMethod.PUT })
	public ResponseBundle updateDistrict(@RequestBody District district) {
		try {
			Repositories.districtRepository.saveAndFlush(district);
			return new ResponseBundle().success(Repositories.districtRepository.findAll());
		} catch (Exception e) {
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}

	/**
	 * district删
	 */
	@ResponseBody
	@RequestMapping(value = "/delete", method = { RequestMethod.DELETE })
	public ResponseBundle deleteDistrict(@RequestParam("districtId") Integer districtId) {
		try {

			List<Station> list = Repositories.stationRepository.findByDistrictId(districtId);
			if (list != null && list.size() > 0) {
				return new ResponseBundle().failure(ResponseMeta.DISTRICT_HASSTATION);
			}
			Repositories.districtRepository.delete(districtId);
			return new ResponseBundle().success(Repositories.districtRepository.findAll());
		} catch (Exception e) {
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}
}
