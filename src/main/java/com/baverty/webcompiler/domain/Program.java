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
import javax.persistence.OneToMany;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;

import com.baverty.webcompiler.domain.enumtypes.ProgramStatus;
import com.baverty.webcompiler.domain.enumtypes.ProgramStatusConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * A execution managed by the application.
 * 
 * Contains information about a execution (Source Code, Language, ...) and
 * information about its lifecycle (status, compilation output...)
 *
 */
@Entity
@Data
@EqualsAndHashCode(exclude = { "compilationOutput", "executions" })
public class Program {

	/**
	 * Technical ID of the execution.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long tid;

	/**
	 * Status of the execution.
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
	 * Id of the container assigned to this execution.
	 */
	@Column
	@Length(max = 64)
	private String containerId;

	/**
	 * SourceCode of the execution.
	 */
	@Column
	@Type(type = "text")
	private String sourceCode;

	/**
	 * Result of the compilation of the execution.
	 */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "program")
	@JsonIgnore
	private Set<OutputChunk> compilationOutput;

	/**
	 * Executions of this execution.
	 */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "program", cascade = { CascadeType.ALL })
	@JsonIgnore
	private Set<Execution> executions;

	/**
	 * Returns the output of the compilation as a string.
	 * 
	 * The output of this method doesn't have any information about chunks and their type (stdin, stdout, stderr).
	 */
	public String getCompilationOutput() {
		return compilationOutput.stream()
				.sorted((c1, c2) -> c1.getIndex().compareTo(c2.getIndex()))
				.map(c -> c.getContent())
				.reduce((s1, s2) -> s1+s2)
				.orElse("");
	}

}
