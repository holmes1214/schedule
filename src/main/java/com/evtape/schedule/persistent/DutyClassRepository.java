package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.DutyClass;
import com.evtape.schedule.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DutyClassRepository extends JpaRepository<DutyClass, Integer>{
}
