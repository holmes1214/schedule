package com.evtape.schedule.web;

import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.evtape.schedule.consts.ResponseMeta;
import com.evtape.schedule.domain.District;
import com.evtape.schedule.domain.Station;
import com.evtape.schedule.domain.vo.ResponseBundle;
import com.evtape.schedule.persistent.Repositories;

/**
 * @author ripper 站区接口,增刪改查
 */
@Api(value = "站区接口")
@RestController
@RequestMapping("/district")
public class DistrictController {

    @ApiOperation(value = "获取站区列表", produces = "application/json")
    @GetMapping
    public ResponseBundle getDistricts() {
        return new ResponseBundle().success(Repositories.districtRepository.findAll());
    }


    @ApiOperation(value = "新增站区", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "districtName", value = "站区名", required = true, paramType = "body",
                    dataType = "string"),
            @ApiImplicitParam(name = "lineNumber", value = "线路号", required = true, paramType = "body",
                    dataType = "int"),
            @ApiImplicitParam(name = "content", value = "站区说明", required = true, paramType = "body",
                    dataType = "string"),
    })
    @PostMapping
    public ResponseBundle addDistrict(@RequestBody District district) {
        try {
            Repositories.districtRepository.saveAndFlush(district);
            return new ResponseBundle().success(Repositories.districtRepository.findAll());
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    @ApiOperation(value = "修改站区信息", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "站区id", required = true, paramType = "path",
                    dataType = "int"),
            @ApiImplicitParam(name = "districtName", value = "站区名", required = true, paramType = "body",
                    dataType = "string"),
            @ApiImplicitParam(name = "lineNumber", value = "线路号", required = true, paramType = "body",
                    dataType = "int"),
            @ApiImplicitParam(name = "content", value = "站区说明", required = true, paramType = "body",
                    dataType = "string"),
    })
    @PutMapping("/{id}")
    public ResponseBundle updateDistrict(@PathVariable("id") Integer id, @RequestBody District form) {
        try {
            District district = Repositories.districtRepository.findOne(id);
            district.setDistrictName(form.getDistrictName());
            district.setContent(form.getContent());
            Repositories.districtRepository.saveAndFlush(district);
            return new ResponseBundle().success(Repositories.districtRepository.findAll());
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }


    @ApiOperation(value = "根据id删除站区", produces = "application/json")
    @ApiImplicitParam(name = "id", value = "站区id", required = true, paramType = "path", dataType
            = "int")
    @DeleteMapping("/{id}")
    public ResponseBundle deleteDistrict(@PathVariable("id") Integer districtId) {
        try {
            List<Station> list = Repositories.stationRepository.findByDistrictId(districtId);
            if (list != null && list.size() > 0) {
                return new ResponseBundle().failure(ResponseMeta.DISTRICT_HASSTATION);
            }
            Repositories.districtRepository.delete(districtId);
            return new ResponseBundle().success(Repositories.districtRepository.findAll());
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }
}
