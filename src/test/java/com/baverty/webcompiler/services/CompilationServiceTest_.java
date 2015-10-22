package com.baverty.webcompiler.services;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.baverty.webcompiler.domain.Program;
import com.baverty.webcompiler.domain.enumtypes.ProgramStatus;
import com.baverty.webcompiler.repositories.ProgramsRepository;

public class CompilationServiceTest_ {

	/**
	 * Service to test
	 */
	@InjectMocks
	private CompilationService compilationService;

	/** Programs repository mock. */
	@Mock
	private ProgramsRepository programsRepository;
	/** docker service mock. */
	@Mock
	private DockerManagementService dockerService;

	/**
	 * Setup instance of service to test and inject mocks into it.
	 */
	@Before
	public void setup() {
		compilationService = new CompilationService();

		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Test the method {@link CompilationService#compile(Program)} in the
	 * nominal case.
	 * 
	 * In the nominal case, this method takes a program and should set its
	 * status to COMPILED and its output to the compilation output, then save it
	 * in the repository.
	 */
	@Test
	public void testCompileNominal() {
		final String compilationOutput = "";
		final String containerId = "mockContainerId";

		when(dockerService.getContainer()).thenReturn(containerId);
		when(dockerService.compile(anyString())).thenReturn();
		when(dockerService.checkProgramOnContainer(containerId)).thenReturn(true);

		Program program = Mockito.mock(Program.class);

		compilationService.compile(program);

		verify(program).setCompilationOutput(compilationOutput);
		verify(program).setStatus(ProgramStatus.COMPILED);
		verify(programsRepository).save(program);
	}

	/**
	 * Test the execution of the {@link CompilationService#compile(Program)}
	 * method when the compilation fails.
	 * 
	 * In case of exception, the {@link CompilationService#compile(Program) compile}
	 * method should set the status of the execution to EXECUTION_ERROR and not
	 * fill the output.
	 * 
	 */
	@Test
	public void testCompileCompilationFailure() {
		
		final String compilationOutput = "mockCompilationOutput";
		final String containerId = "mockContainerId";

		when(dockerService.compile(anyString())).thenReturn(compilationOutput);
		when(dockerService.checkProgramOnContainer(containerId)).thenReturn(false);

		Program program = Mockito.mock(Program.class);

		compilationService.compile(program);

		verify(program).setCompilationOutput(compilationOutput);
		verify(program).setStatus(ProgramStatus.COMPILE_ERROR);
		verify(programsRepository).save(program);
	}
	
	/**
	 * Test the execution of the {@link CompilationService#compile(Program)}
	 * method when an exception occurs in the underlying docker service.
	 * 
	 * In case of exception, the {@link CompilationService#compile(Program) compile}
	 * method should set the status of the program to COMPILE_ERROR and not
	 * fill the output.
	 * 
	 */
	@Test
	public void testCompileDockerException() {
		
		when(dockerService.compile(anyString())).thenThrow(new RuntimeException());

		Program program = Mockito.mock(Program.class);

		compilationService.compile(program);

		verify(program, never()).setCompilationOutput(anyString());
		verify(program).setStatus(ProgramStatus.COMPILE_ERROR);
		verify(programsRepository).save(program);
	}
}
