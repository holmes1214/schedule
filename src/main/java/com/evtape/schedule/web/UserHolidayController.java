package com.evtape.schedule.web;

import com.evtape.schedule.consts.ResponseMeta;
import com.evtape.schedule.domain.UserHolidayLimit;
import com.evtape.schedule.domain.vo.ResponseBundle;
import com.evtape.schedule.persistent.Repositories;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author ripper 年假设置接口
 */
@Api(value = "年假设置接口")
@RestController
@RequestMapping("/holiday")
public class UserHolidayController {

    @ApiOperation(value = "按年度获取年假额度", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "year", value = "年度", required = true, paramType = "query",
                    dataType = "string"),
            @ApiImplicitParam(name = "districtId", value = "站区id", required = false, paramType = "query",
                    dataType = "int"),
    })
    @GetMapping
    public ResponseBundle search(@RequestParam("year") String year,@RequestParam("lineNumber") String lineNumber,@RequestParam("districtId") Integer districtId) {
        List<UserHolidayLimit> list=Repositories.holidayLimitRepository.findByYearStr(year);
        if (districtId!=null){
            list=list.stream().filter(t->districtId.equals(t.getDistrictId())).collect(Collectors.toList());
        }
        return new ResponseBundle().success(list);
    }

    @ApiOperation(value = "获取用户剩余年假", produces = "application/json")
    @GetMapping("/{userId}")
    public ResponseBundle getHoliday(@PathVariable("userId") Integer userId) {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        UserHolidayLimit limit=Repositories.holidayLimitRepository.findByYearStrAndUserId(year+"",userId);
        long n=Repositories.scheduleLeaveRepository.countAnnualLeave(userId,year+"-01-01");
        Map<String,Integer> result=new HashMap<>();
        result.put("limit",limit.getYearlyLimit());
        result.put("consumed", (int) n);
        return new ResponseBundle().success(result);
    }


    @ApiOperation(value = "导入休假", produces = "application/json")
    @PostMapping
    public ResponseBundle addDistrict(@RequestParam  MultipartFile file) {
        try {
            return new ResponseBundle().success();
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }


}
