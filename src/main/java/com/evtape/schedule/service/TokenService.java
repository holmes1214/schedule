package com.evtape.schedule.service;

import com.evtape.schedule.domain.RecorderUser;
import com.evtape.schedule.exception.BaseException;
import com.evtape.schedule.persistent.Repositories;
import com.evtape.schedule.consts.CacheKeyConstant;
import com.evtape.schedule.util.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Created by holmes1214 on 30/07/2017.
 */
@Service
public class TokenService {

    @Value("${user.token.expire}")
    private int userTokenExpire;

    @Autowired
    private RedisService redis;

    public String checkToken(String token) {
        String phone = validate(token);
        if (phone != null) {
            RecorderUser user = Repositories.recorderUserRepo.findByPhoneNumber(phone);
            if (user!=null){
                return user.getId().toString();
            }
        }
        throw new BaseException(ErrorCode.INVALID_TOKEN);
    }

    private String validate(String token) {
        if(token==null){
            return null;
        }
        if (redis.exists(token)) {
            redis.expire(token, userTokenExpire);
            String phone = redis.get(token);
            return phone;
        }
        return null;
    }

    public String createToken(String phone) {
        String token = UUID.randomUUID().toString();
        String key = CacheKeyConstant.USER_TOKEN_PREFIX+phone;
        String old = redis.get(key);
        redis.del(key);
        if(old!=null){
            redis.del(old);
        }
        redis.setex(token, phone, userTokenExpire);
        redis.setex(key, token, userTokenExpire);
        return token;
    }

    public String getPhoneByToken(String token) {
        return validate(token);
    }

    public String getUserIdByToken(String token) {
        String phone=validate(token);
        RecorderUser user = Repositories.recorderUserRepo.findByPhoneNumber(phone);
        if (user!=null){
            return user.getId().toString();
        }
        return null;
    }
}
