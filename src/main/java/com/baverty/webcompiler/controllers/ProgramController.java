package com.baverty.webcompiler.controllers;

import javax.inject.Inject;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.baverty.webcompiler.controllers.responseobjects.ProgramStatusResponse;
import com.baverty.webcompiler.domain.Program;
import com.baverty.webcompiler.domain.enumtypes.ProgramStatus;
import com.baverty.webcompiler.repositories.ProgramRepository;
import com.baverty.webcompiler.services.CompilationService;

@RestController
public class ProgramController {

	/***
	 * Repository for Program persistence.
	 */
	@Inject
	private ProgramRepository programRepository;
	
	/***
	 * Service used to compile programs.
	 */
	@Inject
	private CompilationService compilationService;
	
	/**
	 * REST service used to create a program.
	 * 
	 * If the program needs to be compiled, the compilation will be launched asynchronously by this webservice.
	 * 
	 * @param body The body of the request should contain the source code to compile.
	 * @return A unique ID representing the program in the system. The id will be used for all further interactions with the API.
	 */
	@RequestMapping(value = "/program", method=RequestMethod.POST)
	public Long createProgram(@RequestBody String body) {
		
		Program p = new Program();
		
		p.setStatus(ProgramStatus.NEW);
		p.setSourceCode(body);
		programRepository.save(p);
		
		compilationService.compile(p);
		
		return p.getTid();
		
	}
	
	/**
	 * REST service used to retrieve the status of a program.
	 * 
	 * @param tid The tid of the program.
	 * @return A structure containing the status and information related to the status.
	 */
	@RequestMapping(value = "/program/{tid}/status", method=RequestMethod.GET)
	public ProgramStatusResponse getProgramStatus(@PathVariable Long tid) {
		
		ProgramStatusResponse response = new ProgramStatusResponse();
		
		Program p = programRepository.findByTid(tid);
		
		response.status = p.getStatus();
		response.compilationOutput = p.getCompilationOutput();
		
		return response;
	}
}
