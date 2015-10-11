package com.baverty.webcompiler.domain;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;

import com.baverty.webcompiler.domain.enumtypes.ProgramStatus;
import com.baverty.webcompiler.domain.enumtypes.ProgramStatusConverter;

import lombok.Data;

/**
 * A program managed by the application.
 * 
 * Contains information about a program (Source Code, Language, ...) and
 * information about its lifecycle (status, compilation output...)
 *
 */
@Entity
@Data
public class Program implements Serializable {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 6135137973275371882L;

	/**
	 * Technical ID of the program.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long tid;

	/**
	 * Status of the program.
	 * 
	 * Stored in database as an int
	 * 
	 * @see ProgramStatus
	 * @see ProgramStatusConverter
	 */
	@Column
	@Convert(converter = ProgramStatusConverter.class)
	private ProgramStatus status;

	/**
	 * Id of the container assigned to this program.
	 */
	@Column
	@Length(max=64)
	private String containerId;

	/**
	 * SourceCode of the program.
	 */
	@Column
	@Type(type="text")
	private String sourceCode;

	/**
	 * Result of the compilation of the program.
	 */
	@Column
	@Type(type="text")
	private String compilationOutput;
	
	/**
	 * Executions of this program.
	 */
	@Column
	@OneToMany(fetch=FetchType.LAZY, mappedBy="program")
	private Set<Execution> executions;
	

}
