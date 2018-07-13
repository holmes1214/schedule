package com.evtape.schedule.web;

import com.evtape.schedule.consts.ResponseMeta;
import com.evtape.schedule.domain.District;
import com.evtape.schedule.domain.OperationLog;
import com.evtape.schedule.domain.Station;
import com.evtape.schedule.domain.User;
import com.evtape.schedule.domain.form.DistrictManagerForm;
import com.evtape.schedule.domain.vo.ResponseBundle;
import com.evtape.schedule.persistent.Repositories;
import com.evtape.schedule.web.auth.Identity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

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
    @GetMapping
    public ResponseBundle addDistrict(@Identity String userPhoneNumber,
                                      @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
                                      @RequestParam(value = "dateStr", required = false) String dateStr
    ) {
        try {
            User user = Repositories.userRepository.findByPhoneNumber(userPhoneNumber);
            if (user.getRoleId() > 2) {
                return new ResponseBundle().failure(ResponseMeta.FORBIDDEN);
            }
            String sql = "select * from sys_operation_log where 1=1 ";
            if (user.getDistrictId() != null) {
                sql += " and district_id="+user.getDistrictId();
            }
            if (phoneNumber!=null){
                sql+=" and phone_number="+phoneNumber;
            }
            if(dateStr!=null){
                sql+=" and date(create_date)="+dateStr;
            }
            List<OperationLog> resultList = em.createNativeQuery(sql, OperationLog.class).getResultList();
            return new ResponseBundle().success(resultList);
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

}
