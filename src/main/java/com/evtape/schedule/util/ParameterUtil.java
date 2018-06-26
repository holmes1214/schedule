package com.evtape.schedule.util;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lianhai on 2018/4/10.
 */
public class ParameterUtil {
    /**
     * 将HttpServletRequest请求参数转为Map<String, String>
     */
    public static Map<String, String> getParamsFromRequest(HttpServletRequest request) {
        Map<String, String> parameters = new HashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();

        while (parameterNames.hasMoreElements()) {
            String parameterName = parameterNames.nextElement();
            parameters.put(parameterName, request.getParameter(parameterName));
        }
        return parameters;
    }
}
