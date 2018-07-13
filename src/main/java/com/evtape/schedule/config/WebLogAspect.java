package com.evtape.schedule.config;

import com.alibaba.fastjson.JSON;
import com.evtape.schedule.domain.OperationLog;
import com.evtape.schedule.domain.User;
import com.evtape.schedule.domain.vo.ResponseBundle;
import com.evtape.schedule.persistent.Repositories;
import com.evtape.schedule.util.JWTUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by lianhai on 2018/6/7.
 */
@Aspect
@Component
public class WebLogAspect {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private static final ThreadLocal<OperationLog> threadLocal = new ThreadLocal<>();

    @Pointcut("execution(public * com.evtape.schedule.web..*.*(..))")
    public void webLog() {
    }


    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) {
        LOGGER.info("--------------------------AOP begin--------------------------");
        OperationLog log=new OperationLog();
        log.setBeginTime(System.currentTimeMillis());

        // 接收到请求，记录请求内容
        LOGGER.info("WebLogAspect.doBefore()");
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        // 记录下请求内容
        LOGGER.info("请求URL : " + request.getRequestURL().toString());
        LOGGER.info("请求方法 : " + request.getMethod());
        String authorization = request.getHeader("Authorization");
        if (authorization != null) {
            String phoneNumber=getPhoneNumber(authorization);
            LOGGER.info("手机号码: {}", phoneNumber);
            log.setPhoneNumber(phoneNumber);
            if (request.getMethod().equals("POST")||request.getMethod().equals("PUT")||request.getMethod().equals("DELETE")){
                User u=Repositories.userRepository.findByPhoneNumber(phoneNumber);
                if (u!=null){
                    log.setOperatorName(u.getUserName());
                    log.setOperationName(getOperationName(joinPoint));
                }
            }
        }
        LOGGER.info("请求IP : " + request.getRemoteAddr());
        LOGGER.info("请求Handler : " + joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint
                .getSignature().getName());
        LOGGER.info("请求参数 : " + Arrays.toString(joinPoint.getArgs()));
        Enumeration<String> enu = request.getParameterNames();
        StringBuilder content=new StringBuilder();
        while (enu.hasMoreElements()) {
            String paraName = enu.nextElement();
            LOGGER.info("paramName: {}", request.getParameter(paraName));
            content.append(request.getParameter(paraName)).append(";");
        }
        log.setContent(content.toString());
        threadLocal.set(log);

    }

    @AfterReturning(returning = "result", pointcut = "webLog()")
    public Object doAfterReturning(Object result) {
        LOGGER.debug("返回值: {}", result == null ? "" : JSON.toJSONString(result));
        OperationLog log = threadLocal.get();
        long startTime = log.getBeginTime();
        double callTime = (System.currentTimeMillis() - startTime) / 1000.0;
        LOGGER.info("调用Handler花费时间time = {}s", callTime);
        LOGGER.info("--------------------------AOP end--------------------------");
        if (log.getPhoneNumber()!=null){
            log.setCreateDate(new Date());
            ResponseBundle responseBundle= (ResponseBundle) result;
            if (responseBundle.isSuccess()){
                log.setOperationState(0);
            }else {
                log.setOperationState(1);
            }
            Repositories.logRepository.save(log);
        }
        return result;
    }

    private String getPhoneNumber(String header) {
        LOGGER.debug("aspect header: {}", header);
        String phoneNumber = JWTUtil.getPhoneNumber(header);
        LOGGER.debug("aspect phoneNumber: {}", phoneNumber);
        return phoneNumber;
    }

    private String getOperationName(JoinPoint joinPoint) {
        Class clz = joinPoint.getSignature().getDeclaringType();
        Api api = (Api) clz.getDeclaredAnnotation(Api.class);
        int argLength = joinPoint.getArgs().length;
        Method method = null;
        try {
            Method[] declaredMethods = clz.getDeclaredMethods();
            List<Method> methods = Arrays.stream(declaredMethods).filter(m -> m.getName()
                    .equals(joinPoint.getSignature().getName()) && m.getParameterCount() == argLength)
                    .collect(Collectors.toList());
            method = methods.get(0);
            ApiOperation apiOperation = method.getDeclaredAnnotation(ApiOperation.class);
            return api.value()+"-"+apiOperation.value();
        } catch (Exception e) {
            return joinPoint.getSignature().getName();
        }
    }
}
