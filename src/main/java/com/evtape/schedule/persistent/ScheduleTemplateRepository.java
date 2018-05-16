package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.ScheduleTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleTemplateRepository extends JpaRepository<ScheduleTemplate, Integer> {

    ScheduleTemplate findBySuiteIdAndWeekNumAndDayNum(Integer dutySuiteId, Integer dutyClassId, Integer weekNum, Integer dayNum);

    long countBySuiteIdAndClassId(Integer dutySuiteId, Integer dutyClassId);
}
