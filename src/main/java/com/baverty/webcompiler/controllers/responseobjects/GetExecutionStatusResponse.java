package com.baverty.webcompiler.controllers.responseobjects;

import com.baverty.webcompiler.controllers.ExecutionsController;
import com.baverty.webcompiler.domain.enumtypes.ExecutionStatus;

/**
 * Response object for GET /executions/(id)/status endpoint.
 * 
 * @see ExecutionsController#getExecutionStatus(long)
 *
 */
public class GetExecutionStatusResponse {
	public ExecutionStatus status;
	public String compilationOutput;
}
