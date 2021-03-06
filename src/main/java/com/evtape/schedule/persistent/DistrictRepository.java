package com.evtape.schedule.persistent;

import org.springframework.data.jpa.repository.JpaRepository;

import com.evtape.schedule.domain.District;
import org.springframework.stereotype.Repository;

@Repository
public interface DistrictRepository extends JpaRepository<District, Integer> {

}
