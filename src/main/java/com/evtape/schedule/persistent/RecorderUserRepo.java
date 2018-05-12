package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.RecorderUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RecorderUserRepo extends JpaRepository<RecorderUser, Long>{
    RecorderUser findByPhoneNumber(String phoneNumber);

    List<RecorderUser> findByTenantId(long tenantId);

    @Query("from RecorderUser where isAdmin > 0")
    List<RecorderUser> getActiveUsers();
}
