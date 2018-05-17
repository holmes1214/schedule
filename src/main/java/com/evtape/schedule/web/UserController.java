package com.evtape.schedule.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.evtape.schedule.consts.ResultCode;
import com.evtape.schedule.consts.ResultMap;
import com.evtape.schedule.domain.User;
import com.evtape.schedule.persistent.Repositories;

/**
 * @author ripper 用戶列表
 */
@Controller
@RequestMapping("/user")
public class UserController {

	@ResponseBody
	@RequestMapping(value = "/list", method = { RequestMethod.POST, RequestMethod.GET })
	public ResultMap userList(@RequestParam("districtId") Integer districtId,
			@RequestParam("stationId") Integer stationId, @RequestParam("positionId") Integer positionId) {
		ResultMap resultMap;
		try {
			resultMap = new ResultMap(ResultCode.SUCCESS);
			resultMap.setData(Repositories.userRepository.findByDistrictIdAndStationIdAndPositionId(districtId,
					stationId, positionId));
		} catch (Exception e) {
			resultMap = new ResultMap(ResultCode.SERVER_ERROR);
		}
		return resultMap;
	}

	@ResponseBody
	@RequestMapping(value = "/addUser", method = { RequestMethod.POST, RequestMethod.GET })
	public ResultMap addUser(@RequestParam("backup") Integer backup, @RequestParam("districtId") Integer districtId,
			@RequestParam("districtName") Integer districtName, @RequestParam("idCardNumber") Integer idCardNumber,
			@RequestParam("passWord") Integer passWord, @RequestParam("positionId") Integer positionId,
			@RequestParam("stationId") Integer stationId, @RequestParam("stationName") Integer stationName,
			@RequestParam("birthday") String birthday, @RequestParam("certLevel") String certLevel,
			@RequestParam("certNo") String certNo, @RequestParam("eduBackGround") String eduBackGround,
			@RequestParam("employeeCard") String employeeCard, @RequestParam("entryDate") String entryDate,
			@RequestParam("gender") String gender, @RequestParam("hasChild") String hasChild,
			@RequestParam("homeAddress") String homeAddress, @RequestParam("isMarried") String isMarried,
			@RequestParam("isPartyMember") String isPartyMember, @RequestParam("joinDate") String joinDate,
			@RequestParam("phoneNumber") String phoneNumber, @RequestParam("positionName") String positionName,
			@RequestParam("userName") String userName) {

		ResultMap resultMap;
		User user = new User();
		try {
			resultMap = new ResultMap(ResultCode.SUCCESS);
			user.setBackup(backup);
			user.setDistrictId(districtId);
			user.setDistrictName(districtName);
			user.setIdCardNumber(idCardNumber);
			user.setPassWord(passWord);
			user.setPositionId(positionId);
			user.setStationId(stationId);
			user.setStationName(stationName);
			user.setBirthday(birthday);
			user.setCertLevel(certLevel);
			user.setCertNo(certNo);
			user.setEduBackGround(eduBackGround);
			user.setEmployeeCard(employeeCard);
			user.setEntryDate(entryDate);
			user.setGender(gender);
			user.setHasChild(hasChild);
			user.setHomeAddress(homeAddress);
			user.setIsMarried(isMarried);
			user.setIsPartyMember(isPartyMember);
			user.setJoinDate(joinDate);
			user.setPhoneNumber(phoneNumber);
			user.setPositionName(positionName);
			user.setUserName(userName);
			Repositories.userRepository.saveAndFlush(user);
		} catch (Exception e) {
			resultMap = new ResultMap(ResultCode.SERVER_ERROR);
		}
		return resultMap;
	}

