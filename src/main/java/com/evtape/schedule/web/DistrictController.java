package com.evtape.schedule.web;

import java.util.ArrayList;
import java.util.List;

import com.evtape.schedule.domain.User;
import com.evtape.schedule.domain.form.DistrictManagerForm;
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
    	List<District> districts = Repositories.districtRepository.findAll();
        return new ResponseBundle().success(districts);
    }


    @ApiOperation(value = "新增站区", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "districtName", value = "站区名", required = true, paramType = "body",
                    dataType = "string"),
            @ApiImplicitParam(name = "lineNumber", value = "线路号", required = true, paramType = "body",
                    dataType = "String"),
            @ApiImplicitParam(name = "content", value = "站区说明", required = false, paramType = "body",
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
                    dataType = "String"),
            @ApiImplicitParam(name = "content", value = "站区说明", required = false, paramType = "body",
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

    @ApiOperation(value = "设置站区管理员", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "districtId", value = "站区id", required = true, paramType = "body",
                    dataType = "string"),
            @ApiImplicitParam(name = "userId", value = "管理员Id", required = true, paramType = "body",
                    dataType = "String"),
    })
    @PostMapping("/managers")
    public ResponseBundle setAdmin(@RequestBody DistrictManagerForm form) {
        try {
            District district = Repositories.districtRepository.findOne(form.getDistrictId());
            String[] userids=form.getUserId().split(",");
            List<User> managerList=new ArrayList<>();
            for(String uid:userids){
                int userId = Integer.parseInt(uid);
                User manager = Repositories.userRepository.findOne(userId);
                if (!manager.getDistrictId().equals(district.getId())){
                    return new ResponseBundle().failure(ResponseMeta.UN_UNIQUE_DISTRICT);
                }
                manager.setRoleId(2);
                managerList.add(manager);
            }
            Repositories.userRepository.save(managerList);
            List<User> list = Repositories.userRepository.findByDistrictIdAndRoleId(form.getDistrictId(), 2);
            district.setManagers(list);
            return new ResponseBundle().success(district);
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }
}
