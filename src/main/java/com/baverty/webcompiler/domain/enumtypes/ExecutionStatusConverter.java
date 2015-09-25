package com.baverty.webcompiler.domain.enumtypes;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Converter from the enum type ExecutionStatus to an integer value stored in
 * database.
 * 
 * For now, this only uses the ordinal of the enum as we're using a in-memory DB
 * and we don't need to worry about old records.
 * 
 * If a persistent database is used and the requirements change, this class will
 * need to be updated so that old records still map to the correct status.
 *
 */
@Converter
public class ExecutionStatusConverter implements AttributeConverter<ExecutionStatus, Integer> {

	/**
	 * Convert a ProgramStatus to an integer to store into the DB.
	 */
	@Override
	public Integer convertToDatabaseColumn(ExecutionStatus es) {
		return es.ordinal();
	}

	/**
	 * Get the correct status corresponding to an integer record in database.
	 */
	@Override
	public ExecutionStatus convertToEntityAttribute(Integer db) {
		for (ExecutionStatus status : ExecutionStatus.values()) {
			if (status.ordinal() == db)
				return status;
		}

		return null;
	}

}
