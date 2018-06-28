package com.evtape.schedule.domain.form;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by lianhai on 2018/6/27.
 */
@Getter
@Setter
@ToString
public class RoleForm {
    private String name;
    private String description;
    private Integer[] permissionIds;
}
