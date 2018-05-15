package com.evtape.schedule.support.domain;


import com.evtape.schedule.support.service.ScheduleCalculator;

import java.util.List;

/**
 * Created by holmes1214 on 06/06/2017.
 */
public class TaskSuite {
    final int priBefore;
    final int priAfter;
    final Task[] tasks;
    final PersonalDuty p;
    List<TaskSuite> available;


    public TaskSuite(PersonalDuty p) {
        this.p = p;
        int maxDay = 0, minDay = Integer.MAX_VALUE;
        tasks = new Task[ScheduleCalculator.WEEK_DAYS];
        for (Integer day : p.workingMap.keySet()
                ) {
            tasks[day] = p.workingMap.get(day);
            if (day > maxDay) {
                maxDay = day;
            }
            if (day < minDay) {
                minDay = day;
            }
        }
        Task t = tasks[maxDay];
        priAfter = t.priAfter;
        priBefore = tasks[minDay].priBefore + ScheduleCalculator.WEEK_DAYS * ScheduleCalculator.DAY_MINUTES;
    }

    public int getPriBefore() {
        return priBefore;
    }

    public int getPriAfter() {
        return priAfter;
    }

    public Task[] getTasks() {
        return tasks;
    }

    public PersonalDuty getP() {
        return p;
    }

    public List<TaskSuite> getAvailable() {
        return available;
    }

    public void setAvailable(List<TaskSuite> available) {
        this.available = available;
    }
}
