package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.UserHolidayLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface UserHolidayLimitRepository extends JpaRepository<UserHolidayLimit, Integer> {

}
