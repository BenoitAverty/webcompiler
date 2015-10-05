package com.baverty.webcompiler.services;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.baverty.webcompiler.domain.Execution;
import com.baverty.webcompiler.domain.Program;
import com.baverty.webcompiler.domain.enumtypes.ExecutionStatus;
import com.baverty.webcompiler.repositories.ExecutionsRepository;

public class ExecutionServiceTest {

	/**
	 * Service to test
	 */
	@InjectMocks
	private ExecutionService executionService;

	/** Execution repository mock. */
	@Mock
	private ExecutionsRepository executionsRepository;
	/** docker service mock. */
	@Mock
	private DockerManagementService dockerService;

	/**
	 * Setup instance of service to test and inject mocks into it.
	 */
	@Before
	public void setup() {
		executionService = new ExecutionService();

		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Test the nominal execution of the
	 * {@link ExecutionService#execute(Execution)} method.
	 * 
	 * In the nominal case, the docker management service should return the
	 * output of the execution (in this case, a simple "Hello World"). The
	 * output should be saved in the execution object and the status should be
	 * set to "compiled".
	 * 
	 */
	@Test
	public void testExecuteNominal() {

		final String executionOutput = "Hello, world!";

		when(dockerService.execute(anyString())).thenReturn(executionOutput);

		Execution execution = Mockito.mock(Execution.class);
		Program p = Mockito.mock(Program.class);
		when(execution.getProgram()).thenReturn(p);

		executionService.execute(execution);

		verify(execution).setOutput(executionOutput);
		verify(execution).setStatus(ExecutionStatus.EXECUTED);
		verify(executionsRepository).save(execution);
	}

	/**
	 * Test the execution of the {@link ExecutionService#execute(Execution)}
	 * method when an exception occurs in the underlying docker service.
	 * 
	 * In case of exception, the {@link ExecutionService#execute(Execution)}
	 * method should set the status of the execution to EXECUTION_ERROR and not
	 * fill the output.
	 * 
	 */
	@Test
	public void testExecuteDockerException() {
		
		when(dockerService.execute(anyString())).thenThrow(new RuntimeException());

		Execution execution = Mockito.mock(Execution.class);

		executionService.execute(execution);

		verify(execution, never()).setOutput(anyString());
		verify(execution).setStatus(ExecutionStatus.EXECUTION_ERROR);
		verify(executionsRepository).save(execution);
	}

}
