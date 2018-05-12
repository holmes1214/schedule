package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.SysTenant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysTenantRepo extends JpaRepository<SysTenant, Long>{
	
}
