package com.evtape.schedule.web;

import com.evtape.schedule.consts.Constants;
import com.evtape.schedule.consts.ResponseMeta;
import com.evtape.schedule.domain.District;
import com.evtape.schedule.domain.ScheduleInfo;
import com.evtape.schedule.domain.User;
import com.evtape.schedule.domain.UserHolidayLimit;
import com.evtape.schedule.domain.vo.ResponseBundle;
import com.evtape.schedule.persistent.Repositories;
import com.evtape.schedule.util.PoiUtil;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
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
            @ApiImplicitParam(name = "districtId", value = "站区id", paramType = "query",
                    dataType = "int"),
            @ApiImplicitParam(name = "idCardNo", value = "身份证号", paramType = "query",
                    dataType = "int"),
    })
    @GetMapping
    public ResponseBundle search(@RequestParam("year") String year, @RequestParam(value = "districtId",required = false) Integer districtId,
                                 @RequestParam(value = "idCardNo",required = false) String idCardNo) {
        if (StringUtils.isNotBlank(idCardNo)){
            User user = Repositories.userRepository.findByIdCardNumber(idCardNo);
            if (user!=null) {
                List<UserHolidayLimit> list=new ArrayList<>();
                list.add(Repositories.holidayLimitRepository.findByYearStrAndUserId(year,user.getId()));
                return new ResponseBundle().success(list);
            }
        }
        List<UserHolidayLimit> list = Repositories.holidayLimitRepository.findByYearStr(year);
        if (districtId != null) {
            list = list.stream().filter(t -> districtId.equals(t.getDistrictId())).collect(Collectors.toList());
        }
        return new ResponseBundle().success(list);
    }

    @ApiOperation(value = "获取用户剩余年假", produces = "application/json")
    @ApiImplicitParam(name = "userId", value = "用户id", required = true,  paramType = "path", dataType = "int")
    @GetMapping("/annual/{userId}")
    public ResponseBundle getHoliday(@PathVariable("userId") Integer userId) {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        UserHolidayLimit limit = Repositories.holidayLimitRepository.findByYearStrAndUserId(year + "", userId);
        long n = Repositories.scheduleLeaveRepository.countAnnualLeave(userId, year + "-01-01");
        Map<String, Integer> result = new HashMap<>();
        result.put("limit", limit.getYearlyLimit());
        result.put("consumed", (int) n);
        return new ResponseBundle().success(result);
    }

    @ApiOperation(value = "获取用户剩余病假", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "scheduleInfoId", value = "请假日排班id", required = true, paramType = "query",
                    dataType = "int"),
    })
    @GetMapping("/sickleft")
    public ResponseBundle getSickLeave(@RequestParam("scheduleInfoId") Integer scheduleInfoId) throws ParseException {
        DateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT);
        ScheduleInfo info = Repositories.scheduleInfoRepository.findOne(scheduleInfoId);
        Date leaveDate=info.getScheduleDate();
        Date next = DateUtils.ceiling(leaveDate, Calendar.YEAR);
        Date begin=DateUtils.addYears(next,-1);
        long n = Repositories.scheduleLeaveRepository.countSickLeave(info.getUserId(),  df.format(begin));
        String limit=calcSickLimit(Repositories.userRepository.findOne(info.getUserId()),leaveDate);
        Map<String, Object> result = new HashMap<>();
        result.put("limit", limit);
        result.put("consumed", (int) n);
        return new ResponseBundle().success(result);
    }

    private String calcSickLimit(User user,Date leaveDate) throws ParseException {
        DateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT);
        String beginWork = user.getBeginWorkDate();
        String entry = user.getEntryDate();
        Date beginDate = df.parse(beginWork);
        Date entryDate = df.parse(entry);
        Date decade = DateUtils.addYears(beginDate, 10);
        boolean moreThanDecade=false;
        if (decade.getTime()<leaveDate.getTime()){
            moreThanDecade=true;
        }
        String medicalPeriod="医疗期";
        if (!moreThanDecade){
            Date delta = DateUtils.addYears(entryDate, 5);
            if (delta.getTime()>leaveDate.getTime()){
                return medicalPeriod+"3个月";
            }else {
                return medicalPeriod+"6个月";
            }
        }else {
            Date delta1 = DateUtils.addYears(entryDate, 5);
            if (delta1.getTime()>leaveDate.getTime()){
                return medicalPeriod+"6个月";
            }
            Date delta2=DateUtils.addYears(entryDate, 10);
            if (delta2.getTime()>leaveDate.getTime()){
                return medicalPeriod+"9个月";
            }
            Date delta3=DateUtils.addYears(entryDate, 15);
            if (delta3.getTime()>leaveDate.getTime()){
                return medicalPeriod+"12个月";
            }
            Date delta4=DateUtils.addYears(entryDate, 20);
            if (delta4.getTime()>leaveDate.getTime()){
                return medicalPeriod+"18个月";
            }else {
                return medicalPeriod+"24个月";
            }
        }
    }


    @ApiOperation(value = "导入年假", produces = "application/json")
    @PostMapping
    public ResponseBundle importAnnualLeave(@ApiParam(value = "上传的文件",required = true) MultipartFile file) {
        try {
            if (!file.getOriginalFilename().endsWith("xlsx")){
                return new ResponseBundle().failure(ResponseMeta.BAD_FILE_FORMAT);
            }
            List<String> titles=PoiUtil.readTitle(file,0);
            String title = titles.get(3);
            String yearStr=title.substring(0,4);
            Map<Integer, District> districtMap = Repositories.districtRepository.findAll().stream().collect(Collectors.toMap(District::getId, d -> d));
            List<List<String>> lists = PoiUtil.readExcelListContent(file, 0, 1);
            List<UserHolidayLimit> result=new ArrayList<>();
            lists.forEach(l->{
                String code=l.get(0);
                String identity=l.get(1);
                String days=l.get(3);
                User u=Repositories.userRepository.findByEmployeeCode(code);
                if (u==null){
                    u=Repositories.userRepository.findByIdCardNumber(identity);
                    if (u==null){
                        return;
                    }
                }

                UserHolidayLimit limit=new UserHolidayLimit();
                limit.setLeaveType(1);
                limit.setYearStr(yearStr);
                limit.setLineNumber(districtMap.get(u.getDistrictId()).getLineNumber());
                limit.setDistrictId(u.getDistrictId());
                limit.setDistrictName(u.getDistrictName());
                limit.setPositionId(u.getPositionId());
                limit.setStationName(u.getPositionName());
                limit.setUserCode(code);
                limit.setStationId(u.getStationId());
                limit.setStationName(u.getStationName());
                limit.setUserId(u.getId());
                limit.setYearlyLimit(Integer.parseInt(days));
                limit.setUserName(u.getUserName());
                result.add(limit);
            });
            Repositories.holidayLimitRepository.save(result);
            return new ResponseBundle().success();
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }


}
