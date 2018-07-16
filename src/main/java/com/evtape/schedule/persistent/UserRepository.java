package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

	User findByUserName(String userName);

	User findByPhoneNumber(String phoneNumber);

	List<User> findByDistrictId(Integer districtId);

	List<User> findByDistrictIdAndStationId(Integer districtId, Integer stationId);

	List<User> findByPhoneNumberNot(String phoneNumber);

	List<User> findByPhoneNumberNotAndEmployeeCardStartingWith(String phoneNumber, String employeeCard);

	List<User> findByPhoneNumberNotAndDistrictId(String phoneNumber, Integer districtId);

	List<User> findByPhoneNumberNotAndDistrictIdAndEmployeeCardStartingWith(String phoneNumber, Integer districtId, String employeeCard);


	List<User> findByDistrictIdAndBackup(Integer districtId,Integer backup);

	List<User> findByUserNameOrEmployeeCard(String userName, String employeeCard);

	List<User> findByDistrictIdAndRoleId(Integer districtId, int i);

    User findByEmployeeCode(String code);

	User findByIdCardNumber(String identity);
}
