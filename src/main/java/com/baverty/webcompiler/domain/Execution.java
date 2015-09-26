package com.baverty.webcompiler.domain;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.baverty.webcompiler.domain.enumtypes.ExecutionStatus;
import com.baverty.webcompiler.domain.enumtypes.ExecutionStatusConverter;

import lombok.Data;

/**
 * An execution of a program.
 * 
 * Contains the information about the execution : status, output, ...
 *
 */
@Entity
@Data
public class Execution {

	/**
	 * Technical id of the execution.
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long tid;
	
	/**
	 * The program related to this execution.
	 */
	@ManyToOne
	private Program program;
	
	/**
	 * Status of the execution.
	 */
	@Column
	@Convert(converter = ExecutionStatusConverter.class)
	private ExecutionStatus status;
	
	/**
	 * The output of the execution.
	 */
	@Column
	private String output;
}