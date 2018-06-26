package com.evtape.schedule.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.evtape.schedule.consts.ResponseMeta;
import com.evtape.schedule.domain.DutyClass;
import com.evtape.schedule.domain.DutyPeriodChecking;
import com.evtape.schedule.domain.DutySuite;
import com.evtape.schedule.domain.vo.ResponseBundle;
import com.evtape.schedule.persistent.Repositories;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;


/**
 * TODO 权限没加，最后一块儿加，现在加上权限调接口费劲
 *
 * @author ripper duty列表
 */
@Api(value = "班制接口")
@RestController
@RequestMapping("/duty")
public class DutyController {


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
    @GetMapping
    public ResponseBundle suitlist(@RequestParam("districtId") Integer districtId,
                                   @RequestParam(value="stationId",required=false) Integer stationId,
                                   @RequestParam(value="positionId",required=false) Integer positionId,
                                   @RequestParam(value="backup",required=false) Integer backup) {
        // 备班班制，backup传1
        try {
        	   List<DutySuite> list = null ;
			if (backup != null && backup == 1) {
            	list=Repositories.dutySuiteRepository.findByDistrictIdAndBackup(districtId, backup);
            }else if ((positionId != null) && (stationId != null)) {
				list = Repositories.dutySuiteRepository.findByDistrictIdAndStationIdAndPositionId(districtId, stationId,
						positionId);
			}else if((positionId == null) && (stationId != null)){
				list = Repositories.dutySuiteRepository.findByDistrictIdAndStationId(districtId, stationId);
			}
			else if((positionId == null) && (stationId == null)){
				list = Repositories.dutySuiteRepository.findByDistrictId(districtId);
			}
            return new ResponseBundle().success(list);
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

	@ApiOperation(value = "根据班制id获取班次和检查条件", produces = "application/json")
	@ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "query", dataType = "int")
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
			@ApiImplicitParam(name = "dutyName", value = "班制名", required = true, paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "active", value = "是否启用(启动1, 没启用:0)", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "districtId", value = "站区id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "districtName", value = "站区名", required = true, paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "stationId", value = "站id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "stationName", value = "站点名", required = true, paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "positionId", value = "岗位id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "positionName", value = "岗位名，身份：管理员、普通职工", required = true, paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "maxWorkingHour", value = "每周最大工时", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "minWorkingHour", value = "每周最小工时", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "maxWeeklyRestDays", value = "每周最多休几天", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "minWeeklyRestDays", value = "每周最少休几天", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "monthlyWorkingHourLimit", value = "每月最大工时数", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "yearlyWorkingHourLimit", value = "每年最大工时数", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "backup", value = "是否是备班(1备班，0正常)", required = true, paramType = "body", dataType = "integer") })
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

    @ApiOperation(value = "修改班制", produces = "application/json")
    @ApiImplicitParams({
    		@ApiImplicitParam(name = "id", value = "班制id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "dutyName", value = "班制名", required = true, paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "active", value = "是否启用(启动1, 没启用:0)", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "districtId", value = "站区id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "districtName", value = "站区名", required = true, paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "stationId", value = "站id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "stationName", value = "站点名", required = true, paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "positionId", value = "岗位id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "positionName", value = "岗位名，身份：管理员、普通职工", required = true, paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "maxWorkingHour", value = "每周最大工时", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "minWorkingHour", value = "每周最小工时", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "maxWeeklyRestDays", value = "每周最多休几天", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "minWeeklyRestDays", value = "每周最少休几天", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "monthlyWorkingHourLimit", value = "每月最大工时数", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "yearlyWorkingHourLimit", value = "每年最大工时数", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "backup", value = "是否是备班(1备班，0正常)", required = true, paramType = "body", dataType = "integer") })
    @ResponseBody
    @PutMapping("/suitupdate")
    public ResponseBundle suitupdate(@RequestBody DutySuite dutySuite) {
        try {
            Repositories.dutySuiteRepository.saveAndFlush(dutySuite);
            return new ResponseBundle().success(selectSuiteInfo(dutySuite.getId()));
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

	@ApiOperation(value = "启用某班制", produces = "application/json")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "districtId", value = "站区id", required = true, paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "stationId", value = "站点id", required = true, paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "positionId", value = "岗位id", required = true, paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "query", dataType = "int"), })
    @ResponseBody
    @PutMapping("/suiteactive")
    public ResponseBundle suitactive(@RequestParam("districtId") Integer districtId,
                                     @RequestParam("stationId") Integer stationId,
                                     @RequestParam("positionId") Integer positionId,
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
			@ApiImplicitParam(name = "dutyName", value = "班次名", required = true, paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "dutyCode", value = "班次code", required = true, paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "districtId", value = "站区id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "districtName", value = "站区名", required = true, paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "stationId", value = "站点id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "stationName", value = "站点名", required = true, paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "positionId", value = "岗位id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "positionName", value = "岗位名，身份：管理员、普通职工", required = true, paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "userCount", value = "班次人数", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "classColor", value = "班次颜色", required = true, paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "startTimeStr", value = "班次几点上班-小时", required = true, paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "endTimeStr", value = "班次几点下班-小时", required = true, paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "workingLength", value = "本班工时，班次时长（分钟）", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "restMinutes", value = "两班间隔（分钟）", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "relevantClassId", value = "关联班次", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "backup", value = "是否有备班(1备班，0正常)", required = true, paramType = "body", dataType = "integer"), 
			@ApiImplicitParam(name = "comment", value = "注意事项", required = true, paramType = "body", dataType = "string"), })
    @ResponseBody
    @PostMapping("/class")
    public ResponseBundle classadd(@RequestBody DutyClass dutyClass) {
        try {
        	if(StringUtils.isNotBlank(dutyClass.getStartTimeStr())){
				dutyClass.setStartTime(new Integer(StringUtils.substring(dutyClass.getStartTimeStr(), 0, 2)) * 60
						+ (new Integer(StringUtils.substring(dutyClass.getStartTimeStr(), 3, 5))));
        	}
        	if(StringUtils.isNotBlank(dutyClass.getEndTimeStr())){
				dutyClass.setEndTime(new Integer(StringUtils.substring(dutyClass.getEndTimeStr(), 0, 2)) * 60
						+ (new Integer(StringUtils.substring(dutyClass.getEndTimeStr(), 3, 5))));
        	}
            Integer suiteId = dutyClass.getSuiteId();
            Repositories.dutyClassRepository.saveAndFlush(dutyClass);
            return new ResponseBundle().success(selectSuiteInfo(suiteId));
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }
    
	@ApiOperation(value = "改班次", produces = "application/json")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "id", value = "班次id", required = true, paramType = "body", dataType = "int"),
			@ApiImplicitParam(name = "dutyName", value = "班次名", required = true, paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "dutyCode", value = "班次code", required = true, paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "districtId", value = "站区id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "districtName", value = "站区名", required = true, paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "stationId", value = "站点id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "stationName", value = "站点名", required = true, paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "positionId", value = "岗位id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "positionName", value = "岗位名，身份：管理员、普通职工", required = true, paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "userCount", value = "班次人数", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "classColor", value = "班次颜色", required = true, paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "startTimeStr", value = "班次几点上班-小时", required = true, paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "endTimeStr", value = "班次几点下班-小时", required = true, paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "workingLength", value = "本班工时，班次时长（分钟）", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "restMinutes", value = "两班间隔（分钟）", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "relevantClassId", value = "关联班次", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "backup", value = "是否有备班(1备班，0正常)", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "comment", value = "注意事项", required = true, paramType = "body", dataType = "string"), })
    @ResponseBody
    @PutMapping("/classupdate")
    public ResponseBundle classupdate(@RequestBody DutyClass dutyClass) {
        try {
            Integer suiteId = dutyClass.getSuiteId();
        	if(StringUtils.isNotBlank(dutyClass.getStartTimeStr())){
				dutyClass.setStartTime(new Integer(StringUtils.substring(dutyClass.getStartTimeStr(), 0, 2)) * 60
						+ (new Integer(StringUtils.substring(dutyClass.getStartTimeStr(), 3, 5))));
        	}
        	if(StringUtils.isNotBlank(dutyClass.getEndTimeStr())){
				dutyClass.setEndTime(new Integer(StringUtils.substring(dutyClass.getEndTimeStr(), 0, 2)) * 60
						+ (new Integer(StringUtils.substring(dutyClass.getEndTimeStr(), 3, 5))));
        	}

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
	@ApiOperation(value = "删班次", produces = "application/json")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "id", value = "班次id", required = true, paramType = "path", dataType = "integer"),
			@ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "qyery", dataType = "integer"), })
    @ResponseBody
    @DeleteMapping("/classdelete/{id}")
    public ResponseBundle classdelete(@PathVariable("id") Integer id,@RequestParam("suiteId") Integer suiteId) {
        try {
            Repositories.dutyClassRepository.delete(id);
            Repositories.dutyClassRepository.flush();
            return new ResponseBundle().success(selectSuiteInfo(suiteId));
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }
	
	@ApiOperation(value = "增一个检查条件", produces = "application/json")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "districtId", value = "站区id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "stationId", value = "站点id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "positionId", value = "岗位id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "startTimeStr", value = "班次几点上班-小时", required = true, paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "endTimeStr", value = "班次几点下班-小时", required = true, paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "userCount", value = "班次人数", required = true, paramType = "body", dataType = "integer"), })
    @ResponseBody
    @PostMapping("/periodadd")
    public ResponseBundle periodadd(@RequestBody DutyPeriodChecking dutyPeriodChecking) {
        try {
            Integer suiteId = dutyPeriodChecking.getSuiteId();
            
        	if(StringUtils.isNotBlank(dutyPeriodChecking.getStartTimeStr())){
        		dutyPeriodChecking.setStartTime(new Integer(StringUtils.substring(dutyPeriodChecking.getStartTimeStr(), 0, 2)) * 60
						+ (new Integer(StringUtils.substring(dutyPeriodChecking.getStartTimeStr(), 3, 5))));
        	}
        	if(StringUtils.isNotBlank(dutyPeriodChecking.getEndTimeStr())){
        		dutyPeriodChecking.setEndTime(new Integer(StringUtils.substring(dutyPeriodChecking.getEndTimeStr(), 0, 2)) * 60
						+ (new Integer(StringUtils.substring(dutyPeriodChecking.getEndTimeStr(), 3, 5))));
        	}
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
	@ApiOperation(value = "更新一个检查条件", produces = "application/json")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "id", value = "检查条件id", required = true, paramType = "body", dataType = "int"),
			@ApiImplicitParam(name = "districtId", value = "站区id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "stationId", value = "站点id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "positionId", value = "岗位id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "suiteId", value = "班制id", required = true, paramType = "body", dataType = "integer"),
			@ApiImplicitParam(name = "startTimeStr", value = "班次几点上班-小时", required = true, paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "endTimeStr", value = "班次几点下班-小时", required = true, paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "userCount", value = "班次人数", required = true, paramType = "body", dataType = "integer"), })
    @ResponseBody
    @PutMapping("/periodupdate")
    public ResponseBundle periodupdate(@RequestBody DutyPeriodChecking dutyPeriodChecking) {
        try {
            Integer suiteId = dutyPeriodChecking.getSuiteId();
        	if(StringUtils.isNotBlank(dutyPeriodChecking.getStartTimeStr())){
        		dutyPeriodChecking.setStartTime(new Integer(StringUtils.substring(dutyPeriodChecking.getStartTimeStr(), 0, 2)) * 60
						+ (new Integer(StringUtils.substring(dutyPeriodChecking.getStartTimeStr(), 3, 5))));
        	}
        	if(StringUtils.isNotBlank(dutyPeriodChecking.getEndTimeStr())){
        		dutyPeriodChecking.setEndTime(new Integer(StringUtils.substring(dutyPeriodChecking.getEndTimeStr(), 0, 2)) * 60
						+ (new Integer(StringUtils.substring(dutyPeriodChecking.getEndTimeStr(), 3, 5))));
        	}
            Repositories.dutyPeriodCheckingRepository.saveAndFlush(dutyPeriodChecking);
            return new ResponseBundle().success(selectSuiteInfo(suiteId));
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }


	@ApiOperation(value = "删一个检查条件", produces = "application/json")
	@ApiImplicitParam(name = "id", value = "检查条件id", required = true, paramType = "path", dataType = "integer")
    @ResponseBody
    @DeleteMapping("/perioddelete/{id}")
    public ResponseBundle perioddelete(@PathVariable("id") Integer id) {
        try {
            Repositories.dutyPeriodCheckingRepository.delete(id);
            Repositories.dutyPeriodCheckingRepository.flush();
            return new ResponseBundle().success(id);
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

	@ApiOperation(value = "删一个班制", produces = "application/json")
	@ApiImplicitParam(name = "id", value = "班制id", required = true, paramType = "path", dataType = "integer")
    @ResponseBody
    @DeleteMapping("/suitedelete/{id}")
    public ResponseBundle suitdelete(@PathVariable("id") Integer id) {
        try {
            DutySuite dutySuite1 = Repositories.dutySuiteRepository.findOne(id);
            if (dutySuite1 == null || dutySuite1.getActive() == 1) {
                return new ResponseBundle().failure(ResponseMeta.SUITE_ISACTIVE);
            }
            List<DutyClass> dutyClasslist = Repositories.dutyClassRepository.findBySuiteId(id);
            for (DutyClass dutyClass : dutyClasslist) {
                Repositories.dutyClassRepository.delete(dutyClass.getId());
            }
            Repositories.dutyClassRepository.flush();
            List<DutyPeriodChecking> periodCheckinglist = Repositories.dutyPeriodCheckingRepository
                    .findBySuiteId(id);
            for (DutyPeriodChecking dutyPeriodChecking : periodCheckinglist) {
                Repositories.dutyPeriodCheckingRepository.delete(dutyPeriodChecking.getId());
            }
            Repositories.dutyPeriodCheckingRepository.flush();
            Repositories.dutySuiteRepository.delete(id);
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
