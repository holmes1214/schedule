package com.evtape.schedule.web;

import com.evtape.schedule.consts.Constants;
import com.evtape.schedule.consts.ResponseMeta;
import com.evtape.schedule.domain.*;
import com.evtape.schedule.domain.form.DistrictManagerForm;
import com.evtape.schedule.domain.vo.ResponseBundle;
import com.evtape.schedule.persistent.Repositories;
import com.evtape.schedule.web.auth.Identity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ripper 站区接口,增刪改查
 */
@Api(value = "操作记录接口")
@RestController
@RequestMapping("/log")
public class LogController {

    @Autowired
    EntityManager em;

    @ApiOperation(value = "操作记录查询", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "phoneNumber", value = "操作人手机号", paramType = "query",
                    dataType = "string"),
            @ApiImplicitParam(name = "dateStr", value = "查询日期", paramType = "query",
                    dataType = "String"),
    })
    @GetMapping("/operation")
    public ResponseBundle getOperationLog(@Identity String userPhoneNumber,
                                          @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
                                          @RequestParam(value = "dateStr", required = false) String dateStr
    ) {
        try {
            User user = Repositories.userRepository.findByPhoneNumber(userPhoneNumber);
            if (user.getRoleId().equals(Constants.COMMON)) {
                return new ResponseBundle().failure(ResponseMeta.FORBIDDEN);
            }
            String sql = "select * from sys_operation_log where 1=1 ";
            if (user.getDistrictId() != null) {
                sql += " and district_id=" + user.getDistrictId();
            }
            if (user.getRoleId().equals(Constants.STATION)) {
                sql += " and operator_name='" + user.getUserName() + "'";
            }
            if (phoneNumber != null) {
                sql += " and phone_number='" + phoneNumber + "'";
            }
            if (dateStr != null) {
                sql += " and date(create_date)='" + dateStr + "'";
            }
            sql += " order by create_date desc";
            List<OperationLog> resultList = em.createNativeQuery(sql, OperationLog.class).getResultList();
            return new ResponseBundle().success(resultList);
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    @ApiOperation(value = "工时报表查询", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "districtId", value = "站区", paramType = "query",
                    dataType = "int"),
            @ApiImplicitParam(name = "yearStr", value = "查询年份", required = true, paramType = "query",
                    dataType = "string"),
            @ApiImplicitParam(name = "season", value = "查询季度", paramType = "query",
                    dataType = "string"),
            @ApiImplicitParam(name = "month", value = "查询月份", paramType = "query",
                    dataType = "string"),
    })
    @GetMapping("/workload")
    public ResponseBundle getWorkLoad(@Identity String userPhoneNumber,
                                      @RequestParam(value = "districtId", required = false) Integer districtId,
                                      @RequestParam(value = "yearStr") String yearStr,
                                      @RequestParam(value = "season", required = false) String season,
                                      @RequestParam(value = "month", required = false) String month
    ) {
        try {
            User user = Repositories.userRepository.findByPhoneNumber(userPhoneNumber);
            if (user.getRoleId() != 1 && user.getRoleId() != 2) {
                return new ResponseBundle().failure(ResponseMeta.FORBIDDEN);
            }
            if (user.getRoleId() == 2) {
                districtId = user.getDistrictId();
            }
            String sql = "from WorkLoadReport where yearStr='" + yearStr + "'";
            if (districtId != null) {
                sql += " and districtId=" + districtId;
            }
            if (season != null) {
                sql += " and seasonStr='" + season + "'";
            }
//            else {
//                sql += " and seasonStr is null";
//            }
            if (month != null) {
                sql += " and monthStr='" + month + "'";
            }
//            else {
//                sql += " and monthStr is null";
//            }
            List<WorkLoadReport> resultList = em.createQuery(sql, WorkLoadReport.class).getResultList();
            return new ResponseBundle().success(resultList.stream().collect(Collectors.groupingBy(WorkLoadReport::getLineNumber)));
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }


}
