package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.RoleUser;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleUserRepository extends JpaRepository<RoleUser, Integer> {
    List<RoleUser> findByUserId(Integer userId);

    RoleUser findByUserIdAndRoleId(Integer userId, Integer roleId);

}