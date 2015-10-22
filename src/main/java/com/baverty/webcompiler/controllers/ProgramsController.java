package com.baverty.webcompiler.controllers;

import javax.inject.Inject;

import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.baverty.webcompiler.controllers.responseobjects.PostProgramResponse;
import com.baverty.webcompiler.controllers.responseobjects.PostProgramExecutionResponse;
import com.baverty.webcompiler.controllers.responseobjects.GetProgramStatusResponse;
import com.baverty.webcompiler.controllers.responseobjects.RequestStatus;
import com.baverty.webcompiler.domain.Execution;
import com.baverty.webcompiler.domain.Program;
import com.baverty.webcompiler.domain.enumtypes.ExecutionStatus;
import com.baverty.webcompiler.domain.enumtypes.ProgramStatus;
import com.baverty.webcompiler.repositories.ExecutionsRepository;
import com.baverty.webcompiler.repositories.ProgramsRepository;
import com.baverty.webcompiler.services.CompilationService;
import com.baverty.webcompiler.services.ExecutionService;

@RestController
public class ProgramsController {

	/***
	 * Repository for Program persistence.
	 */
	@Inject
	private ProgramsRepository programsRepository;
	
	/***
	 * Repository for Executions persistence.
	 */
	@Inject
	private ExecutionsRepository executionsRepository;

	/***
	 * Service used to compile programs.
	 */
	@Inject
	private CompilationService compilationService;
	
	/***
	 * Service used to execute programs.
	 */
	@Inject
	private ExecutionService executionService;

	/**
	 * Create a execution.
	 * 
	 * If the execution needs to be compiled, the compilation will be launched
	 * asynchronously by this webservice.
	 * 
	 * @param body
	 *            The body of the request should contain the source code to
	 *            compile
	 * @return A unique ID representing the execution in the system. The id will
	 *         be used for all further interactions with the API
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/programs")
	public PostProgramResponse postProgram(@RequestBody String body) {

		Program p = new Program();
		PostProgramResponse response = new PostProgramResponse();
		
		try {
			p.setStatus(ProgramStatus.NEW);
			p.setSourceCode(body);
			programsRepository.save(p);
		}
		catch(DataAccessException e) {
			response.status = RequestStatus.KO;
			response.programId = null;
			return response;
		}

		compilationService.compile(p);

		response.status = RequestStatus.OK;
		response.programId = p.getTid();
		
		return response;

	}

	/**
	 * Retrieve the status of a execution.
	 * 
	 * @param tid
	 *            The tid of the execution
	 * @return A structure containing the status and information related to the
	 *         status
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/programs/{tid}/status")
	public GetProgramStatusResponse getProgramStatus(@PathVariable Long tid) {

		Program p = programsRepository.findOne(tid);

		GetProgramStatusResponse response = new GetProgramStatusResponse();
		response.status = p.getStatus();
		response.compilationOutput = p.getCompilationOutput();

		return response;
	}

	/**
	 * Execute a execution.
	 * 
	 * @param tid
	 *            The tid of the execution
	 * @return the ID of the execution launched
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/programs/{tid}/executions")
	public PostProgramExecutionResponse postProgramExecution(@PathVariable Long tid) {

		Program p = programsRepository.findOne(tid);

		PostProgramExecutionResponse response = new PostProgramExecutionResponse();
		
		if(p.getStatus() == ProgramStatus.COMPILED) {
			
			Execution e = new Execution();
			e.setProgram(p);
			e.setStatus(ExecutionStatus.NEW);
			executionsRepository.save(e);
			
			executionService.execute(e);
			
			response.executionId = e.getTid();
			response.status = RequestStatus.OK;
		}
		else {
			response.status = RequestStatus.KO;
		}
		
		return response;
	}
}
