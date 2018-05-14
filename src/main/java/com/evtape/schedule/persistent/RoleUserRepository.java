package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.RoleUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleUserRepository extends JpaRepository<RoleUser, Integer>{
}
