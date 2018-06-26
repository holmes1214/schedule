package com.evtape.schedule.domain.vo;

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


    private List<String> roles;

    private Set<String> permissions;
}
