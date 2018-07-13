package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.District;
import com.evtape.schedule.domain.OperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Integer> {

}
