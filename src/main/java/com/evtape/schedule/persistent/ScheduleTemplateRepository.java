package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.ScheduleTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleTemplateRepository extends JpaRepository<ScheduleTemplate, Integer> {

    ScheduleTemplate findBySuiteIdAndWeekNumAndDayNum(Integer dutySuiteId, Integer weekNum, Integer dayNum);

    long countBySuiteIdAndClassId(Integer dutySuiteId, Integer dutyClassId);

    List<ScheduleTemplate> findBySuiteIdOrderByOrderIndex(Integer suiteId);
    
    List<ScheduleTemplate> findBySuiteId(Integer suiteId);
    
}
