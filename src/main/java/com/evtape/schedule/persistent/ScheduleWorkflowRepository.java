package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.ScheduleWorkFlow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleWorkflowRepository extends JpaRepository<ScheduleWorkFlow, Integer> {

    List<ScheduleWorkFlow> findBySuiteIdAndClassId(Integer dutySuiteId, Integer dutyClassId);
}
