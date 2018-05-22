package com.evtape.schedule.persistent;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.evtape.schedule.domain.DutyClass;

public interface DutyClassRepository extends JpaRepository<DutyClass, Integer> {
	List<DutyClass> findBySuiteId(Integer suitId);

}
