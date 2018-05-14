package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Integer>{
}
