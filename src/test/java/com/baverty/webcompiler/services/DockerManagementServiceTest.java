package com.baverty.webcompiler.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs;

import com.baverty.webcompiler.test.utils.SelfReturningAnswer;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmd;
import com.github.dockerjava.api.command.ExecStartCmd;
import com.github.dockerjava.api.command.InspectContainerCmd;
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
	 * Mock of the tcp service.
	 */
	@Mock
	private TcpService tcpService;

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

	/**
	 * Test the nominal execution of the
	 * {@link DockerManagementService#transferSourceCode(String, String)}
	 * method.
	 * 
	 * In the nominal case, the method should open a port on the destination
	 * machine and use the tcpservice to send data through this port.
	 * 
	 */
	@Test
	public void testTransferSourceCodeNominal() {

		final String containerId = "mockContainerId";
		final String sourceCode = "mockSourceCode";
		final Integer mockPort = 1;

		// Use self returning answer to mock the chaining used by the API
		ExecCreateCmd createCmd = mock(ExecCreateCmd.class, new SelfReturningAnswer());
		ExecStartCmd startCmd = mock(ExecStartCmd.class, new SelfReturningAnswer());
		when(docker.execCreateCmd(anyString())).thenReturn(createCmd);
		when(docker.execStartCmd(anyString())).thenReturn(startCmd);

		// mock the very tedious call used to retrieve port in the container
		InspectContainerCmd inspectCmd = mock(InspectContainerCmd.class, new ReturnsDeepStubs());
		when(docker.inspectContainerCmd(anyString())).thenReturn(inspectCmd);
		when(inspectCmd.exec().getNetworkSettings().getPorts().getBindings().get(any(ExposedPort.class))[0]
				.getHostPort()).thenReturn(mockPort);
		
		// Call method to test
		dockerManagementService.transferSourceCode(sourceCode, containerId);

		// Verify that we connected to the right port and sent the right data
		verify(tcpService).sendData("localhost", mockPort, sourceCode);
	}

}
