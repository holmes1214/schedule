package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {

	// findbyid用getone
	// insert和update用saveAndFlush
	// 查找全部用findAll
	
	List<User> findById(Integer id);

	User findByUserName(String username);

	List<User> findByDistrictId(Integer districtId);

	List<User> findByDistrictIdAndStationId(Integer districtId, Integer stationId);

	List<User> findByDistrictIdAndStationIdAndPositionId(Integer districtId, Integer stationId, Integer positionId);

	List<User> findByDistrictIdAndBackup(Integer districtId,Integer backup);

}
