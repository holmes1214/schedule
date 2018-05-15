package com.evtape.schedule.support.domain;

import com.evtape.schedule.domain.DutySuite;
import com.evtape.schedule.domain.Position;
import com.evtape.schedule.domain.User;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class CalculateResult {
	private DutySuite model;
	private Position post;
	private String startFrom;
	private String endAt;
	private int totalDays;
	private String modelId;
	private Map<Integer, List<Task>> taskMap;
	private List<User> userList;
	private String groupName;
}
