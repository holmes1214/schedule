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
@Table(name = "sys_operation_log", indexes = { @Index(name = "IDX_DISTRICT_ID", columnList = "districtId"),
		@Index(name = "IDX_PHONE_NUMBER", columnList = "phoneNumber") })
public class OperationLog {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	/**
	 * 站区id
	 */
	private Integer districtId;
	/**
	 * 操作人手机号
	 */
	private String phoneNumber;
	/**
	 * 操作人姓名
	 */
	private String operatorName;
	/**
	 * 操作名称
	 */
	private String operationName;
	/**
	 * 操作状态
	 */
	private Integer operationState;
	private Date createDate;
	/**
	 * 备注
	 */
	private String content;
	@Transient
	private long beginTime;
}
