package com.baverty.webcompiler.controllers.responseobjects;

import com.baverty.webcompiler.controllers.ProgramsController;
import com.baverty.webcompiler.domain.enumtypes.ProgramStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Response object for GET /program/(id)/status endpoint.
 * 
 * @see ProgramsController#getProgramStatus(Long)
 *
 */
@JsonInclude(Include.NON_EMPTY)
public class GetProgramStatusResponse {
	public ProgramStatus status;
	public String compilationOutput;
}
