package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.DutyPeriodChecking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DutyPeriodCheckingRepository extends JpaRepository<DutyPeriodChecking, Integer>{
}
