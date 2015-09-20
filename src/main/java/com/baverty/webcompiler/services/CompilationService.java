package com.baverty.webcompiler.services;

import javax.inject.Inject;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.baverty.webcompiler.domain.Program;
import com.baverty.webcompiler.repositories.ProgramRepository;

@Service
public class CompilationService {

	@Inject 
	public ProgramRepository programRepository;
	
	@Async
	public void compile(Program p) {
		try {
			System.out.println("Starting sleeping");
			Thread.sleep(20000);
			System.out.println("Sleep end");
			p.setStatus("Compiled");
			programRepository.save(p);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}

}
