package com.evtape.schedule.persistent;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.evtape.schedule.domain.Station;

public interface StationRepository extends JpaRepository<Station, Integer> {

	// findbyid用getone
	// insert和update用saveAndFlush
	// 查找全部用findAll

	List<Station> findByDistrictId(Integer districtId);

}
