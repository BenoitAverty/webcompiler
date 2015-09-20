package com.baverty.webcompiler.services;

import javax.inject.Inject;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.baverty.webcompiler.domain.Program;
import com.baverty.webcompiler.repositories.ProgramRepository;

@Service
public class CompilationService {

	@Inject
	ProgramRepository programRepository;
	
	@Async
	public void compile(Program p) {
		try {
			Thread.sleep(20000);
			p.setStatus("Compiled");
			programRepository.save(p);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}

}
