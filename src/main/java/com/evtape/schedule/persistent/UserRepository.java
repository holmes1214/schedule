package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer>{
    List<User> findByDistrictId(Integer scheduleInfoId);

    List<User> findByDistrictIdAndStationId(Integer scheduleInfoId, Integer leaveHours);

}
