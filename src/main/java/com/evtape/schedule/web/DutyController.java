package com.evtape.schedule.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.evtape.schedule.consts.ResponseMeta;
import com.evtape.schedule.domain.DutyClass;
import com.evtape.schedule.domain.DutyPeriodChecking;
import com.evtape.schedule.domain.DutySuite;
import com.evtape.schedule.domain.vo.ResponseBundle;
import com.evtape.schedule.persistent.Repositories;


/**
 * TODO 权限没加，最后一块儿加，现在加上权限调接口费劲
 *
 * @author ripper duty列表
 */
@Api(value = "班制接口")
@Controller
@RequestMapping("/duty")
public class DutyController {


    @ApiOperation(value = "获取班制列表", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "districtId", value = "站区id", required = true, paramType = "query",
                    dataType = "int"),
            @ApiImplicitParam(name = "stationId", value = "站点id", required = true, paramType = "query",
                    dataType = "int"),
            @ApiImplicitParam(name = "positionId", value = "岗位id", required = true, paramType = "query",
                    dataType = "int"),
            @ApiImplicitParam(name = "backup", value = "是否备班(1备班，0正常)", required = true, paramType = "query",
                    dataType = "int"),
    })
    @ResponseBody
    @GetMapping
    public ResponseBundle suitlist(@RequestParam("districtId") Integer districtId,
                                   @RequestParam("stationId") Integer stationId,
                                   @RequestParam("positionId") Integer positionId,
                                   @RequestParam("backup") Integer backup) {
        // 备班班制，backup传1
        try {
            if (backup != null && backup == 1) {
                return new ResponseBundle()
                        .success(Repositories.dutySuiteRepository.findByDistrictIdAndBackup(districtId, backup));
            }
            return new ResponseBundle().success(Repositories.dutySuiteRepository
                    .findByDistrictIdAndStationIdAndPositionId(districtId, stationId, positionId));
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    @ApiOperation(value = "根据班制id获取班次", produces = "application/json")
    @ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "query",
            dataType = "int")
    @ResponseBody
    @GetMapping("/suite")
    public ResponseBundle getsuite(@RequestParam("suiteId") Integer suiteId) {
        try {
            return new ResponseBundle().success(selectSuiteInfo(suiteId));
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    @ApiOperation(value = "新增班制", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "dutyName", value = "班制名", required = true, paramType = "body",
                    dataType = "string"),
            @ApiImplicitParam(name = "active", value = "是否启用(启动1, 没启用:0)", required = true, paramType = "body",
                    dataType = "integer"),
            @ApiImplicitParam(name = "districtId", value = "站区id", required = true, paramType = "body",
                    dataType = "integer"),
            @ApiImplicitParam(name = "stationId", value = "站id", required = true, paramType = "body",
                    dataType = "integer"),
            @ApiImplicitParam(name = "positionId", value = "岗位id", required = true, paramType = "body",
                    dataType = "integer"),
            @ApiImplicitParam(name = "maxWorkingHour", value = "每周最大工时", required = true, paramType = "body",
                    dataType = "integer"),
            @ApiImplicitParam(name = "minWorkingHour", value = "每周最小工时", required = true, paramType = "body",
                    dataType = "integer"),
            @ApiImplicitParam(name = "maxWeeklyRestDays", value = "每周最多休几天", required = true, paramType = "body",
                    dataType = "integer"),
            @ApiImplicitParam(name = "minWeeklyRestDays", value = "每周最少休几天", required = true, paramType = "body",
                    dataType = "integer"),
            @ApiImplicitParam(name = "monthlyWorkingHourLimit", value = "每月最大工时数", required = true, paramType = "body",
                    dataType = "integer"),
            @ApiImplicitParam(name = "yearlyWorkingHourLimit", value = "每年最大工时数", required = true, paramType = "body",
                    dataType = "integer"),
            @ApiImplicitParam(name = "backup", value = "是否有备班(1备班，0正常)", required = true, paramType = "body",
                    dataType = "integer")
    })
    @ResponseBody
    @PostMapping("/suite")
    public ResponseBundle suitadd(@RequestBody DutySuite dutySuite) {
        try {
            Repositories.dutySuiteRepository.saveAndFlush(dutySuite);
            return new ResponseBundle().success(selectSuiteInfo(dutySuite.getId()));
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    /**
     * 激活某个suite，同时把同岗位的其他suite置成未激活，激活的suite前端可以特殊展示
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/suiteactive", method = {RequestMethod.PUT})
    public ResponseBundle suitactive(@RequestParam("districtId") Integer districtId,
                                     @RequestParam("stationId") Integer stationId,
                                     @RequestParam("positionId")
                                             Integer positionId,
                                     @RequestParam("suiteId") Integer suiteId) {
        try {
            List<DutySuite> suitlist = Repositories.dutySuiteRepository
                    .findByDistrictIdAndStationIdAndPositionId(districtId, stationId, positionId);
            for (DutySuite dutySuite : suitlist) {
                if (dutySuite.getId().equals(suiteId)) {
                    dutySuite.setActive(1);
                } else {
                    dutySuite.setActive(0);
                }
            }
            Repositories.dutySuiteRepository.save(suitlist);
            Repositories.dutySuiteRepository.flush();
            return new ResponseBundle().success(suitlist);
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    @ApiOperation(value = "新增班次", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "dutyName", value = "班次名", required = true, paramType = "body",
                    dataType = "string"),
            @ApiImplicitParam(name = "dutyCode", value = "班次code", required = true, paramType = "body",
                    dataType = "string"),
            @ApiImplicitParam(name = "districtId", value = "站区id", required = true, paramType = "body",
                    dataType = "integer"),
            @ApiImplicitParam(name = "stationId", value = "站点id", required = true, paramType = "body",
                    dataType = "integer"),
            @ApiImplicitParam(name = "positionId", value = "岗位id", required = true, paramType = "body",
                    dataType = "integer"),
            @ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "body",
                    dataType = "integer"),
            @ApiImplicitParam(name = "userCount", value = "班次人数", required = true, paramType = "body",
                    dataType = "integer"),
            @ApiImplicitParam(name = "classColor", value = "班次颜色", required = true, paramType = "body",
                    dataType = "string"),
            @ApiImplicitParam(name = "startTime", value = "班次几点上班(从零点开始算，第多少分钟开始上班)", required = true,
                    paramType = "body",
                    dataType = "integer"),
            @ApiImplicitParam(name = "endTime", value = "班次几点下班(从零点开始算，第多少分钟下班)", required = true, paramType = "body",
                    dataType = "integer"),
            @ApiImplicitParam(name = "startTimeStr", value = "班次几点上班-小时", required = true, paramType = "body",
                    dataType = "string"),
            @ApiImplicitParam(name = "endTimeStr", value = "班次几点下班-小时", required = true, paramType = "body",
                    dataType = "string"),
            @ApiImplicitParam(name = "workingLength", value = "班次时长（分钟）", required = true, paramType = "body",
                    dataType = "integer"),
            @ApiImplicitParam(name = "restMinutes", value = "两班间隔（分钟）", required = true, paramType = "body",
                    dataType = "integer"),
            @ApiImplicitParam(name = "relevantClassId", value = "关联班次", required = true, paramType = "body",
                    dataType = "integer"),
            @ApiImplicitParam(name = "restMinutes", value = "两班间隔（分钟）", required = true, paramType = "body",
                    dataType = "integer"),
            @ApiImplicitParam(name = "backupPosition", value = "是否有备班(1备班，0正常)", required = true, paramType = "body",
                    dataType = "integer"),
    })
    @ResponseBody
    @PostMapping("/class")
    public ResponseBundle classadd(@RequestBody DutyClass dutyClass) {
        try {
            Integer suiteId = dutyClass.getSuiteId();
            Repositories.dutyClassRepository.saveAndFlush(dutyClass);
            return new ResponseBundle().success(selectSuiteInfo(suiteId));
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    /**
     * 班次改
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/classupdate", method = {RequestMethod.PUT})
    public ResponseBundle classupdate(@RequestBody DutyClass dutyClass) {
        try {
            Integer suiteId = dutyClass.getSuiteId();
            Repositories.dutyClassRepository.saveAndFlush(dutyClass);
            return new ResponseBundle().success(selectSuiteInfo(suiteId));
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    /**
     * 班次删
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/classdelete", method = {RequestMethod.DELETE})
    public ResponseBundle classdelete(@RequestBody DutyClass dutyClass) {
        try {
            Integer suiteId = dutyClass.getSuiteId();
            Repositories.dutyClassRepository.delete(dutyClass.getId());
            Repositories.dutyClassRepository.flush();
            return new ResponseBundle().success(selectSuiteInfo(suiteId));
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    /**
     * 检查条件增改
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/periodadd", method = {RequestMethod.POST})
    public ResponseBundle periodadd(@RequestBody DutyPeriodChecking dutyPeriodChecking) {
        try {
            Integer suiteId = dutyPeriodChecking.getSuiteId();
            Repositories.dutyPeriodCheckingRepository.saveAndFlush(dutyPeriodChecking);
            return new ResponseBundle().success(selectSuiteInfo(suiteId));
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    /**
     * 检查条件增改
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/periodupdate", method = {RequestMethod.PUT})
    public ResponseBundle periodupdate(@RequestBody DutyPeriodChecking dutyPeriodChecking) {
        try {
            Integer suiteId = dutyPeriodChecking.getSuiteId();
            Repositories.dutyPeriodCheckingRepository.saveAndFlush(dutyPeriodChecking);
            return new ResponseBundle().success(selectSuiteInfo(suiteId));
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    /**
     * 检查条件删
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/perioddelete", method = {RequestMethod.DELETE})
    public ResponseBundle perioddelete(@RequestBody DutyPeriodChecking dutyPeriodChecking) {
        try {
            Integer suiteId = dutyPeriodChecking.getSuiteId();
            Repositories.dutyPeriodCheckingRepository.delete(dutyPeriodChecking.getId());
            Repositories.dutyPeriodCheckingRepository.flush();
            return new ResponseBundle().success(selectSuiteInfo(suiteId));
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }


    /**
     * 删除班制，返回班制列表
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/suitedelete", method = {RequestMethod.DELETE})
    public ResponseBundle suitdelete(@RequestBody DutySuite dutySuite) {
        try {
            Integer suiteId = dutySuite.getId();
            DutySuite dutySuite1 = Repositories.dutySuiteRepository.findOne(suiteId);
            if (dutySuite1 == null || dutySuite1.getActive() == 1) {
                return new ResponseBundle().failure(ResponseMeta.SUITE_ISACTIVE);
            }
            List<DutyClass> dutyClasslist = Repositories.dutyClassRepository.findBySuiteId(suiteId);
            for (DutyClass dutyClass : dutyClasslist) {
                Repositories.dutyClassRepository.delete(dutyClass.getId());
            }
            Repositories.dutyClassRepository.flush();
            List<DutyPeriodChecking> periodCheckinglist = Repositories.dutyPeriodCheckingRepository
                    .findBySuiteId(suiteId);
            for (DutyPeriodChecking dutyPeriodChecking : periodCheckinglist) {
                Repositories.dutyPeriodCheckingRepository.delete(dutyPeriodChecking.getId());
            }
            Repositories.dutyPeriodCheckingRepository.flush();
            Repositories.dutySuiteRepository.delete(suiteId);
            Repositories.dutySuiteRepository.flush();
            if (dutySuite1.getBackup() != null && dutySuite1.getBackup() == 1) {
                return new ResponseBundle().success(Repositories.dutySuiteRepository
                        .findByDistrictIdAndBackup(dutySuite1.getDistrictId(), dutySuite1.getBackup()));
            }
            return new ResponseBundle()
                    .success(Repositories.dutySuiteRepository.findByDistrictIdAndStationIdAndPositionId(
                            dutySuite1.getDistrictId(), dutySuite1.getStationId(), dutySuite1.getPositionId()));
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    private Map<String, Object> selectSuiteInfo(Integer suiteId) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("dutysuite", Repositories.dutySuiteRepository.findOne(suiteId));
        map.put("dutyclass", Repositories.dutyClassRepository.findBySuiteId(suiteId));
        map.put("dutyperiodchecking", Repositories.dutyPeriodCheckingRepository.findBySuiteId(suiteId));
        return map;
    }
}
