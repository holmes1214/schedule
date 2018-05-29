package com.evtape.schedule.web.auth;

import org.apache.shiro.authc.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Created by lianhai on 2018/5/27.
 */
@Component
public class IdentityArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return (methodParameter.getParameterType().isAssignableFrom(String.class) &&
                methodParameter.hasParameterAnnotation(Identity.class));
    }


    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest
            webRequest, WebDataBinderFactory binderFactory) throws Exception {
        String phoneNumber = (String) webRequest.getAttribute("phoneNumber", RequestAttributes.SCOPE_REQUEST);
        String identification;
        if (phoneNumber != null) {
            identification = phoneNumber;
            LOGGER.info("phoneNumber:{}", phoneNumber);
            return identification;
        }
        throw new AuthenticationException();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityArgumentResolver.class);
}
