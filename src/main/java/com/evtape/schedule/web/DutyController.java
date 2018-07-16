package com.evtape.schedule.web;

import com.evtape.schedule.consts.ResponseMeta;
import com.evtape.schedule.domain.*;
import com.evtape.schedule.domain.vo.ResponseBundle;
import com.evtape.schedule.persistent.Repositories;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * TODO 权限没加，最后一块儿加，现在加上权限调接口费劲
 *
 * @author ripper duty列表
 */
@Api(value = "班制接口")
@RestController
@RequestMapping("/duty")
public class DutyController {
    private static Logger logger = LoggerFactory.getLogger(DutyController.class);

    @ApiOperation(value = "获取班制列表", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "districtId", value = "站区id", required = true, paramType = "query",
                    dataType = "int"),
            @ApiImplicitParam(name = "stationId", value = "站点id", required = false, paramType = "query",
                    dataType = "int"),
            @ApiImplicitParam(name = "positionId", value = "岗位id", required = false, paramType = "query",
                    dataType = "int"),
            @ApiImplicitParam(name = "backup", value = "是否备班(1备班，0正常)", required = false, paramType = "query",
                    dataType = "int"),
    })
    @ResponseBody
    @GetMapping("/suite")
    public ResponseBundle suitList(@RequestParam("districtId") Integer districtId,
                                   @RequestParam(value = "stationId", required = false) Integer stationId,
                                   @RequestParam(value = "positionId", required = false) Integer positionId,
                                   @RequestParam(value = "backup", required = false) Integer backup) {
        // 备班班制，backup传1
        try {
            List<DutySuite> list = Repositories.dutySuiteRepository.findByDistrictId(districtId);
            if (backup != null) {
                list = list.stream().filter(d -> backup.equals(d.getBackup())).collect(Collectors.toList());
            }
            if (stationId != null) {
                list = list.stream().filter(d -> stationId.equals(d.getStationId())).collect(Collectors.toList());
            }
            if (positionId != null) {
                list = list.stream().filter(d -> positionId.equals(d.getPositionId())).collect(Collectors.toList());
            }
            return new ResponseBundle().success(list);
        } catch (Exception e) {
            logger.error("error:", e);
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    @ApiOperation(value = "根据班制id获取班次和检查条件", produces = "application/json")
    @ApiImplicitParam(name = "suiteId", value = "班制id", required = true,  paramType = "path", dataType = "int")
    @ResponseBody
    @GetMapping("/suite/{suiteId}")
    public ResponseBundle getSuite(@PathVariable("suiteId") Integer suiteId) {
        try {
            return new ResponseBundle().success(selectSuiteInfo(suiteId));
        } catch (Exception e) {
            logger.error("error:", e);
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    @ApiOperation(value = "新增班制", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "dutyName", value = "班制名", required = true, paramType = "body", dataType = "string"),
            @ApiImplicitParam(name = "districtId", value = "站区id", required = true, paramType = "body", dataType = "integer"),
            @ApiImplicitParam(name = "stationId", value = "站id", required = true, paramType = "body", dataType = "integer"),
            @ApiImplicitParam(name = "positionId", value = "岗位id", required = true, paramType = "body", dataType = "integer"),
            @ApiImplicitParam(name = "maxWorkingHour", value = "每周最大工时", required = true, paramType = "body", dataType = "integer"),
            @ApiImplicitParam(name = "minWorkingHour", value = "每周最小工时", required = true, paramType = "body", dataType = "integer"),
            @ApiImplicitParam(name = "maxWeeklyRestDays", value = "每周最多休几天", required = true, paramType = "body", dataType = "integer"),
            @ApiImplicitParam(name = "minWeeklyRestDays", value = "每周最少休几天", required = true, paramType = "body", dataType = "integer"),
            @ApiImplicitParam(name = "monthlyWorkingHourLimit", value = "每月最大工时数", required = true, paramType = "body", dataType = "integer"),
            @ApiImplicitParam(name = "yearlyWorkingHourLimit", value = "每年最大工时数", required = true, paramType = "body", dataType = "integer"),
            @ApiImplicitParam(name = "backup", value = "是否是备班(1备班，0正常)", required = true, paramType = "body", dataType = "integer")})
    @ResponseBody
    @PostMapping("/suite")
    public ResponseBundle addSuite(@RequestBody DutySuite dutySuite) {
        try {
            District district = Repositories.districtRepository.findOne(dutySuite.getDistrictId());
            dutySuite.setDistrictName(district.getDistrictName());
            if (dutySuite.getStationId()!=null){
                Station station = Repositories.stationRepository.findOne(dutySuite.getStationId());
                dutySuite.setStationName(station.getStationName());
            }
            Position pos = Repositories.positionRepository.findOne(dutySuite.getPositionId());
            dutySuite.setPositionName(pos.getPositionName());
            Repositories.dutySuiteRepository.saveAndFlush(dutySuite);
            return new ResponseBundle().success(selectSuiteInfo(dutySuite.getId()));
        } catch (Exception e) {
            logger.error("error:", e);
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    @ApiOperation(value = "修改班制", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "班制id", required = true, paramType = "body", dataType = "integer"),
            @ApiImplicitParam(name = "maxWorkingHour", value = "每周最大工时", required = true, paramType = "body", dataType = "integer"),
            @ApiImplicitParam(name = "minWorkingHour", value = "每周最小工时", required = true, paramType = "body", dataType = "integer"),
            @ApiImplicitParam(name = "maxWeeklyRestDays", value = "每周最多休几天", required = true, paramType = "body", dataType = "integer"),
            @ApiImplicitParam(name = "minWeeklyRestDays", value = "每周最少休几天", required = true, paramType = "body", dataType = "integer"),
            @ApiImplicitParam(name = "monthlyWorkingHourLimit", value = "每月最大工时数", required = true, paramType = "body", dataType = "integer"),
            @ApiImplicitParam(name = "yearlyWorkingHourLimit", value = "每年最大工时数", required = true, paramType = "body", dataType = "integer")})
    @ResponseBody
    @PutMapping("/suite")
    public ResponseBundle updateSuite(@RequestBody DutySuite dutySuite) {
        try {
            DutySuite duty = Repositories.dutySuiteRepository.findOne(dutySuite.getId());
            duty.setMaxWeeklyRestDays(dutySuite.getMaxWeeklyRestDays());
            duty.setMaxWorkingHour(dutySuite.getMaxWorkingHour());
            duty.setMinWorkingHour(dutySuite.getMinWorkingHour());
            duty.setMinWeeklyRestDays(dutySuite.getMinWeeklyRestDays());
            duty.setMonthlyWorkingHourLimit(dutySuite.getMonthlyWorkingHourLimit());
            duty.setYearlyWorkingHourLimit(dutySuite.getYearlyWorkingHourLimit());
            Repositories.dutySuiteRepository.save(duty);
            return new ResponseBundle().success(selectSuiteInfo(dutySuite.getId()));
        } catch (Exception e) {
            logger.error("error:", e);
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    @ApiOperation(value = "新增时间段", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "body", dataType = "integer"),
            @ApiImplicitParam(name = "startTimeStr", value = "开始时间", required = true, paramType = "body", dataType = "String"),
            @ApiImplicitParam(name = "endTimeStr", value = "结束时间", required = true, paramType = "body", dataType = "String"),
            @ApiImplicitParam(name = "userCount", value = "值班人数", required = true, paramType = "body", dataType = "integer")})
    @ResponseBody
    @PostMapping("/period")
    public ResponseBundle addPeriod(@RequestBody DutyPeriodChecking period) {
        try {
            period.setStartTime(getTime(period.getStartTimeStr()));
            period.setEndTime(getTime(period.getEndTimeStr()));
            Repositories.dutyPeriodCheckingRepository.save(period);
            return new ResponseBundle().success(period);
        } catch (Exception e) {
            logger.error("error:", e);
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    private Integer getTime(String startTimeStr) {
        String t=startTimeStr.startsWith("0")?startTimeStr.substring(1):startTimeStr;
        String[] time = t.split(":");
        return Integer.parseInt(time[0])*60+Integer.parseInt(time[1]);
    }

    @ApiOperation(value = "修改时间段", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true, paramType = "body", dataType = "integer"),
            @ApiImplicitParam(name = "startTimeStr", value = "开始时间", required = true, paramType = "body", dataType = "String"),
            @ApiImplicitParam(name = "endTimeStr", value = "结束时间", required = true, paramType = "body", dataType = "String"),
            @ApiImplicitParam(name = "userCount", value = "值班人数", required = true, paramType = "body", dataType = "integer")})
    @ResponseBody
    @PutMapping("/period")
    public ResponseBundle modifyPeriod(@RequestBody DutyPeriodChecking period) {
        try {
            DutyPeriodChecking p = Repositories.dutyPeriodCheckingRepository.findOne(period.getId());
            p.setEndTime(getTime(period.getEndTimeStr()));
            p.setStartTime(getTime(period.getStartTimeStr()));
            p.setStartTimeStr(period.getStartTimeStr());
            p.setEndTimeStr(period.getEndTimeStr());
            p.setUserCount(period.getUserCount());
            Repositories.dutyPeriodCheckingRepository.saveAndFlush(p);
            return new ResponseBundle().success(p);
        } catch (Exception e) {
            logger.error("error:", e);
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    @ApiOperation(value = "删除时间段", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "periodId", value = "时间段id", required = true,  paramType = "path", dataType = "integer")})
    @ResponseBody
    @DeleteMapping("/period/{periodId}")
    public ResponseBundle modifyPeriod(@PathVariable Integer periodId) {
        try {
            Repositories.dutyPeriodCheckingRepository.delete(periodId);
            return new ResponseBundle().success();
        } catch (Exception e) {
            logger.error("error:", e);
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    @ApiOperation(value = "新增班次", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "body", dataType = "integer"),
            @ApiImplicitParam(name = "dutyName", value = "班次名", required = true, paramType = "body", dataType = "String"),
            @ApiImplicitParam(name = "dutyCode", value = "班次编号", required = true, paramType = "body", dataType = "String"),
            @ApiImplicitParam(name = "userCount", value = "班次人数", required = true, paramType = "body", dataType = "integer"),
            @ApiImplicitParam(name = "classColor", value = "班次颜色", required = true, paramType = "body", dataType = "string"),
            @ApiImplicitParam(name = "startTimeStr", value = "班次几点上班-小时", required = true, paramType = "body", dataType = "string"),
            @ApiImplicitParam(name = "endTimeStr", value = "班次几点下班-小时", required = true, paramType = "body", dataType = "string"),
            @ApiImplicitParam(name = "restMinutes", value = "两班间隔（分钟）", required = true, paramType = "body", dataType = "integer"),
            @ApiImplicitParam(name = "relevantClassId", value = "关联班次", required = false, paramType = "body", dataType = "integer"),
            @ApiImplicitParam(name = "comment", value = "注意事项", required = true, paramType = "body", dataType = "string"),})
    @ResponseBody
    @PostMapping("/class")
    public ResponseBundle addDutyClass(@RequestBody DutyClass dutyClass) {
        try {
            DutySuite suite = Repositories.dutySuiteRepository.findOne(dutyClass.getSuiteId());
            dutyClass.setDistrictId(suite.getDistrictId());
            dutyClass.setDistrictName(suite.getDistrictName());
            dutyClass.setStationId(suite.getStationId());
            dutyClass.setStationName(suite.getStationName());
            dutyClass.setPositionId(suite.getPositionId());
            dutyClass.setPositionName(suite.getPositionName());
            dutyClass.setStartTime(getTime(dutyClass.getStartTimeStr()));
            dutyClass.setEndTime(getTime(dutyClass.getEndTimeStr()));
            dutyClass.setWorkingLength(dutyClass.getEndTime()-dutyClass.getStartTime());
            Repositories.dutyClassRepository.save(dutyClass);
            return new ResponseBundle().success(dutyClass);
        } catch (Exception e) {
            logger.error("error:", e);
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    @ApiOperation(value = "改班次", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "班次id", required = true, paramType = "body", dataType = "int"),
            @ApiImplicitParam(name = "dutyName", value = "班次名", required = true, paramType = "body", dataType = "String"),
            @ApiImplicitParam(name = "dutyCode", value = "班次编号", required = true, paramType = "body", dataType = "String"),
            @ApiImplicitParam(name = "userCount", value = "班次人数", required = true, paramType = "body", dataType = "integer"),
            @ApiImplicitParam(name = "classColor", value = "班次颜色", required = true, paramType = "body", dataType = "string"),
            @ApiImplicitParam(name = "startTimeStr", value = "班次几点上班-小时", required = true, paramType = "body", dataType = "string"),
            @ApiImplicitParam(name = "endTimeStr", value = "班次几点下班-小时", required = true, paramType = "body", dataType = "string"),
            @ApiImplicitParam(name = "restMinutes", value = "两班间隔（分钟）", required = true, paramType = "body", dataType = "integer"),
            @ApiImplicitParam(name = "relevantClassId", value = "关联班次", required = false, paramType = "body", dataType = "integer"),
            @ApiImplicitParam(name = "comment", value = "注意事项", required = true, paramType = "body", dataType = "string"),})
    @ResponseBody
    @PutMapping("/class")
    public ResponseBundle updateClass(@RequestBody DutyClass dutyClass) {
        try {
            DutyClass clz = Repositories.dutyClassRepository.findOne(dutyClass.getId());
            clz.setDutyName(dutyClass.getDutyName());
            clz.setDutyCode(dutyClass.getDutyCode());
            clz.setUserCount(dutyClass.getUserCount());
            clz.setClassColor(dutyClass.getClassColor());
            clz.setStartTimeStr(dutyClass.getStartTimeStr());
            clz.setStartTime(getTime(dutyClass.getStartTimeStr()));
            clz.setEndTimeStr(dutyClass.getEndTimeStr());
            clz.setEndTime(getTime(dutyClass.getEndTimeStr()));
            clz.setRestMinutes(dutyClass.getRestMinutes());
            clz.setWorkingLength(clz.getEndTime()-clz.getStartTime());
            clz.setRelevantClassId(dutyClass.getRelevantClassId());
            clz.setComment(dutyClass.getComment());
            Repositories.dutyClassRepository.save(clz);
            return new ResponseBundle().success(clz);
        } catch (Exception e) {
            logger.error("error:", e);
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    /**
     * 班次删
     *
     * @return
     */
    @ApiOperation(value = "删班次", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "classId", value = "班次id", required = true, paramType = "path", dataType = "integer"),})
    @ResponseBody
    @DeleteMapping("/class/{classId}")
    public ResponseBundle deleteClass(@PathVariable("classId") Integer classId) {
        try {
            DutyClass clz = Repositories.dutyClassRepository.findOne(classId);
            if (clz!=null){
                List<ScheduleWorkflow> list = Repositories.workflowRepository.findBySuiteIdAndClassId(clz.getSuiteId(), clz.getId());
                if (list.size()>0){
                    return new ResponseBundle().failure(ResponseMeta.HAS_WORKFLOW);
                }
                List<ScheduleTemplate> templates=Repositories.scheduleTemplateRepository.findBySuiteIdAndClassId(clz.getSuiteId(),classId);
                if (templates.size()>0){
                    return new ResponseBundle().failure(ResponseMeta.HAS_TEMPLATE);
                }
            }
            Repositories.dutyClassRepository.delete(classId);
            return new ResponseBundle().success();
        } catch (Exception e) {
            logger.error("error:", e);
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    @ApiOperation(value = "删一个班制", produces = "application/json")
    @ApiImplicitParam(name = "id", value = "班制id", required = true, paramType = "path", dataType = "integer")
    @ResponseBody
    @DeleteMapping("/suite/{id}")
    public ResponseBundle deleteSuite(@PathVariable("id") Integer id) {
        try {
            List<DutyClass> classes = Repositories.dutyClassRepository.findBySuiteId(id);
            if (!classes.isEmpty()){
                return new ResponseBundle().failure(ResponseMeta.HAS_CLASSES);
            }
            List<DutyPeriodChecking> periodCheckingList = Repositories.dutyPeriodCheckingRepository
                    .findBySuiteId(id);
            Repositories.dutyPeriodCheckingRepository.delete(periodCheckingList);
            Repositories.dutySuiteRepository.delete(id);
            return new ResponseBundle()
                    .success();
        } catch (Exception e) {
            logger.error("error:", e);
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    private Map<String, Object> selectSuiteInfo(Integer suiteId) {
        Map<String, Object> map = new HashMap<>();
        map.put("dutysuite", Repositories.dutySuiteRepository.findOne(suiteId));
        List<DutyClass> list = Repositories.dutyClassRepository.findBySuiteId(suiteId);
        list.forEach(l -> {
            if (l.getRelevantClassId() != null) {
                l.setRelevant(Repositories.dutyClassRepository.findOne(l.getRelevantClassId()));
            }
        });
        map.put("dutyclass", list);
        map.put("dutyperiodchecking", Repositories.dutyPeriodCheckingRepository.findBySuiteId(suiteId));
        return map;
    }

}
