package com.baverty.webcompiler.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs;

import com.baverty.webcompiler.test.utils.AnswerWithSelf;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmd;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.ExecStartCmd;
import com.github.dockerjava.api.command.InspectContainerCmd;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports.Binding;

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
	 * Set up the docker client mock to answer "true" when asking if a container
	 * is running.
	 * 
	 * Used to test methods that check if the container is running before doing
	 * their job.
	 * 
	 * @return the InspectContainerCmd mock created, so that it can be further
	 *         expanded if the test needs it.
	 */
	private InspectContainerCmd mockContainerIsStarted() {

		InspectContainerCmd cmd = mock(InspectContainerCmd.class, new ReturnsDeepStubs());
		when(cmd.exec().getState().isRunning()).thenReturn(true);

		when(docker.inspectContainerCmd(anyString())).thenReturn(cmd);

		return cmd;
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
		CreateContainerCmd cmd = mock(CreateContainerCmd.class, new AnswerWithSelf(CreateContainerCmd.class));
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

		// Mock inspect cmd to get container sate and keep the mock to improve
		// it
		InspectContainerCmd inspectCmd = mockContainerIsStarted();

		// Use self returning answer to mock the chaining used by the API
		ExecCreateCmdResponse resp = mock(ExecCreateCmdResponse.class);
		when(resp.getId()).thenReturn("");

		ExecCreateCmd createCmd = mock(ExecCreateCmd.class, new AnswerWithSelf(ExecCreateCmd.class));
		when(createCmd.exec()).thenReturn(resp);

		when(docker.execCreateCmd(anyString())).thenReturn(createCmd);

		ExecStartCmd startCmd = mock(ExecStartCmd.class, new AnswerWithSelf(ExecStartCmd.class));
		when(docker.execStartCmd(anyString())).thenReturn(startCmd);

		// mock the very tedious call used to retrieve port in the container
		Binding bmock = mock(Binding.class);
		when(inspectCmd.exec().getNetworkSettings().getPorts().getBindings().get(any(ExposedPort.class)))
				.thenReturn(new Binding[] { bmock });
		when(bmock.getHostPort()).thenReturn(mockPort);

		// Call method to test
		dockerManagementService.transferSourceCode(sourceCode, containerId);

		// Verify that we connected to the right port and sent the right data
		verify(tcpService).sendData("localhost", mockPort, sourceCode);
	}

	/**
	 * Test the nominal execution of the
	 * {@link DockerManagementService#compile(String)} method.
	 * 
	 * In the nominal case, the method should start the compilation cmd on the
	 * container and return a compilation output.
	 * 
	 */
	@Test
	public void testCompileNominal() {

		final String containerId = "mockContainerId";
		final String compilationOutput = "mock\nCompilationOutput";

		mockContainerIsStarted();

		// Use self returning answer to mock the chaining used by the API
		ExecCreateCmd createCmd = mock(ExecCreateCmd.class, new AnswerWithSelf(ExecCreateCmd.class));
		when(createCmd.exec()).thenReturn(mock(ExecCreateCmdResponse.class));
		ExecStartCmd startCmd = mock(ExecStartCmd.class, new AnswerWithSelf(ExecStartCmd.class));
		when(docker.execCreateCmd(anyString())).thenReturn(createCmd);
		when(docker.execStartCmd(anyString())).thenReturn(startCmd);
		when(startCmd.exec()).thenReturn(IOUtils.toInputStream(compilationOutput));

		// Call method to test
		String result = dockerManagementService.compile(containerId);

		// Verify that we connected to the right port and sent the right data
		verify(docker, times(1)).execCreateCmd(containerId);
		verify(docker, times(1)).execStartCmd(containerId);
		assertThat(result).isEqualTo(compilationOutput);
	}

}
