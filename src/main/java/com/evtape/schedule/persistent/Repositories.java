package com.evtape.schedule.persistent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Repositories {

	public static DistrictRepository districtRepository;

	public static StationRepository stationRepository;

	public static PositionRepository positionRepository;

	public static UserRepository userRepository;

	@Autowired
	public void setDistrictRepository(DistrictRepository districtRepository) {
		Repositories.districtRepository = districtRepository;
	}

	@Autowired
	public void setStationRepository(StationRepository stationRepository) {
		Repositories.stationRepository = stationRepository;
	}

	@Autowired
	public void setPositionRepository(PositionRepository positionRepository) {
		Repositories.positionRepository = positionRepository;
	}

	@Autowired
	public void setUserRepository(UserRepository repository) {
		Repositories.userRepository = repository;
	}
}
