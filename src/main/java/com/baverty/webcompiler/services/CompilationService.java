package com.baverty.webcompiler.services;

import javax.inject.Inject;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.baverty.webcompiler.domain.Program;
import com.baverty.webcompiler.domain.enumtypes.ProgramStatus;
import com.baverty.webcompiler.repositories.ProgramRepository;

/**
 * Service used to compile programs.
 * 
 * @author baverty
 */
@Service
public class CompilationService {

	/**
	 * Repository for Program persistence.
	 */
	@Inject
	private ProgramRepository programRepository;

	/**
	 * Docker management service.
	 * 
	 * Used to get containers in which to compile programs
	 */
	@Inject
	private DockerManagementService dockerManagementService;

	/**
	 * Compile the given program.
	 * 
	 * <ol>
	 * <li>Retrieve a suitable Docker container</li>
	 * <li>Compile the source code in the container</li>
	 * <li>Set the program status to COMPILED once finished (or COMPILE_ERROR if failure)</li>
	 * </ol>
	 * 
	 * This method is asynchronous. It starts the compilation and it's up to the
	 * user of the service to check in the DB if the status changed to compile.
	 * 
	 * @param p
	 *            the program to compile
	 */
	@Async
	public void compile(Program p) {
		// Get a container suitable for this program
		String containerId = dockerManagementService.getContainer();
		p.setContainerId(containerId);
		
		// Try to compile the program using this container.
		try {
			dockerManagementService.transferSourceCode(p.getSourceCode(), containerId);
			dockerManagementService.compile(p.getContainerId());
			p.setStatus(ProgramStatus.COMPILED);
		}
		catch(Exception e) {
			p.setStatus(ProgramStatus.COMPILE_ERROR);
		}
		programRepository.save(p);
	}

}
