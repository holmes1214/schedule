package com.evtape.schedule.support.domain;

import java.util.LinkedHashMap;
import java.util.Map;

public class PersonalDuty implements Comparable<PersonalDuty> {
    public Integer userId;
    public int available = 0;
    public int total;
    public Map<Integer, Task> workingMap = new LinkedHashMap<>();


    public void addWorkingDays(int day, Task t) {
        workingMap.put(day, t);
        total += t.shift.getWorkingLength();
    }

    public void removeWorkingDays(int day) {
        Task task = workingMap.remove(day);
        total -= task.shift.getWorkingLength();
    }

    public boolean hasWork(int day) {
        return workingMap.containsKey(day);
    }

    public String toString() {
        return "userId:" + userId + ",total:" + total;
    }

    @Override
    public int compareTo(PersonalDuty o) {
        if (workingMap.size()==o.workingMap.size()){
            return total - o.total;
        }
        return workingMap.size()-o.workingMap.size();
    }
}

