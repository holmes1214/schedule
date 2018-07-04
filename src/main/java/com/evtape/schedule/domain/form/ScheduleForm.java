package com.evtape.schedule.domain.form;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ScheduleForm {
	private Integer suiteId;
	private Integer weekNum1;
	private Integer weekNum2;
	private Integer dayNum1;
	private Integer dayNum2;
}
