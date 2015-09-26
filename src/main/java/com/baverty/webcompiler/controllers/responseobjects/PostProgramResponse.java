package com.baverty.webcompiler.controllers.responseobjects;

import com.baverty.webcompiler.controllers.ProgramsController;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Response object for POST /programs endpoint.
 * 
 * @see ProgramsController#postProgram(String)
 *
 */
@JsonInclude(Include.NON_EMPTY)
public class PostProgramResponse {
	public RequestStatus status;
	public long programId;
}
