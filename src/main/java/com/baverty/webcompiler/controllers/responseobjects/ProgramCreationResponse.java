package com.baverty.webcompiler.controllers.responseobjects;

import com.baverty.webcompiler.controllers.ProgramsController;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Response object for POST /programs endpoint.
 * 
 * @see ProgramsController#createProgram(String)
 *
 */
@JsonInclude(Include.NON_EMPTY)
public class ProgramCreationResponse {
	public RequestStatus status;
	public long programId;
}
