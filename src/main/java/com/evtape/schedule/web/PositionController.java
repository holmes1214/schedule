package com.evtape.schedule.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.evtape.schedule.consts.ResultCode;
import com.evtape.schedule.consts.ResultMap;
import com.evtape.schedule.domain.Position;
import com.evtape.schedule.persistent.Repositories;

/**
 * @author ripper 站下岗位,增刪改查
 */
@Controller
@RequestMapping("/position")
public class PositionController {

	@ResponseBody
	@RequestMapping(value = "/list", method = { RequestMethod.POST, RequestMethod.GET })
	public ResultMap positionList(@RequestParam("stationId") Integer stationId) {
		ResultMap resultMap;
		try {
			resultMap = new ResultMap(ResultCode.SUCCESS);
			resultMap.setData(Repositories.positionRepository.findByStationId(stationId));
		} catch (Exception e) {
			resultMap = new ResultMap(ResultCode.SERVER_ERROR);
		}
		return resultMap;
	}

	@ResponseBody
	@RequestMapping(value = "/addPosition", method = { RequestMethod.POST, RequestMethod.GET })
	public ResultMap addPosition(@RequestParam("positionName") String positionName,
			@RequestParam("districtId") Integer districtId, @RequestParam("stationId") Integer stationId) {

		ResultMap resultMap;
		Position position = new Position();
		try {
			resultMap = new ResultMap(ResultCode.SUCCESS);
			position.setDistrictId(districtId);
			position.setPositionName(positionName);
			position.setStationId(stationId);
			Repositories.positionRepository.saveAndFlush(position);
		} catch (Exception e) {
			resultMap = new ResultMap(ResultCode.SERVER_ERROR);
		}
		return resultMap;
	}

	@ResponseBody
	@RequestMapping(value = "/updatePosition", method = { RequestMethod.POST, RequestMethod.GET })
	public ResultMap updatePosition(@RequestParam("positionName") String positionName,
			@RequestParam("districtId") Integer districtId, @RequestParam("stationId") Integer stationId,
			@RequestParam("id") Integer id, @RequestParam("backupPosition") Integer backupPosition) {
		ResultMap resultMap;
		Position position = new Position();
		try {
			resultMap = new ResultMap(ResultCode.SUCCESS);
			position.setId(id);
			position.setDistrictId(districtId);
			position.setPositionName(positionName);
			position.setStationId(stationId);
			position.setBackupPosition(backupPosition);
			Repositories.positionRepository.saveAndFlush(position);
		} catch (Exception e) {
			resultMap = new ResultMap(ResultCode.SERVER_ERROR);
		}
		return resultMap;
	}

	@ResponseBody
	@RequestMapping(value = "/deletePosition", method = { RequestMethod.POST, RequestMethod.GET })
	public ResultMap deletePosition(@RequestParam("id") Integer id) {
		ResultMap resultMap;
		try {
			resultMap = new ResultMap(ResultCode.SUCCESS);
			Repositories.positionRepository.delete(id);
		} catch (Exception e) {
			resultMap = new ResultMap(ResultCode.SERVER_ERROR);
		}
		return resultMap;
	}
}
