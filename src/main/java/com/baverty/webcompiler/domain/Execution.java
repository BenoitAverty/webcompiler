package com.baverty.webcompiler.domain;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.baverty.webcompiler.domain.enumtypes.ExecutionStatus;
import com.baverty.webcompiler.domain.enumtypes.ExecutionStatusConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * An execution of a program.
 * 
 * Contains the information about the execution : status, output, ...
 *
 */
@Entity
@Data
@EqualsAndHashCode(exclude={"output"})
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
	@JoinColumn(name="program_tid")
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
	@OneToMany(fetch=FetchType.LAZY, mappedBy="execution", cascade={CascadeType.ALL})
	@JsonIgnore
	private Set<OutputChunk> output;
}
