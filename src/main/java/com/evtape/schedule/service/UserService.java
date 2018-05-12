package com.evtape.schedule.service;

import com.evtape.schedule.consts.CacheKeyConstant;
import com.evtape.schedule.consts.Constants;
import com.evtape.schedule.domain.RecorderUser;
import com.evtape.schedule.persistent.Repositories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by holmes1214 on 18/12/2017.
 */
@Component
public class UserService {
    @Autowired
    private RedisService redis;

    private Logger logger = LoggerFactory.getLogger(UserService.class);

    public String getUserName(Long createUserId) {
        String key= CacheKeyConstant.USER_INFO_PREFIX+createUserId;
        updateRedis(createUserId);
        return redis.hget(key,"userName");
    }

    public void updateRedis(Long userId){
        String key=CacheKeyConstant.USER_INFO_PREFIX+userId;
        if(!redis.exists(key)){
            RecorderUser one = Repositories.recorderUserRepo.findOne(userId);
            redis.hset(key,"userName",one.getUserName());
            redis.hset(key,"phone",one.getPhoneNumber());
            redis.hset(key,"downloadLimit",String.valueOf(one.getDownloadLimit()));
            redis.hset(key,"delPolicy",String.valueOf(one.getDelPolicy()));
            redis.hset(key,"isAdmin",String.valueOf(one.getIsAdmin()));
            redis.hset(key,"reserveDays",String.valueOf(one.getReserveDays()));
            redis.hset(key,"saveLimit",String.valueOf(one.getSaveLimit()));
            redis.hset(key,"warningType",String.valueOf(one.getWarningType()));
            redis.hset(key,"tenantId",String.valueOf(one.getTenantId()));
            redis.expire(key, Constants.ONE_DAY);
        }
    }

    public String getUserDelPolicy(Long userId) {
        String key=CacheKeyConstant.USER_INFO_PREFIX+userId;
        updateRedis(userId);
        return redis.hget(key,"delPolicy");
    }

    public String getUserNDays(Long userId) {
        String key=CacheKeyConstant.USER_INFO_PREFIX+userId;
        updateRedis(userId);
        return redis.hget(key,"reserveDays");
    }
}
