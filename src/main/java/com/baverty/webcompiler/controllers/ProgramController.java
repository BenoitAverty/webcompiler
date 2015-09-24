package com.baverty.webcompiler.controllers;

import javax.inject.Inject;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
	 * REST service to create a compiled program.
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
	
	@RequestMapping(value = "/program/{tid}/status", method=RequestMethod.GET)
	public ProgramStatus getProgramStatus(@PathVariable Long tid) {
		
		return programRepository.findByTid(tid).getStatus();
	}
}
