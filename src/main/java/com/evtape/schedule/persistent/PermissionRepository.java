package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {
    @Query(nativeQuery = true, value = "SELECT * FROM sys_permission p WHERE p.id IN ?1")
    List<Permission> queryByIds(Integer[] ids);
}
