package com.baverty.webcompiler.controllers.responseobjects;

import com.baverty.webcompiler.controllers.ProgramsController;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Response object for POST /programs/(id)/executions endpoint.
 * 
 * @see ProgramsController#executeProgram(Long)
 *
 */
@JsonInclude(Include.NON_EMPTY)
public class ProgramExecutionResponse {
	public RequestStatus status;
	public Long executionId;
}
