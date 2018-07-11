package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.UserHolidayLimit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Repositories {

	public static DistrictRepository districtRepository;
	public static DutyClassRepository dutyClassRepository;
	public static DutyPeriodCheckingRepository dutyPeriodCheckingRepository;
	public static DutySuiteRepository dutySuiteRepository;
	public static LeaveDaySetRepository leaveDaySetRepository;
	public static PermissionRepository permissionRepository;
	public static PositionRepository positionRepository;
	public static RolePermissionRepository rolePermissionRepository;
	public static RoleRepository roleRepository;
	public static ScheduleInfoRepository scheduleInfoRepository;
	public static ScheduleLeaveRepository scheduleLeaveRepository;
	public static ScheduleTemplateRepository scheduleTemplateRepository;
	public static ScheduleUserRepository scheduleUserRepository;
	public static ScheduleWorkflowRepository workflowRepository;
	public static ScheduleWorkflowContentRepository contentRepository;
	public static StationRepository stationRepository;
	public static UserRepository userRepository;
	public static UserHolidayLimitRepository holidayLimitRepository;

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
	@Autowired
	public void setDutyClassRepository(DutyClassRepository dutyClassRepository) {
		Repositories.dutyClassRepository = dutyClassRepository;
	}

	@Autowired
	public void setDutyPeriodCheckingRepository(DutyPeriodCheckingRepository dutyPeriodCheckingRepository) {
		Repositories.dutyPeriodCheckingRepository = dutyPeriodCheckingRepository;
	}

	@Autowired
	public void setDutySuiteRepository(DutySuiteRepository dutySuiteRepository) {
		Repositories.dutySuiteRepository = dutySuiteRepository;
	}

	@Autowired
	public void setLeaveDaySetRepository(LeaveDaySetRepository leaveDaySetRepository) {
		Repositories.leaveDaySetRepository = leaveDaySetRepository;
	}

	@Autowired
	public void setPermissionRepository(PermissionRepository permissionRepository) {
		Repositories.permissionRepository = permissionRepository;
	}

	@Autowired
	public void setRolePermissionRepository(RolePermissionRepository rolePermissionRepository) {
		Repositories.rolePermissionRepository = rolePermissionRepository;
	}

	@Autowired
	public void setRoleRepository(RoleRepository roleRepository) {
		Repositories.roleRepository = roleRepository;
	}

	@Autowired
	public void setScheduleInfoRepository(ScheduleInfoRepository scheduleInfoRepository) {
		Repositories.scheduleInfoRepository = scheduleInfoRepository;
	}

	@Autowired
	public void setScheduleLeaveRepository(ScheduleLeaveRepository scheduleLeaveRepository) {
		Repositories.scheduleLeaveRepository = scheduleLeaveRepository;
	}

	@Autowired
	public void setScheduleTemplateRepository(ScheduleTemplateRepository scheduleTemplateRepository) {
		Repositories.scheduleTemplateRepository = scheduleTemplateRepository;
	}

	@Autowired
	public void setScheduleUserRepository(ScheduleUserRepository scheduleUserRepository) {
		Repositories.scheduleUserRepository = scheduleUserRepository;
	}

	@Autowired
	public void setWorkflowRepository(ScheduleWorkflowRepository workflowRepository) {
		Repositories.workflowRepository = workflowRepository;
	}

	@Autowired
	public void setContentRepository(ScheduleWorkflowContentRepository contentRepository) {
		Repositories.contentRepository = contentRepository;
	}
	@Autowired
	public void setHolidayLimitRepository(UserHolidayLimitRepository repository) {
		Repositories.holidayLimitRepository = repository;
	}
}
