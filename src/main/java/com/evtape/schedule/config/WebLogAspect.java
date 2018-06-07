package com.evtape.schedule.config;

import com.alibaba.fastjson.JSON;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Enumeration;

/**
 * Created by lianhai on 2018/6/7.
 */
@Aspect
@Component
public class WebLogAspect {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private static final ThreadLocal<Long> timeTreadLocal = new ThreadLocal<>();

    @Pointcut("execution(public * com.evtape.schedule.web..*.*(..))")
    public void webLog() {
    }


    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) {
        LOGGER.info("--------------------------AOP begin--------------------------");
        timeTreadLocal.set(System.currentTimeMillis());
        // 接收到请求，记录请求内容
        LOGGER.info("WebLogAspect.doBefore()");
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        // 记录下请求内容
        LOGGER.info("请求URL : " + request.getRequestURL().toString());
        LOGGER.info("请求方法 : " + request.getMethod());
        LOGGER.info("请求IP : " + request.getRemoteAddr());
        LOGGER.info("请求Handler : " + joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint
                .getSignature().getName());
        LOGGER.info("请求参数 : " + Arrays.toString(joinPoint.getArgs()));
        Enumeration<String> enu = request.getParameterNames();
        while (enu.hasMoreElements()) {
            String paraName = enu.nextElement();
            LOGGER.info("paramName: {}", request.getParameter(paraName));
        }
    }

    @AfterReturning(returning = "result", pointcut = "webLog()")
    public Object doAfterReturning(Object result) {
        LOGGER.debug("返回值: {}", result == null ? "" : JSON.toJSONString(result));
        long startTime = timeTreadLocal.get();
        double callTime = (System.currentTimeMillis() - startTime) / 1000.0;
        LOGGER.info("调用Handler花费时间time = {}s", callTime);
        LOGGER.info("--------------------------AOP end--------------------------");
        return result;
    }
}
