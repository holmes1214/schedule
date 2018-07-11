package com.evtape.schedule.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.evtape.schedule.consts.ResponseMeta;
import com.evtape.schedule.domain.Position;
import com.evtape.schedule.domain.vo.ResponseBundle;
import com.evtape.schedule.persistent.Repositories;

/**
 * @author ripper 站下岗位,增刪改查
 */
@Api(value = "岗位接口")
@Controller
@RequestMapping("/position")
public class PositionController {


    @ApiOperation(value = "根据站区id获取岗位列表", produces = "application/json")
    @ApiImplicitParam(name = "districtId", value = "站区id", required = true, paramType = "query",
            dataType = "int")
    @ResponseBody
    @GetMapping
    public ResponseBundle positionList(@RequestParam("districtId") Integer districtId) {
        try {
            return new ResponseBundle().success(Repositories.positionRepository.findByDistrictId(districtId));
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    @ApiOperation(value = "新增岗位", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "positionName", value = "岗位名", required = true, paramType = "body",
                    dataType = "string"),
            @ApiImplicitParam(name = "districtId", value = "所属站区id", required = true, paramType = "body",
                    dataType = "integer"),
            @ApiImplicitParam(name = "backupPosition", value = "是否有备班(1备班，0正常)", required = true, paramType = "body",
                    dataType = "integer"),
    })
    @ResponseBody
    @PostMapping
    public ResponseBundle addPosition(@RequestBody Position position) {
        try {
            Repositories.positionRepository.saveAndFlush(position);
            return new ResponseBundle()
                    .success(Repositories.positionRepository.findByDistrictId(position.getDistrictId()));
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }


    @ApiOperation(value = "更新岗位", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "岗位id", required = true, paramType = "path",
                    dataType = "int"),
            @ApiImplicitParam(name = "positionName", value = "岗位名", required = true, paramType = "body",
                    dataType = "string"),
            @ApiImplicitParam(name = "districtId", value = "所属站区id", required = true, paramType = "body",
                    dataType = "integer"),
            @ApiImplicitParam(name = "backupPosition", value = "是否有备班(1备班，0正常)", required = true, paramType = "body",
                    dataType = "integer"),
    })
    @ResponseBody
    @PutMapping("/{id}")
    public ResponseBundle updatePosition(@PathVariable("id") Integer id, @RequestBody Position form) {
        try {
            Position position = Repositories.positionRepository.findOne(id);
            position.setBackupPosition(form.getBackupPosition());
            position.setDistrictId(form.getDistrictId());
            position.setPositionName(form.getPositionName());
            Repositories.positionRepository.saveAndFlush(position);
            return new ResponseBundle()
                    .success(Repositories.positionRepository.findByDistrictId(form.getDistrictId()));
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    @ApiOperation(value = "根据id删除岗位", produces = "application/json")
    @ApiImplicitParam(name = "id", value = "岗位id", required = true, paramType = "path", dataType
            = "int")
    @ResponseBody
    @DeleteMapping("/{id}")
    public ResponseBundle deletePosition(@PathVariable("id") Integer positionId) {
        try {
            Position position = Repositories.positionRepository.findOne(positionId);
            Repositories.positionRepository.delete(positionId);
            return new ResponseBundle()
                    .success(Repositories.positionRepository.findByDistrictId(position.getDistrictId()));
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }
}
