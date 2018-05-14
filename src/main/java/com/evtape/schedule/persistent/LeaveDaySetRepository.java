package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.LeaveDaySet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveDaySetRepository extends JpaRepository<LeaveDaySet, Integer>{
}
