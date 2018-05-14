package com.evtape.schedule.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * 用户可以有多个角色
 */
@Entity
@Getter
@Setter
@Table(name = "sys_role_user", indexes = {
        @Index(name = "IDX_ROLE_ID", columnList = "roleId"),
        @Index(name = "ID_USER_ID", columnList = "userId")
})
public class RoleUser {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private Integer roleId;

    private Integer userId;

}
