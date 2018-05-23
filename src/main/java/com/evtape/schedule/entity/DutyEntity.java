package com.evtape.schedule.entity;

import java.util.List;

import com.evtape.schedule.domain.DutyClass;

import lombok.Getter;
import lombok.Setter;

/**
 * 站区
 */
@Getter
@Setter
public class DutyEntity {
	Integer suiteId;
	
	List<DutyClass> updatelist;
	
	List<Integer> deletelist;

}
