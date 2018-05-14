package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Integer>{
}
