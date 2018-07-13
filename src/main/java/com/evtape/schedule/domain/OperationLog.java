package com.evtape.schedule.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * 操作日志
 */
@Entity
@Getter
@Setter
@Table(name = "sys_operation_log")
public class OperationLog {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	private Integer districtId;
	private String phoneNumber;
	private String operatorName;
	private String operationName;
	private Integer operationState;
	private Date createDate;
	private String content;
	@Transient
	private long beginTime;
}
