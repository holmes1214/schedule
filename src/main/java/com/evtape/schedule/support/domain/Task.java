package com.evtape.schedule.support.domain;

import com.evtape.schedule.domain.DutyClass;

import java.util.PriorityQueue;
import java.util.Queue;

public class Task implements Cloneable {
	
    /**
     * 星期几
     */
    public int day;
    /**
     * 所在班次
     */
    public DutyClass shift;
    public Integer userId;
    public int priBefore;
    public int priAfter;
    public Task relevance;
    public Task parent;
    public Queue<PersonalDuty> userAvailable = new PriorityQueue<>();

    public String toString() {
        return "day:" + day + ",pri:" + priBefore + ", shift:" + shift.getId() + ", user:" + userId;
    }


    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}