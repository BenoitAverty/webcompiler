package com.baverty.webcompiler.controllers;

import javax.inject.Inject;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.baverty.webcompiler.domain.Program;
import com.baverty.webcompiler.repositories.ProgramRepository;
import com.baverty.webcompiler.services.CompilationService;

@RestController
public class ProgramController {

	@Inject
	private ProgramRepository programRepository;
	
	@Inject
	private CompilationService compilationService;
	
	@RequestMapping(value = "/program", method=RequestMethod.POST)
	public Long createCompilation(@RequestBody String body) {
		
		Program p = new Program();
		
		p.setStatus("New");
		p.setSourceCode(body);
		
		programRepository.save(p);
		
		System.out.println("Calling compile");
		compilationService.compile(p);
		System.out.println("Compile exited.");
		
		return p.getTid();
		
	}
	
	@RequestMapping(value = "/program/{tid}/status", method=RequestMethod.GET)
	public String getProgramStatus(@PathVariable Long tid) {
		
		return programRepository.findByTid(tid).getStatus();
	}
}
