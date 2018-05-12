package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.District;
import com.evtape.schedule.domain.DutySuite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DutySuiteRepository extends JpaRepository<DutySuite, Integer>{
}
