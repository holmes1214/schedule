package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.SysConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysConfigRepo extends JpaRepository<SysConfig, Long>{

    SysConfig findByConfigKey(String configKey);
}
