package com.evtape.schedule.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

/**
 * 用户
 */

/**
 * @author ripper
 */
@Entity
@Getter
@Setter
@Table(name = "biz_user", indexes = {@Index(name = "IDX_DISTRICT_ID", columnList = "districtId,stationId"),
        @Index(name = "IDX_POSITION_ID", columnList = "districtId,positionId")})
public class User {
    // 包括所有用户，管理员，员工，都可以登录，查看自己的排班信息
    // 但是只有站区或站的管理员能填写请假排班等信息
    // 体现在用户角色权限三者的关联上

    /**
     * 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    /**
     * 站区表id
     */
    private Integer districtId;

    /**
     * 站区名
     */
    private String districtName;
    /**
     * 站点表id
     */
    private Integer stationId;

    /**
     * 站点名
     */
    private String stationName;

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
     * 密码
     */
    private String password;

    /**
     * 身份证号
     */
    private String idCardNumber;

    /**
     * 生日
     */
    private Date birthday;

    /**
     * 性别 0男1女
     */
    private Integer gender;

    /**
     * 入职时间
     */
    private Date entryDate;

    /**
     * 未婚已婚 0未婚 1已婚
     */
    private Integer isMarried;

    /**
     * 已育未育 0未育 1已育
     */
    private Integer hasChild;

    /**
     * 员工卡号
     */
    private String employeeCard;

    /**
     * 学历，高中以下，本科，专科，研究生，博士
     */
    private Integer eduBackGround;
    /**
     * 群众共产党员，共青团员
     */
    private Integer partyMember;
    /**
     * 入党入团时间
     */
    private Date joinDate;

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
     * 是否是补位人员 1是0不是，默认0
     */
    private Integer backup = 0;

}
