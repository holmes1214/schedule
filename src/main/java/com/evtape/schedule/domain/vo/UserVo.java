package com.evtape.schedule.domain.vo;

import com.evtape.schedule.domain.Permission;
import com.evtape.schedule.domain.Role;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Set;

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
    private String districtName;

    /**
     * 站点表id
     */
    private Integer stationId;

    /**
     * 岗位表id
     */
    private Integer positionId;

    /**
     * 用户名
     */
    private String userName;


    private List<Role> roles;

    private Set<Permission> permissions;
}
