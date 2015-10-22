package com.baverty.webcompiler.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.stubbing.answers.CallsRealMethods;

import com.baverty.webcompiler.domain.Execution;
import com.baverty.webcompiler.domain.OutputChunk;
import com.baverty.webcompiler.domain.Program;
import com.baverty.webcompiler.domain.enumtypes.ExecutionStatus;
import com.baverty.webcompiler.domain.enumtypes.ProgramStatus;
import com.baverty.webcompiler.repositories.ExecutionsRepository;
import com.baverty.webcompiler.repositories.ProgramsRepository;
import com.nitorcreations.junit.runners.NestedRunner;

@RunWith(NestedRunner.class)
public class ExecutionServiceTest {

	@InjectMocks
	private ExecutionService service;

	@Mock
	private ExecutionsRepository executionsRepository;

	@Mock
	private DockerManagementService dockerService;

	@Before
	public void beforeEach() {
		service = new ExecutionService();
		MockitoAnnotations.initMocks(this);
	}


	public class ExecuteMethod {
		
		// Test constants / dummy values
		final Set<OutputChunk> executionOutputChunks = new HashSet<OutputChunk>();
		final String containerId = "mockContainerId";
		final Execution execution = Mockito.mock(Execution.class, new CallsRealMethods());
		final Program program = Mockito.mock(Program.class, new CallsRealMethods());
		
		@Before
		public void beforeEach() {
			when(dockerService.getContainer()).thenReturn(containerId);
			when(dockerService.splitOutput(any(InputStream.class))).thenReturn(executionOutputChunks);
			execution.setProgram(program);
		}
		
		@Test
		public void shouldSaveTheExecutionUsingTheRepo() {
			service.execute(execution);
			verify(executionsRepository).save(execution);
		}
		
		
		public class InTheNominalCase {
			
			@Before
			public void beforeEach() {
				when(dockerService.splitOutput(any(InputStream.class))).thenReturn(executionOutputChunks);
				service.execute(execution);
			}
			
			@Test
			public void shouldSetTheCorrectOutputInTheExecution() {
				verify(execution).setOutput(executionOutputChunks);
			}
			
			@Test
			public void shouldSetTheProgramStatusToCompiled() {
				assertThat(execution.getStatus()).isEqualTo(ExecutionStatus.EXECUTED);
			}
			
		}
		
		public class InCaseOfDockerException {
			@Before
			public void beforeEach() {
				when(dockerService.execute(anyString())).thenThrow(new RuntimeException());
				service.execute(execution);
			}
			
			@Test
			public void shouldNotSetAnyExecutionOutput() {
				verify(execution, never()).setOutput(any());	
			}
			
			@Test 
			public void shouldSetTheExecutionStatusToCompileError() {
				assertThat(execution.getStatus()).isEqualTo(ExecutionStatus.EXECUTION_ERROR);
			}
		}
	}
}
