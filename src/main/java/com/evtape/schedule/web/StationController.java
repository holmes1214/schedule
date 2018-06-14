package com.evtape.schedule.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.evtape.schedule.consts.ResponseMeta;
import com.evtape.schedule.domain.Station;
import com.evtape.schedule.domain.vo.ResponseBundle;
import com.evtape.schedule.persistent.Repositories;

/**
 * @author ripper 站列表接口,增刪改查
 */
@Api(value = "站点接口")
@Controller
@RequestMapping("/station")
public class StationController {

    @ApiOperation(value = "获取站点列表", produces = "application/json")
    @ApiImplicitParam(name = "districtId", value = "站区id", required = true, paramType = "query",
            dataType = "int")
    @ResponseBody
    @GetMapping
    public ResponseBundle stationList(@RequestParam("districtId") Integer districtId) {
        try {
            return new ResponseBundle().success(Repositories.stationRepository.findByDistrictId(districtId));
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    @ApiOperation(value = "新增站区", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "stationName", value = "站点名", required = true, paramType = "body",
                    dataType = "string"),
            @ApiImplicitParam(name = "districtId", value = "站点所在站区id", required = true, paramType = "body",
                    dataType = "integer"),
    })
    @ResponseBody
    @PostMapping
    public ResponseBundle addStation(@RequestBody Station station) {
        try {
            Repositories.stationRepository.saveAndFlush(station);
            return new ResponseBundle()
                    .success(Repositories.stationRepository.findByDistrictId(station.getDistrictId()));
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    @ApiOperation(value = " 更新站区", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "站点id", required = true, paramType = "path",
                    dataType = "int"),
            @ApiImplicitParam(name = "stationName", value = "站点名", required = true, paramType = "body",
                    dataType = "string"),
            @ApiImplicitParam(name = "districtId", value = "站点所在站区id", required = true, paramType = "body",
                    dataType = "integer")
    })
    @ResponseBody
    @PutMapping("/{id}")
    public ResponseBundle updateStation(@PathVariable("id") Integer id, @RequestBody Station form) {
        try {
            Station station = Repositories.stationRepository.findOne(id);
            station.setDistrictId(form.getDistrictId());
            station.setStationName(form.getStationName());
            Repositories.stationRepository.saveAndFlush(station);
            return new ResponseBundle()
                    .success(Repositories.stationRepository.findByDistrictId(form.getDistrictId()));
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    @ApiOperation(value = "根据id删除站点", produces = "application/json")
    @ApiImplicitParam(name = "id", value = "站区id", required = true, paramType = "path", dataType
            = "int")
    @ResponseBody
    @DeleteMapping("/{id}")
    public ResponseBundle deleteStation(@PathVariable("id") Integer id) {
        try {
            Station station = Repositories.stationRepository.findOne(id);
            Repositories.stationRepository.delete(id);
            return new ResponseBundle()
                    .success(Repositories.stationRepository.findByDistrictId(station.getDistrictId()));
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

}
