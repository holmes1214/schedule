package com.evtape.schedule.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.evtape.schedule.consts.ResultCode;
import com.evtape.schedule.consts.ResultMap;
import com.evtape.schedule.domain.User;
import com.evtape.schedule.persistent.Repositories;

/**
 * @author ripper 用戶列表
 */
@Controller
@RequestMapping("/user")
public class UserController {

	@ResponseBody
	@RequestMapping(value = "/list", method = { RequestMethod.POST, RequestMethod.GET })
	public ResultMap userList(@RequestParam("districtId") Integer districtId,
			@RequestParam("stationId") Integer stationId, @RequestParam("positionId") Integer positionId) {
		ResultMap resultMap;
		try {
			resultMap = new ResultMap(ResultCode.SUCCESS);
			resultMap.setData(Repositories.userRepository.findByDistrictIdAndStationIdAndPositionId(districtId,
					stationId, positionId));
		} catch (Exception e) {
			resultMap = new ResultMap(ResultCode.SERVER_ERROR);
		}
		return resultMap;
	}

	@ResponseBody
	@RequestMapping(value = "/addUser", method = { RequestMethod.POST, RequestMethod.GET })
	public ResultMap addUser(
			@RequestParam("userName") String userName, @RequestParam("districtId") Integer districtId,
			@RequestParam("stationId") Integer stationId) {

		ResultMap resultMap;
		User user = new User();
		try {
			resultMap = new ResultMap(ResultCode.SUCCESS);
			user.setDistrictId(districtId);
			user.setUserName(userName);
			user.setStationId(stationId);
			Repositories.userRepository.saveAndFlush(user);
		} catch (Exception e) {
			resultMap = new ResultMap(ResultCode.SERVER_ERROR);
		}
		return resultMap;
	}

	@ResponseBody
	@RequestMapping(value = "/updateUser", method = { RequestMethod.POST, RequestMethod.GET })
	public ResultMap updateUser(@RequestParam("userName") String userName,
			@RequestParam("districtId") Integer districtId, @RequestParam("stationId") Integer stationId,
			@RequestParam("id") Integer id) {
		ResultMap resultMap;
		User user = new User();
		try {
			resultMap = new ResultMap(ResultCode.SUCCESS);
			user.setId(id);
			user.setDistrictId(districtId);
			user.setUserName(userName);
			user.setStationId(stationId);
			Repositories.userRepository.saveAndFlush(user);
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
			Repositories.userRepository.delete(id);
		} catch (Exception e) {
			resultMap = new ResultMap(ResultCode.SERVER_ERROR);
		}
		return resultMap;
	}
}
