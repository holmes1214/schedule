package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.WorkLoadReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkLoadReportRepository extends JpaRepository<WorkLoadReport, Integer> {

}
