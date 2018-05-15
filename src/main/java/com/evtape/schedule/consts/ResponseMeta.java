package com.evtape.schedule.consts;

/**
 * Created by lianhai on 2018/4/9.
 */
public enum ResponseMeta {

    /* 请求成功 */
    SUCCESS(0, "操作成功"),

    /* 请求错误 */
    BAD_REQUEST(400, "错误的请求"),
    UNAUTHORIZED(401, "未经授权资源"),
    FORBIDDEN(403, "权限不足"),

    /* 请求参数错误*/
    REQUEST_PARAM_INVALID(10001, "请求参数错误"),

    /* 验证码错误 */
    SMS_PHONENUMBER_NULL(20001, "接收验证码的手机号码不能为空 "),
    SMS_VCODE_EXPIRED_OR_WRONG(20002, "验证码错误或已过期"),
    SMS_REQUEST_TOO_FAST(20003, "验证码请求过于频繁"),

    /* 用户错误 */
    ADMIN_ACCOUNT_NOT_EXISTE(30001, "账号不存在"),
    ADMIN_PASSWD_NOT_ERROR(30002, "密码错误"),
    USER_ACCOUNT_INACTIVE(30003, "账号已被禁用"),
    USER_NOT_EXIST(30004, "账号不存在"),
    USER_TENANT_EXISTED(30005, "租户名称已存在"),
    USER_ACCOUNT_EXISTS(3006, "用户名已存在"),
    USER_PHONENUMBER_EXISTED(30007, "手机号码已存在"),
    ANCHOR_HAD_EXISTS(30008, "主播URL已经存在"),

    /* 业务错误 */
    BUSINESS_ERROR(40001, "此业务出现问题"),
    VIDEO_PLAY_PROHIBIT(4002, "您无权观看这个视频"),
    BUSINESS_ADD_ANCHOR_LIMIT(40003, "主播添加余额不足"),
    BUSINESS_ADD_USER_LIMIT(40004, "用户添加余额不足"),
    BUSINESS_VIDEO_PLAY_LIMIT(40005, "在线观看余额不足"),
    BUSINESS_GENARATE_FRAGMENT_LIMIT(40006, "片段生成余额不足"),
    BUSINESS_DOWNLOAD_VIDEO_LIMIT(40007, "视频下载余额不足"),

    /* 系统错误 */
    SYSTEM_INNER_ERROR(50001, "系统繁忙，请稍后重试");
    /* 无权观看视频 */

    private Integer code;

    private String message;

    ResponseMeta(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer code() {
        return this.code;
    }

    public String message() {
        return this.message;
    }

    public static Integer getCode(String name) {
        for (ResponseMeta item : ResponseMeta.values()) {
            if (item.name().equals(name)) {
                return item.code;
            }
        }
        return null;
    }

    public static String getMessage(String name) {
        for (ResponseMeta item : ResponseMeta.values()) {
            if (item.name().equals(name)) {
                return item.message;
            }
        }
        return name;
    }

    @Override
    public String toString() {
        return "[" + this.code + "]" + this.message;
    }

}
