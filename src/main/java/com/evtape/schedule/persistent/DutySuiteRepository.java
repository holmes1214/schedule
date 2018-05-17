package com.evtape.schedule.persistent;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.evtape.schedule.domain.DutySuite;

public interface DutySuiteRepository extends JpaRepository<DutySuite, Integer> {

	List<DutySuite> findByDistrictIdAndStationIdAndPositionId(Integer districtId, Integer stationId,
			Integer positionId);

}
