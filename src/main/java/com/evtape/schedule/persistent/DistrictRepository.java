package com.evtape.schedule.persistent;

import org.springframework.data.jpa.repository.JpaRepository;

import com.evtape.schedule.domain.District;
import org.springframework.stereotype.Repository;

@Repository
public interface DistrictRepository extends JpaRepository<District, Integer> {

	// findbyid用getone
	// insert和update用saveAndFlush
	// 查找全部用findAll
	// 刪除用deleteInBatch
}
