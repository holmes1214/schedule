package com.evtape.schedule.domain.form;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by lianhai on 2018/5/28.
 */
@Getter
@Setter
@ToString
public class LeaveForm {
    private Integer scheduleInfoId;

    private Integer leaveType;
    private Integer instead;
    private Integer subType;
    private Double leaveCount;
    private String content;
}
