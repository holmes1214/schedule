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
}
