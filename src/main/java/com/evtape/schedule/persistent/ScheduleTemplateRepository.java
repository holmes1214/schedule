package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.ScheduleTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleTemplateRepository extends JpaRepository<ScheduleTemplate, Integer> {

}
