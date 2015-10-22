package com.baverty.webcompiler.controllers;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.jca.cci.CannotCreateRecordException;

import com.baverty.webcompiler.controllers.responseobjects.PostProgramResponse;
import com.baverty.webcompiler.controllers.responseobjects.RequestStatus;
import com.baverty.webcompiler.domain.Program;
import com.baverty.webcompiler.repositories.ExecutionsRepository;
import com.baverty.webcompiler.repositories.ProgramsRepository;
import com.baverty.webcompiler.services.CompilationService;
import com.baverty.webcompiler.services.ExecutionService;

public class ProgramsControllerTest {

	/** Controller to test. */
	@InjectMocks
	private ProgramsController programsController;

	/** mock of a Program Repository. */
	@Mock
	private ProgramsRepository programsRepository;

	/** mock of a Executions Repository. */
	@Mock
	private ExecutionsRepository executionsRepository;

	/** mock of the Compilation service. */
	@Mock
	private CompilationService compilationService;

	/** mock of the Execution service. */
	@Mock
	private ExecutionService executionService;

	/**
	 * Setup instance of service to test and inject mocks into it.
	 */
	@Before
	public void setUp() {
		programsController = new ProgramsController();

		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Test the {@link ProgramsController#postProgram(String)} method.
	 * 
	 * This method handles the POST /programs endpoint, and should create a
	 * execution via the repository, then launch the compilation and return a new
	 * execution ID.
	 */
	@Test
	public void testPostProgram() {
		final String sourceCode = "source\ncode";
		final long newProgramId = 1L;

		when(programsRepository.save(any(Program.class))).thenAnswer((InvocationOnMock inv) -> {
			inv.getArgumentAt(0, Program.class).setTid(newProgramId);
			return inv.getArgumentAt(0, Program.class);
		});

		PostProgramResponse resp = programsController.postProgram(sourceCode);

		assertThat(resp.programId).isEqualTo(newProgramId);
		assertThat(resp.status).isEqualTo(RequestStatus.OK);

		ArgumentCaptor<Program> pArg = ArgumentCaptor.forClass(Program.class);
		verify(programsRepository).save(pArg.capture());
		assertThat(pArg.getValue().getSourceCode()).isEqualTo(sourceCode);
		verify(compilationService).compile(pArg.getValue());
	}

	/**
	 * Test the {@link ProgramsController#postProgram(String)} method when a
	 * persistance exception occurs.
	 * 
	 * When posting a new execution, the controller might fail to create the
	 * Program entity. In that case, the controller should answer with a null
	 * execution ID and a KO status.
	 * 
	 * Warning suppressed to allow the use of "thenThrow" method with a class parameter.
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testPostProgramPersistenceError() {
		final String sourceCode = "source\ncode";
		
		when(programsRepository.save(any(Program.class))).thenThrow(CannotCreateRecordException.class);
		
		PostProgramResponse resp = programsController.postProgram(sourceCode);

		assertThat(resp.programId).isNull();
		assertThat(resp.status).isEqualTo(RequestStatus.KO);
	}

}
