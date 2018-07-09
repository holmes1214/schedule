package com.evtape.schedule.domain.form;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ScheduleUserForm {
	private Integer suiteId;
	private Integer classId;
	private Integer weekNum;
	private Integer dayNum;
	private Integer userId;
}
