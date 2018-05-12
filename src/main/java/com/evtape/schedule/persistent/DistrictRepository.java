package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.District;
import com.evtape.schedule.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DistrictRepository extends JpaRepository<District, Integer>{
}
