package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Integer>{
}
