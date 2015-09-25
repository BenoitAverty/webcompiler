package com.baverty.webcompiler.controllers;

import javax.inject.Inject;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.baverty.webcompiler.controllers.responseobjects.ExecutionResponse;
import com.baverty.webcompiler.domain.Execution;
import com.baverty.webcompiler.domain.enumtypes.ExecutionStatus;
import com.baverty.webcompiler.repositories.ExecutionsRepository;

@RestController
public class ExecutionsController {

	/**
	 * Repository for Executions persistence.
	 */
	@Inject
	private ExecutionsRepository executionsRepository;
	
	/**
	 * Retrieve the result of an execution.
	 * 
	 * @param tid The technical id of the execution to retrieve.
	 * @return an object of type {@link ExecutionResponse} representing the result of the execution of the program.
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/executions/{tid}")
	public ExecutionResponse getExecution(@PathVariable long tid) {
		
		Execution e = executionsRepository.findOne(tid);
		
		ExecutionResponse response = new ExecutionResponse();
		response.status = e.getStatus();
		
		if(response.status == ExecutionStatus.EXECUTED || response.status == ExecutionStatus.EXECUTION_ERROR) {
			response.output = e.getOutput();
		}
		
		return response;
	}
}
