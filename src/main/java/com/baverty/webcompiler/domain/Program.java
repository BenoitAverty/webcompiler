package com.baverty.webcompiler.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.baverty.webcompiler.domain.enumtypes.ProgramStatus;
import com.baverty.webcompiler.domain.enumtypes.ProgramStatusConverter;

import lombok.Data;

@Entity
@Data
public class Program implements Serializable {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 6135137973275371882L;

	/**
	 *  Technical ID of the program.
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
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
	@Convert(converter=ProgramStatusConverter.class)
	private ProgramStatus status;
	
	/**
	 * Id of the container assigned to this program. 
	 */
	@Column
	private String containerId;
	
	/**
	 * SourceCode of the program.
	 */
	@Column
	private String sourceCode;
	
	/**
	 * Result of the compilation of the program.
	 */
	@Column
	private String compilationResult;
	
}
