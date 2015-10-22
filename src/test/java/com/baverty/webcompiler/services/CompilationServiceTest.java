package com.baverty.webcompiler.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.stubbing.answers.CallsRealMethods;

import com.baverty.webcompiler.domain.OutputChunk;
import com.baverty.webcompiler.domain.Program;
import com.baverty.webcompiler.domain.enumtypes.ProgramStatus;
import com.baverty.webcompiler.repositories.ProgramsRepository;
import com.nitorcreations.junit.runners.NestedRunner;

@RunWith(NestedRunner.class)
public class CompilationServiceTest {

	@InjectMocks
	private CompilationService service;

	@Mock
	private ProgramsRepository programsRepository;

	@Mock
	private DockerManagementService dockerService;

	@Before
	public void beforeEach() {
		service = new CompilationService();
		MockitoAnnotations.initMocks(this);
	}

	public class CompileMethod {
		
		// Test constants / dummy values
		final Set<OutputChunk> compilationOutputChunks = new HashSet<OutputChunk>();
		final String containerId = "mockContainerId";
		final Program program = Mockito.mock(Program.class, new CallsRealMethods());
		
		@Before
		public void beforeEach() {
			when(dockerService.getContainer()).thenReturn(containerId);
			when(dockerService.splitOutput(any(InputStream.class))).thenReturn(compilationOutputChunks);
		}
		
		@Test
		public void shouldSaveTheProgramUsingTheRepo() {
			service.compile(program);
			verify(programsRepository).save(program);
		}
		
		
		public class InTheNominalCase {
			
			@Before
			public void beforeEach() {
				when(dockerService.checkProgramOnContainer(containerId)).thenReturn(true);
				service.compile(program);
			}
			
			@Test
			public void shouldSetTheCorrectCompilationOutputInTheProgram() {
				verify(program).setCompilationOutput(compilationOutputChunks);
			}
			
			@Test
			public void shouldSetTheProgramStatusToCompiled() {
				assertThat(program.getStatus()).isEqualTo(ProgramStatus.COMPILED);
			}
			
		}
		
		public class InCaseOfCompilationFailure {
			
			@Before
			public void beforeEach() {
				when(dockerService.checkProgramOnContainer(containerId)).thenReturn(false);
				service.compile(program);
			}
			
			@Test
			public void shouldSetTheCorrectCompilationOutputInTheProgram() {
				verify(program).setCompilationOutput(compilationOutputChunks);
			}
			
			@Test
			public void shouldSetTheProgramStatusToCompileError() {
				assertThat(program.getStatus()).isEqualTo(ProgramStatus.COMPILE_ERROR);
			}
		}
	}
}
