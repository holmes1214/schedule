package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.LeaveDaySet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveDaySetRepository extends JpaRepository<LeaveDaySet, Integer>{
    LeaveDaySet findByLeaveTypeAndSubType(int i, int i1);
}
