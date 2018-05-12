package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.RecordedSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RecordedSegmentRepository extends JpaRepository<RecordedSegment,Long>{
	
	@Query("FROM RecordedSegment WHERE createUserId = ?1 ORDER BY id DESC")
	public List<RecordedSegment> findByCreateUserId(Long userId);

	@Query("select count(r) FROM RecordedSegment r WHERE r.urlId=?1 and r.status=?2 and r.createUserId=?3")
	long countByUrlIdAndCreateUserId(Long urlId,Integer status,Long createUserId);

	@Query("select r FROM RecordedSegment r WHERE r.urlId=?1 and r.status=?2 and r.createUserId=?3")
	List<RecordedSegment> findByUrlIdAndCreateUserId(Long urlId,Integer status,Long createUserId);

    List<RecordedSegment> findByCreateUserIdAndStatus(Long createUserId, int ready);

    RecordedSegment findByFileKey(String fileKey);

    @Query(nativeQuery = true,value = "select sum(file_size) s from recorded_segment where create_user_id=?1 and" +
			" DATE(uploaded_date) >= ?2 and DATE(uploaded_date) <= ?3 " +
			"GROUP BY DATE(uploaded_date) ORDER BY s desc limit 1")
	List<Number> getUserMaxStorage(Long id, String from, String to);
	@Query(nativeQuery = true,value = "select sum(file_size) s from recorded_segment where tenant_id=?1 and" +
			" DATE(uploaded_date) >= ?2 and DATE(uploaded_date) <= ?3 " +
			"GROUP BY DATE(uploaded_date) ORDER BY s desc limit 1")
	List<Number> getTenentMaxStorage(Long id, String from, String to);
}
