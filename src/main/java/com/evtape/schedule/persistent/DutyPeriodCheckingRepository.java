package com.evtape.schedule.persistent;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.evtape.schedule.domain.DutyPeriodChecking;
import org.springframework.stereotype.Repository;

@Repository
public interface DutyPeriodCheckingRepository extends JpaRepository<DutyPeriodChecking, Integer>{
	
	List<DutyPeriodChecking> findBySuiteId(Integer suiteId);
}
