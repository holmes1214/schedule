package com.evtape.schedule.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.evtape.schedule.consts.ResponseMeta;
import com.evtape.schedule.domain.User;
import com.evtape.schedule.domain.vo.ResponseBundle;
import com.evtape.schedule.persistent.Repositories;

/**
 * @author ripper 用戶列表
 */
@Controller
@RequestMapping("/user")
public class UserController {

	@ResponseBody
	@RequestMapping(value = "/list", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseBundle userList(@RequestParam("districtId") Integer districtId,
			@RequestParam("stationId") Integer stationId, @RequestParam("positionId") Integer positionId) {
		try {
			return new ResponseBundle().success(Repositories.userRepository
					.findByDistrictIdAndStationIdAndPositionId(districtId, stationId, positionId));
		} catch (Exception e) {
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/updateuser", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseBundle updateuser(@RequestBody User user) {

		try {
			Repositories.userRepository.saveAndFlush(user);
			return new ResponseBundle().success(user);
		} catch (Exception e) {
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/deleteuser", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseBundle deleteuser(@RequestParam("userId") Integer userId) {
		try {
			Repositories.userRepository.delete(userId);
			return new ResponseBundle().success(null);
		} catch (Exception e) {
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/backuplist", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseBundle backuplist(@RequestParam("districtId") Integer districtId) {
		try {
			return new ResponseBundle().success(Repositories.userRepository.findByDistrictIdAndBackup(districtId, 1));
		} catch (Exception e) {
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}

}
