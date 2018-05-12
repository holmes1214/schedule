package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.UserUrl;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Created by holmes1214 on 21/08/2017.
 */
@Lazy(value = false)
public interface UserUrlRepository extends JpaRepository<UserUrl, Long> {

    List<UserUrl> findByStatus(int recording );

    long countByUrlString(String url);

    @Query("FROM UserUrl WHERE status >= 0 AND createUserId=?1 ORDER BY lastUploading DESC")
    List<UserUrl> findOrderByLastUploadingDesc(Long createUserId);

    List<UserUrl> findByUrlString(String urlString);

    List<UserUrl> findByCreateUserId(long userId);

    List<UserUrl> findByUrlId(Long id);
}