	@ResponseBody
	@RequestMapping(value = "/updateUser", method = { RequestMethod.POST, RequestMethod.GET })
	public ResultMap updateUser(@RequestParam("backup") Integer backup, @RequestParam("districtId") Integer districtId,
			@RequestParam("districtName") Integer districtName, @RequestParam("idCardNumber") Integer idCardNumber,
			@RequestParam("passWord") Integer passWord, @RequestParam("positionId") Integer positionId,
			@RequestParam("stationId") Integer stationId, @RequestParam("stationName") Integer stationName,
			@RequestParam("birthday") String birthday, @RequestParam("certLevel") String certLevel,
			@RequestParam("certNo") String certNo, @RequestParam("eduBackGround") String eduBackGround,
			@RequestParam("employeeCard") String employeeCard, @RequestParam("entryDate") String entryDate,
			@RequestParam("gender") String gender, @RequestParam("hasChild") String hasChild,
			@RequestParam("homeAddress") String homeAddress, @RequestParam("isMarried") String isMarried,
			@RequestParam("isPartyMember") String isPartyMember, @RequestParam("joinDate") String joinDate,
			@RequestParam("phoneNumber") String phoneNumber, @RequestParam("positionName") String positionName,
			@RequestParam("userName") String userName, @RequestParam("id") Integer id) {
		ResultMap resultMap;
		User user = new User();
		try {
			resultMap = new ResultMap(ResultCode.SUCCESS);
			user.setId(id);
			user.setBackup(backup);
			user.setDistrictId(districtId);
			user.setDistrictName(districtName);
			user.setIdCardNumber(idCardNumber);
			user.setPassWord(passWord);
			user.setPositionId(positionId);
			user.setStationId(stationId);
			user.setStationName(stationName);
			user.setBirthday(birthday);
			user.setCertLevel(certLevel);
			user.setCertNo(certNo);
			user.setEduBackGround(eduBackGround);
			user.setEmployeeCard(employeeCard);
			user.setEntryDate(entryDate);
			user.setGender(gender);
			user.setHasChild(hasChild);
			user.setHomeAddress(homeAddress);
			user.setIsMarried(isMarried);
			user.setIsPartyMember(isPartyMember);
			user.setJoinDate(joinDate);
			user.setPhoneNumber(phoneNumber);
			user.setPositionName(positionName);
			user.setUserName(userName);
			Repositories.userRepository.saveAndFlush(user);
		} catch (Exception e) {
			resultMap = new ResultMap(ResultCode.SERVER_ERROR);
		}
		return resultMap;
	}

	@ResponseBody
	@RequestMapping(value = "/deleteUser", method = { RequestMethod.POST, RequestMethod.GET })
	public ResultMap deleteUser(@RequestParam("id") Integer id) {
		ResultMap resultMap;
		try {
			resultMap = new ResultMap(ResultCode.SUCCESS);
			Repositories.userRepository.delete(id);
		} catch (Exception e) {
			resultMap = new ResultMap(ResultCode.SERVER_ERROR);
		}
		return resultMap;
	}

	@ResponseBody
	@RequestMapping(value = "/backupList", method = { RequestMethod.POST, RequestMethod.GET })
	public ResultMap backupList(@RequestParam("districtId") Integer districtId) {
		ResultMap resultMap;
		try {
			resultMap = new ResultMap(ResultCode.SUCCESS);
			resultMap.setData(Repositories.userRepository.findByDistrictIdAndBackup(districtId, 1));
		} catch (Exception e) {
			resultMap = new ResultMap(ResultCode.SERVER_ERROR);
		}
		return resultMap;
	}
	@ResponseBody
	@RequestMapping(value = "/addUserExcel", method = { RequestMethod.POST, RequestMethod.GET })
	public ResultMap addExcelUser(@RequestParam("excelfile") MultipartFile excelfile) {
		ResultMap resultMap;
		List<User> list= new ArrayList<User>();
		Integer totalnum;
		Integer successnum;
		Integer failnum;
		
		try {
			resultMap = new ResultMap(ResultCode.SUCCESS);
			Repositories.userRepository.save(list);
		} catch (Exception e) {
			resultMap = new ResultMap(ResultCode.SERVER_ERROR);
		}
		return resultMap;
	}

}
