package com.evtape.schedule.persistent;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.evtape.schedule.domain.DutySuite;
import org.springframework.stereotype.Repository;

@Repository
public interface DutySuiteRepository extends JpaRepository<DutySuite, Integer> {

    List<DutySuite> findByDistrictIdAndStationIdAndPositionId(Integer districtId, Integer stationId,
                                                              Integer positionId);

    List<DutySuite> findByDistrictIdAndBackup(Integer districtId, Integer backup);
    
    List<DutySuite> findByDistrictId(Integer districtId);
    
    List<DutySuite> findByDistrictIdAndStationId(Integer districtId, Integer stationId);

}
