package com.baverty.webcompiler.controllers.responseobjects;

import com.baverty.webcompiler.controllers.ProgramsController;
import com.baverty.webcompiler.domain.enumtypes.ExecutionStatus;

/**
 * Response object for GET /executions/(id) endpoint.
 * 
 * @see ExecutionsController#getExecution(long)
 *
 */
public class ExecutionResponse {
	public ExecutionStatus status;
	public String output;
}
