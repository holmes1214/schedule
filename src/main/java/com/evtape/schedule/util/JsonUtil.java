package com.evtape.schedule.util;

import com.alibaba.fastjson.JSON;

/**
 * JSON转换工具类
 */
public class JsonUtil {
	

    public static String toJson(Object data) {
        return JSON.toJSONString(data);
    }

    public static <T> T fromJson(String jsonSource, Class<T> type) {
        return JSON.parseObject(jsonSource,type);
    }
    
}
