package com.evtape.schedule.domain.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by lianhai on 2018/6/14.
 */
@Getter
@Setter
@ToString
public class UserVo {
    /**
     * 站区表id
     */
    private Integer districtId;

    /**
     * 站区名
     */
    private Integer districtName;
    /**
     * 站点表id
     */
    private Integer stationId;

    /**
     * 站点名
     */
    private Integer stationName;

    /**
     * 岗位表id
     */
    private Integer positionId;

    /**
     * 岗位名，身份：管理员、普通职工
     */
    private String positionName;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 电话号，用于登录
     */
    private String phoneNumber;

    /**
     * 身份证号
     */
    private Integer idCardNumber;

    /**
     * 生日
     */
    private String birthday;

    /**
     * 性别男女0男1女
     */
    private String gender;

    /**
     * 入职时间
     */
    private String entryDate;

    /**
     * 未婚已婚
     */
    private String isMarried;

    /**
     * 已育未育
     */
    private String hasChild;

    /**
     * 员工卡号
     */
    private String employeeCard;

    /**
     * 学历，高中以下，本科，专科，研究生，博士
     */
    private String eduBackGround;
    /**
     * 是否党员，群众共产党员，共青团员
     */
    private String isPartyMember;
    /**
     * 入党入团时间
     */
    private String joinDate;

    /**
     * 家庭住址
     */
    private String homeAddress;

    /**
     * 站务员证书编号
     */
    private String certNo;

    /**
     * 站务员证书等级，站务初级
     */
    private String certLevel;

    /**
     * 消防证书编号
     */
    private String xfzNo;

    /**
     * 综控员证书编号
     */
    private String zwyNo;

    /**
     * 综控员证书级别
     */
    private String zwyLevel;
    /**
     * 是否是补位人员1是0不是，默认0
     */
    private Integer backup;
}