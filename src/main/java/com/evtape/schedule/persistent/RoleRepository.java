package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer>{
    List<Role> findByCodeNot(String code);
}
