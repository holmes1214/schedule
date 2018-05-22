package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.ScheduleWorkflow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleWorkflowRepository extends JpaRepository<ScheduleWorkflow, Integer> {

    List<ScheduleWorkflow> findBySuiteIdAndClassId(Integer dutySuiteId, Integer dutyClassId);
}
