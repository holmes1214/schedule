package com.evtape.schedule.persistent;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.evtape.schedule.domain.Position;
import org.springframework.stereotype.Repository;

@Repository
public interface PositionRepository extends JpaRepository<Position, Integer> {


    List<Position> findByDistrictId(Integer districtId);
}
