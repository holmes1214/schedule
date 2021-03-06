package com.evtape.schedule.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * 角色字典表
 */
@Entity
@Getter
@Setter
@Table(name = "sys_role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    /**
     * 角色编码
     */
    private String code;
    /**
     * 角色名称
     */
    private String name;
    /**
     * 角色描述
     */
    private String description;
}
