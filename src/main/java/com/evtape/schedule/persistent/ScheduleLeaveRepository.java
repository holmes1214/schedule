package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.ScheduleLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleLeaveRepository extends JpaRepository<ScheduleLeave, Integer> {

    List<ScheduleLeave> findByScheduleInfoId(Integer scheduleInfoId);

    @Modifying
    @Query("delete from ScheduleLeave where scheduleInfoId=?1")
    void deleteByScheduleInfoId(Integer scheduleInfoId);

    @Query("select count(1) from ScheduleLeave where leaveType=1 and subType=1 and userId=?1 and leaveDateStr >= ?2")
    long countAnnualLeave(Integer userId, String dateStr);

    @Query("select count(1) from ScheduleLeave where leaveType=2 and subType=1 and userId=?1 and leaveDateStr >= ?2")
    long countSickLeave(Integer userId, String dateStr);
}
