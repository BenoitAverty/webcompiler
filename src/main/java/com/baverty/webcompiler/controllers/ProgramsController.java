package com.baverty.webcompiler.controllers;

import javax.inject.Inject;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.baverty.webcompiler.controllers.responseobjects.ProgramCreationResponse;
import com.baverty.webcompiler.controllers.responseobjects.ProgramExecutionResponse;
import com.baverty.webcompiler.controllers.responseobjects.ProgramStatusResponse;
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
	 * Create a program.
	 * 
	 * If the program needs to be compiled, the compilation will be launched
	 * asynchronously by this webservice.
	 * 
	 * @param body
	 *            The body of the request should contain the source code to
	 *            compile
	 * @return A unique ID representing the program in the system. The id will
	 *         be used for all further interactions with the API
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/programs")
	public ProgramCreationResponse createProgram(@RequestBody String body) {

		Program p = new Program();

		p.setStatus(ProgramStatus.NEW);
		p.setSourceCode(body);
		programsRepository.save(p);

		compilationService.compile(p);

		ProgramCreationResponse response = new ProgramCreationResponse();
		response.status = RequestStatus.OK;
		response.programId = p.getTid();
		
		return response;

	}

	/**
	 * Retrieve the status of a program.
	 * 
	 * @param tid
	 *            The tid of the program
	 * @return A structure containing the status and information related to the
	 *         status
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/programs/{tid}/status")
	public ProgramStatusResponse getProgramStatus(@PathVariable Long tid) {

		Program p = programsRepository.findOne(tid);

		ProgramStatusResponse response = new ProgramStatusResponse();
		response.status = p.getStatus();
		response.compilationOutput = p.getCompilationOutput();

		return response;
	}

	/**
	 * Execute a program.
	 * 
	 * @param tid
	 *            The tid of the program
	 * @return the ID of the execution launched
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/programs/{tid}/executions")
	public ProgramExecutionResponse executeProgram(@PathVariable Long tid) {

		Program p = programsRepository.findOne(tid);

		ProgramExecutionResponse response = new ProgramExecutionResponse();
		
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
