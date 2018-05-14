package com.evtape.schedule.persistent;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.evtape.schedule.domain.Position;

public interface PositionRepository extends JpaRepository<Position, Integer> {

	// findbyid用getone
	// insert和update用saveAndFlush
	// 查找全部用findAll

	/**
	 * 根据站查找此站共有多少岗位
	 * 
	 * @param stationId
	 * @return
	 */
	List<Position> findByStationId(Integer stationId);

	/**
	 * 根据站区和站查找此站共有多少岗位
	 * 
	 * @param districtId
	 * @param stationId
	 * @return
	 */
	List<Position> findByDistrictIdAndStationId(Integer districtId, Integer stationId);

}
