package com.baverty.webcompiler.services;

import java.io.InputStream;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baverty.webcompiler.domain.Execution;
import com.baverty.webcompiler.domain.OutputChunk;
import com.baverty.webcompiler.domain.Program;
import com.baverty.webcompiler.domain.enumtypes.ExecutionStatus;
import com.baverty.webcompiler.repositories.ExecutionsRepository;

@Service
public class ExecutionService {

	private static final Logger log = LoggerFactory.getLogger(ExecutionService.class);
	
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
	 * Launch the execution of a execution.
	 * 
	 * This method is asynchronous. It starts the compilation and it's up to the
	 * user of the service to check in the DB if the status of the execution changed to finished.
	 * 
	 * @param e The execution to execute.
	 */
	@Async
	@Transactional
	public void execute(Execution e) {
		
		Program p = e.getProgram();
		try {
			InputStream output = dockerManagementService.execute(p.getContainerId());
			Set<OutputChunk> chunks = dockerManagementService.splitOutput(output);
			
			for(OutputChunk c : chunks) {
				c.setExecution(e);
			}
			e.setOutput(chunks);
			
			e.setStatus(ExecutionStatus.EXECUTED);
		}
		catch(RuntimeException ex) {
			e.setStatus(ExecutionStatus.EXECUTION_ERROR);
			log.error("Unexpected error during execution " + e.getTid() + " : " + ex.getMessage());
		}
		
		executionsRepository.save(e);
	}

}
