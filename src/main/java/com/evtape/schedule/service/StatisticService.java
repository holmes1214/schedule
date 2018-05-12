package com.evtape.schedule.service;

import com.evtape.schedule.consts.CacheKeyConstant;
import com.evtape.schedule.consts.Constants;
import com.evtape.schedule.domain.RecorderUser;
import com.evtape.schedule.persistent.Repositories;
import com.evtape.schedule.domain.SysTenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by holmes1214 on 18/12/2017.
 */
@Component
public class StatisticService {
    @Autowired
    private RedisService redis;

    private Logger logger = LoggerFactory.getLogger(StatisticService.class);


    public Integer getUserMaxStorage(RecorderUser user, String date) {
        String from=date+"-01";
        String to=date+"-31";
        String key= CacheKeyConstant.USER_STORAGE_VOL_PREFIX+user.getPhoneNumber();
        if(!redis.exists(key)){
            List<Number> result= Repositories.recordedSegmentRepository.getUserMaxStorage(user.getId(),from,to);
            if(result.size()>0&&result.get(0)!=null){
                redis.setex(key,result.get(0).longValue()+"", Constants.ONE_DAY);
            }else {
                redis.setex(key,"0",Constants.ONE_DAY);
            }
        }

        String value = redis.get(CacheKeyConstant.USER_STORAGE_VOL_PREFIX+user.getPhoneNumber());
        long result=value!=null?Long.parseLong(value):0;
        return (int)(result/1024/1024/1024);
    }

    public Integer getTenantMaxStorage(SysTenant tenant,String date) {
        String from=date+"-01";
        String to=date+"-31";
        String key=CacheKeyConstant.TENANT_STORAGE_VOL_PREFIX+tenant.getAdminPhoneNumber();
        if(!redis.exists(key)){
            List<Number> result=  Repositories.recordedSegmentRepository.getTenentMaxStorage(tenant.getId(),from,to);
            if(result.size()>0&&result.get(0)!=null){
                redis.setex(key,result.get(0).longValue()+"", Constants.ONE_DAY);
            }else {
                redis.setex(key,"0",Constants.ONE_DAY);
            }
        }

        String value = redis.get(key);
        long result=value!=null?Long.parseLong(value):0;
        return (int)(result/1024/1024/1024);
    }

    public Integer getUserDownload(RecorderUser user, String date) {
        String from=date+"-01";
        String to=date+"-31";
        String key=CacheKeyConstant.USER_DOWNLOAD_VOL_PREFIX+user.getPhoneNumber();
        if(!redis.exists(key)){
            List<Number> result=  Repositories.downloadLogRepository.getUserMonthlyDownload(user.getId(),from,to);
            if(result.size()>0&&result.get(0)!=null){
                redis.setex(key,result.get(0).longValue()+"", Constants.ONE_DAY);
            }else {
                redis.setex(key,"0",Constants.ONE_DAY);
            }
        }

        String value = redis.get(key);
        long result=value!=null?Long.parseLong(value):0;
        return (int)(result/1024/1024/1024);
    }

    public Integer getTenantDownload(SysTenant tenant,String date) {
        String from=date+"-01";
        String to=date+"-31";
        String key=CacheKeyConstant.TENANT_DOWNLOAD_VOL_PREFIX+tenant.getAdminPhoneNumber();
        if(!redis.exists(key)){
            List<Number> result= Repositories.downloadLogRepository.getTenantMonthlyDownload(tenant.getId(),from,to);
            if(result.size()>0&&result.get(0)!=null){
                redis.setex(key,result.get(0).longValue()+"", Constants.ONE_DAY);
            }else {
                redis.setex(key,"0",Constants.ONE_DAY);
            }
        }

        String value = redis.get(key);
        long result=value!=null?Long.parseLong(value):0;
        return (int)(result/1024/1024/1024);
    }
}
