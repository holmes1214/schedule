package com.evtape.schedule.web;

import com.alibaba.fastjson.JSONObject;
import com.evtape.schedule.consts.ResponseMeta;
import com.evtape.schedule.domain.User;
import com.evtape.schedule.domain.form.LoginForm;
import com.evtape.schedule.domain.vo.ResponseBundle;
import com.evtape.schedule.persistent.Repositories;
import com.evtape.schedule.util.JWTUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author ripper 用戶列表
 */
@RestController
@RequestMapping(value = "/login", produces = "application/json;charset=UTF-8")
public class LoginController {

    /**
     * 登录
     */
    @PostMapping
    public ResponseBundle login(@RequestBody @Validated LoginForm form, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .sorted(Comparator.reverseOrder())
                    .collect(Collectors.toList());
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID, errors);
        }
        Optional<User> user = Optional.ofNullable(Repositories.userRepository.findByUserName(form.getUserName()));
        return user.map(u -> {
            if (ObjectUtils.notEqual(form.getPassword(), u.getPassword())) {
                return new ResponseBundle().failure(ResponseMeta.ADMIN_PASSWD_NOT_ERROR);
            }
            String token = JWTUtil.sign(u.getUserName(), u.getPassword());
            LOGGER.info("create login token:{}", token);
            JSONObject response = new JSONObject();
            response.put("id", u.getId());
            response.put("userName", u.getUserName());
            response.put("token", token);
            return new ResponseBundle().success(response);
        }).orElse(new ResponseBundle().failure(ResponseMeta.ADMIN_ACCOUNT_NOT_EXISTE));

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

}
