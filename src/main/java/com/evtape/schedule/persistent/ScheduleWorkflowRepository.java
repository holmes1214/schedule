package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.ScheduleWorkflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleWorkflowRepository extends JpaRepository<ScheduleWorkflow, Integer> {

    List<ScheduleWorkflow> findBySuiteIdAndClassId(Integer dutySuiteId, Integer dutyClassId);

    List<ScheduleWorkflow> findBySuiteId(Integer suiteId);

    List<ScheduleWorkflow> findByDistrictId(Integer districtId);
}
