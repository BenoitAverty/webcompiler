package com.baverty.webcompiler.domain.enumtypes;

import java.security.InvalidParameterException;

import lombok.Getter;

public enum OutputStreamType {
	STDIN(0), STDOUT(1), STDERR(2);
	
	@Getter
	private int descriptor;
	
	private OutputStreamType(int d) {
		descriptor = d;
	}
	
	public static OutputStreamType fromOrdinal(int ordinal) {
		switch (ordinal) {
		case 0:
			return STDIN;
		case 1:
			return STDOUT;
		case 2:
			return STDERR;
		default:
			throw new InvalidParameterException("There is no OutputStreamType with ordinal "+ordinal);	
		}
	}
}
