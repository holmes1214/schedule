package com.evtape.schedule.persistent;

import org.springframework.data.jpa.repository.JpaRepository;

import com.evtape.schedule.domain.BroadcastUrl;

import java.util.List;

public interface BroadcastUrlRepository extends JpaRepository<BroadcastUrl, Long>{
	Long countByUrlString(String urlString);

	BroadcastUrl findByUrlString(String url);

    List<BroadcastUrl> findByNodeNumber(int nodeNumber);

    Long countByNodeNumber(int i);
}
