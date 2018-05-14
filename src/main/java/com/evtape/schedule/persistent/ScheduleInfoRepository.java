package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.ScheduleInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleInfoRepository extends JpaRepository<ScheduleInfo, Integer> {

}
