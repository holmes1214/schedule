package com.evtape.schedule.persistent;

import com.evtape.schedule.domain.SysRecordedFiles;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysRecordedFilesRepo extends JpaRepository<SysRecordedFiles,Long>{
	
}
