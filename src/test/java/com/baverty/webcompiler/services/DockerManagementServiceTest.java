package com.baverty.webcompiler.services;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.baverty.webcompiler.domain.Execution;
import com.baverty.webcompiler.domain.Program;
import com.baverty.webcompiler.domain.enumtypes.ExecutionStatus;
import com.baverty.webcompiler.repositories.ExecutionsRepository;
import com.baverty.webcompiler.test.utils.SelfReturningAnswer;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;

public class DockerManagementServiceTest {

	/**
	 * Service to test
	 */
	@InjectMocks
	private DockerManagementService dockerManagementService;

	/**
	 * Mock of the docker client
	 */
	@Mock
	private DockerClient docker;

	/**
	 * Setup instance of service to test and inject mocks into it.
	 */
	@Before
	public void setup() {
		dockerManagementService = new DockerManagementService();

		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Test the nominal execution of the
	 * {@link DockerManagementService#getContainer()} method.
	 * 
	 * In the nominal case, the docker api should successfully create a docker
	 * container. In that case, this method retrieves the created container's ID
	 * and returns it.
	 * 
	 */
	@Test
	public void testGetContainerNominal() {

		final String containerId = "mockContainerId";
		
		CreateContainerResponse resp = mock(CreateContainerResponse.class);
		when(resp.getId()).thenReturn(containerId);
		
		// Use self returning answer to mock the chaining used by the API
		CreateContainerCmd cmd = mock(CreateContainerCmd.class, new SelfReturningAnswer());
		when(cmd.exec()).thenReturn(resp);
		when(docker.createContainerCmd(anyString())).thenReturn(cmd);

		String ret = dockerManagementService.getContainer();

		assertThat(ret).isEqualTo(containerId);
	}

}
