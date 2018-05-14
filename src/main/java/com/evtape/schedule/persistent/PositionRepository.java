package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.Position;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionRepository extends JpaRepository<Position, Integer>{
}
