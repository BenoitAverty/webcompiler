package com.baverty.webcompiler.services;

import java.io.InputStream;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baverty.webcompiler.WebcompilerApplication;
import com.baverty.webcompiler.domain.OutputChunk;
import com.baverty.webcompiler.domain.Program;
import com.baverty.webcompiler.domain.enumtypes.ProgramStatus;
import com.baverty.webcompiler.repositories.ProgramsRepository;

/**
 * Service used to compile programs.
 */
@Service
public class CompilationService {

	private static final Logger log = LoggerFactory.getLogger(WebcompilerApplication.class);
	
	/**
	 * Repository for Program persistence.
	 */
	@Inject
	private ProgramsRepository programRepository;

	/**
	 * Docker management service.
	 *
	 * Used to get interact with containers in which to compile programs
	 */
	@Inject
	private DockerManagementService dockerManagementService;

	/**
	 * Compile the given execution.
	 *
	 * <ol>
	 * <li>Retrieve a suitable Docker container</li>
	 * <li>Compile the source code in the container</li>
	 * <li>Set the execution status to COMPILED once finished (or COMPILE_ERROR if failure)</li>
	 * </ol>
	 *
	 * This method is asynchronous. It starts the compilation and it's up to the
	 * user of the service to check in the DB if the status changed to compile.
	 *
	 * @param p
	 *            the execution to compile
	 */
	@Async
	@Transactional
	public void compile(Program p) {

		try {
			// Get a container suitable for this execution
			String containerId = dockerManagementService.getContainer();
			p.setContainerId(containerId);
	
			// Try to compile the execution using this container.
			dockerManagementService.transferSourceCode(p.getSourceCode(), containerId);
			InputStream compilationOutput = dockerManagementService.compile(p.getContainerId());
			
			Set<OutputChunk> chunks = dockerManagementService.splitOutput(compilationOutput);
			
			for(OutputChunk c : chunks) {
				c.setProgram(p);
			}
			p.setCompilationOutput(chunks);
	
			// Check that the compilation was successful
			if(dockerManagementService.checkProgramOnContainer(containerId)) {
				p.setStatus(ProgramStatus.COMPILED);
			}
			else {
				p.setStatus(ProgramStatus.COMPILE_ERROR);
			}
		}
		catch(RuntimeException e) {
			p.setStatus(ProgramStatus.COMPILE_ERROR);
			log.error("Unexpected error during compilation of program " + p.getTid() + " : " + e.getMessage());
		}
		finally {
			programRepository.save(p);
		}
	}

}
