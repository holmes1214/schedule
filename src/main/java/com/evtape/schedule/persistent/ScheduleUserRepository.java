package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.ScheduleUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleUserRepository extends JpaRepository<ScheduleUser, Integer> {

}
