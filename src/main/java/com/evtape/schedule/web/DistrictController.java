package com.evtape.schedule.web;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.evtape.schedule.consts.ResultCode;
import com.evtape.schedule.consts.ResultMap;
import com.evtape.schedule.domain.District;
import com.evtape.schedule.domain.Station;
import com.evtape.schedule.persistent.Repositories;

/**
 * @author ripper 站区接口,增刪改查
 */
@Controller
@RequestMapping("/district")
public class DistrictController {

	@ResponseBody
	@RequestMapping(value = "/list", method = { RequestMethod.POST, RequestMethod.GET })
	public ResultMap districtList() {
		ResultMap resultMap;
		try {
			resultMap = new ResultMap(ResultCode.SUCCESS);
			resultMap.setData(Repositories.districtRepository.findAll());
		} catch (Exception e) {
			resultMap = new ResultMap(ResultCode.SERVER_ERROR);
		}
		return resultMap;
	}

	@ResponseBody
	@RequestMapping(value = "/addDistrict", method = { RequestMethod.POST, RequestMethod.GET })
	public ResultMap addDistrict(@RequestParam("districtName") String districtName,
			@RequestParam("content") String content) {
		ResultMap resultMap;
		District district = new District();
		try {
			resultMap = new ResultMap(ResultCode.SUCCESS);
			district.setContent(content);
			district.setDistrictName(districtName);
			Repositories.districtRepository.saveAndFlush(district);
		} catch (Exception e) {
			resultMap = new ResultMap(ResultCode.SERVER_ERROR);
		}
		return resultMap;
	}

	@ResponseBody
	@RequestMapping(value = "/updateDistrict", method = { RequestMethod.POST, RequestMethod.GET })
	public ResultMap updateDistrict(@RequestParam("districtName") String districtName,
			@RequestParam("content") String content, @RequestParam("id") Integer id) {
		ResultMap resultMap;
		District district = new District();
		try {
			resultMap = new ResultMap(ResultCode.SUCCESS);
			district.setId(id);
			district.setContent(content);
			district.setDistrictName(districtName);
			Repositories.districtRepository.saveAndFlush(district);
		} catch (Exception e) {
			resultMap = new ResultMap(ResultCode.SERVER_ERROR);
		}
		return resultMap;
	}

	@ResponseBody
	@RequestMapping(value = "/deleteDistrict", method = { RequestMethod.POST, RequestMethod.GET })
	public ResultMap deleteDistrict(@RequestParam("id") Integer id) {
		ResultMap resultMap;
		try {
			resultMap = new ResultMap(ResultCode.SUCCESS);
			List<Station> list = Repositories.stationRepository.findByDistrictId(id);
			
			if (list.size() > 0) {
				resultMap.setData("当前站区下仍包含有站，不能删除此站区");
			} else if (Repositories.districtRepository.exists(id)) {
				Repositories.districtRepository.delete(id);
				resultMap.setData("删除成功");
			} else {
				resultMap.setData("数据不存在，不用删除");
			}
		} catch (Exception e) {
			resultMap = new ResultMap(ResultCode.SERVER_ERROR);
		}
		return resultMap;
	}
}
