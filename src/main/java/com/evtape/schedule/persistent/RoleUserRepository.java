package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.RoleUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleUserRepository extends JpaRepository<RoleUser, Integer> {
    List<RoleUser> findByUserId(Integer userId);
}