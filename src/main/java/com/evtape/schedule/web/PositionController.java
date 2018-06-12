package com.evtape.schedule.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.evtape.schedule.consts.ResponseMeta;
import com.evtape.schedule.domain.Position;
import com.evtape.schedule.domain.vo.ResponseBundle;
import com.evtape.schedule.persistent.Repositories;

/**
 * @author ripper 站下岗位,增刪改查
 */
@Controller
@RequestMapping("/position")
public class PositionController {

	/**
	 * Position 列表
	 */
	@ResponseBody
	@RequestMapping(value = "/list", method = { RequestMethod.GET })
	public ResponseBundle positionList(@RequestParam("stationId") Integer stationId) {
		try {
			return new ResponseBundle().success(Repositories.positionRepository.findByStationId(stationId));
		} catch (Exception e) {
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}

	/**
	 * Position 增改
	 */
	@ResponseBody
	@RequestMapping(value = "/add", method = { RequestMethod.POST })
	public ResponseBundle addPosition(@RequestBody Position position) {
		try {
			Repositories.positionRepository.saveAndFlush(position);
			return new ResponseBundle()
					.success(Repositories.positionRepository.findByStationId(position.getStationId()));
		} catch (Exception e) {
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}

	/**
	 * Position 增改
	 */
	@ResponseBody
	@RequestMapping(value = "/update", method = { RequestMethod.PUT })
	public ResponseBundle updatePosition(@RequestBody Position position) {
		try {
			Repositories.positionRepository.saveAndFlush(position);
			return new ResponseBundle()
					.success(Repositories.positionRepository.findByStationId(position.getStationId()));
		} catch (Exception e) {
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}

	/**
	 * Position 删
	 */
	@ResponseBody
	@RequestMapping(value = "/delete", method = { RequestMethod.DELETE })
	public ResponseBundle deletePosition(@RequestBody Position position) {
		try {
			Repositories.positionRepository.delete(position.getId());
			return new ResponseBundle()
					.success(Repositories.positionRepository.findByStationId(position.getStationId()));
		} catch (Exception e) {
			return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
		}
	}
}
