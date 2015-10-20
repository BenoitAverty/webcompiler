package com.baverty.webcompiler.services;

import java.io.InputStream;
import java.util.Set;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.baverty.webcompiler.domain.Execution;
import com.baverty.webcompiler.domain.OutputChunk;
import com.baverty.webcompiler.domain.Program;
import com.baverty.webcompiler.domain.enumtypes.ExecutionStatus;
import com.baverty.webcompiler.repositories.ExecutionsRepository;

@Service
public class ExecutionService {

	/**
	 * Repository for Executions persistence.
	 */
	@Inject
	private ExecutionsRepository executionsRepository;
	
	/**
	 * Docker management service.
	 * 
	 * Used to get interact with containers in which to execute programs
	 */
	@Inject
	private DockerManagementService dockerManagementService;
	
	/**
	 * Launch the execution of a program.
	 * 
	 * This method is asynchronous. It starts the compilation and it's up to the
	 * user of the service to check in the DB if the status of the execution changed to finished.
	 * 
	 * @param e The program to execute.
	 */
	@Async
	@Transactional
	public void execute(Execution e) {
		
		Program p = e.getProgram();
		try {
			InputStream output = dockerManagementService.execute(p.getContainerId());
			Set<OutputChunk> chunks = dockerManagementService.splitOutput(output);
			e.setOutput(chunks);
			e.setStatus(ExecutionStatus.EXECUTED);
		}
		catch(RuntimeException ex) {
			e.setStatus(ExecutionStatus.EXECUTION_ERROR);
			// TODO log something
		}
		
		executionsRepository.save(e);
	}

}
