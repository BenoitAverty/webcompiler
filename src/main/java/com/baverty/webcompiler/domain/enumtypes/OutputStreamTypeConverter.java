package com.baverty.webcompiler.domain.enumtypes;

import javax.persistence.AttributeConverter;

/**
 * Converter from the enum type {@link OutputStreamType} to an integer value stored in
 * database.
 *
 */
public class OutputStreamTypeConverter implements AttributeConverter<OutputStreamType, Integer> {
	/**
	 * Convert a ProgramStatus to an integer to store into the DB.
	 */
	@Override
	public Integer convertToDatabaseColumn(OutputStreamType ps) {
		return ps.getDescriptor();
	}

	/**
	 * Get the correct status corresponding to an integer record in database.
	 */
	@Override
	public OutputStreamType convertToEntityAttribute(Integer db) {
		return OutputStreamType.fromOrdinal(db);
	}
}
