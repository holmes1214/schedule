package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.ScheduleInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ScheduleInfoRepository extends JpaRepository<ScheduleInfo, Integer> {


    ScheduleInfo findByUserIdAndDateStr(Integer userId, String dateStr);

    @Query(value = "from ScheduleInfo where userId=?1 and scheduleDate>=?2")
    List<ScheduleInfo> findByUserWorkLeft(Integer userId, Date dateStr);

    @Query("from ScheduleInfo where scheduleDate>=?1 and scheduleDate<?2 and userId in (?3)")
    List<ScheduleInfo> findByUserIds(Date startDate, Date endDate, List<Integer> collect);

    @Query("from ScheduleInfo where scheduleDate>=?1 and userId in (?2)")
    List<ScheduleInfo> findByUserIdsNoEndDate(Date startDate, List<Integer> collect);

    @Query("from ScheduleInfo where scheduleDate>=?1 and scheduleDate<=?2 and districtId=?3")
    List<ScheduleInfo> findByCondition(Date parse, Date parse1, Integer districtId);

    @Query("from ScheduleInfo where scheduleDate>=?1 and scheduleDate<?2")
    List<ScheduleInfo> findByDate(Date begin, Date end);

    @Query("from ScheduleInfo where userId in (?1) and scheduleDate>=?2")
    List<ScheduleInfo> findByUserIdsAndDate(List<Integer> userIds, Date from);

    @Query("from ScheduleInfo where userId in (?1) and dateStr=?2")
    List<ScheduleInfo> findByUserIdsAndDateStr(List<Integer> collect, String dateStr);
}
