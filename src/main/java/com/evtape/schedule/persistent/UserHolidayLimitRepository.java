package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.UserHolidayLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserHolidayLimitRepository extends JpaRepository<UserHolidayLimit, Integer> {

    List<UserHolidayLimit> findByYearStr(String year);

    UserHolidayLimit findByYearStrAndUserId(String s, Integer userId);
}

