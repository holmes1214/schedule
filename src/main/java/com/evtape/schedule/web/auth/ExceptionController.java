package com.evtape.schedule.web.auth;

import com.evtape.schedule.consts.ResponseMeta;
import com.evtape.schedule.domain.vo.ResponseBundle;
import com.evtape.schedule.exception.ForbiddenException;
import org.apache.shiro.ShiroException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.time.LocalDateTime;

/**
 * Created by lianhai on 2018/3/31.
 */
@RestControllerAdvice
public class ExceptionController {

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(ShiroException.class)
    public ResponseBundle handle401(ShiroException e) {
        return new ResponseBundle().failure(ResponseMeta.UNAUTHORIZED, e.getMessage());
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseBundle handleShiro401(ShiroException e) {
        return new ResponseBundle().failure(ResponseMeta.UNAUTHORIZED, e.getMessage());
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseBundle handle401() {
        return new ResponseBundle().failure(ResponseMeta.UNAUTHORIZED);
    }

    // 捕获ForbiddenException
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(ForbiddenException.class)
    public ResponseBundle handle403() {
        return new ResponseBundle().failure(ResponseMeta.FORBIDDEN);
    }

    // 捕捉其他所有异常
    @ExceptionHandler(Exception.class)
    public void globalException(HttpServletRequest request, Throwable ex) {
        ex.printStackTrace();
    }
}
