package com.baverty.webcompiler.domain;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.mapping.Collection;

import com.baverty.webcompiler.domain.enumtypes.OutputStreamType;
import com.baverty.webcompiler.domain.enumtypes.OutputStreamTypeConverter;

/**
 * Represent a chunk of output of either a program compilation or an execution.
 * 
 * The output of a compilation or execution can be of several types : STDIN(0),
 * STDOUT(1) or STDERR(2). This entity represents a chunk of the output, its
 * type and an index used to reconstitute the entire output in order.
 * 
 * @author Benoit
 *
 */
@Entity
public class OutputChunk {

	/**
	 * Technical ID of the output chunk.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long tid;

	

	/**
	 * A program, the compilation of which contains this output chunk.
	 * 
	 * Mutually exclusive with {@link #execution}
	 */
	@ManyToOne
	@JoinColumn(name = "program_tid")
	private Program program;

	/**
	 * The execution of a program which contains this output chunk.
	 * 
	 * Mutually exclusive with {@link #program}
	 */
	@ManyToOne
	@JoinColumn(name = "execution_tid")
	private Execution execution;
	
	/**
	 * The type (stdin, stdout or stderr) of this output chunk.
	 */
	@Column
	@Convert(converter = OutputStreamTypeConverter.class)
	@NotNull
	private OutputStreamType type;
	
	/**
	 * The index of this output chunk amongst all the other chunks of a compilation/execution.
	 * 
	 * From 0 (first) to n (last)
	 */
	@Column
	@NotNull
	private Integer index; 

	/**
	 * The content of this output chunk.
	 */
	@Column
	@Type(type = "text")
	@NotNull
	private String content;
}
