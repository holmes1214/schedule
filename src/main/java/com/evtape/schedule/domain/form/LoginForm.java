package com.evtape.schedule.domain.form;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Pattern;

/**
 * Created by lianhai on 2018/5/28.
 */
@Getter
@Setter
@ToString
public class LoginForm {
    @Pattern(regexp = "^1(3|5|6|7|8)\\d{9}$", message = "手机号码格式不正确!")
    private String phoneNumber;
    @NotBlank(message = "密码不能为空")
    private String password;
}
