package com.baverty.webcompiler.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs;

import com.baverty.webcompiler.configuration.DockerHostConfiguration;
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
import com.nitorcreations.junit.runners.NestedRunner;

@RunWith(NestedRunner.class)
public class DockerManagementServiceTest {
	
	/** system under test */
	@InjectMocks
	private DockerManagementService service;

	/** Mock of the docker client */
	@Mock
	private DockerClient docker;

	/** Mock of the Tcp Service */
	@Mock
	private TcpService tcpService;
	
	@Mock 
	private DockerHostConfiguration dockerConfig;

	// Test constants / dummy values
	private static final String containerId = "mockContainerId";
	private final InputStream dockerCmdOutput = IOUtils.toInputStream("compilationOutput");
	
	/** Setup instance of service to test and inject mocks into it. */
	@Before
	public void beforeEach() {
		service = new DockerManagementService();
		MockitoAnnotations.initMocks(this);
	}
	
	public class GetContainerMethod {
		
		@Before
		public void beforeEach() {
			CreateContainerResponse resp = mock(CreateContainerResponse.class);
			when(resp.getId()).thenReturn(containerId);

			// Use self returning answer to mock the chaining used by the API
			CreateContainerCmd cmd = mock(CreateContainerCmd.class, new AnswerWithSelf(CreateContainerCmd.class));
			when(cmd.exec()).thenReturn(resp);
			when(docker.createContainerCmd(anyString())).thenReturn(cmd);
		}
		
		@Test
		public void shouldReturnTheCorrectContainerId() {
			String ret = service.getContainer();
			assertThat(ret).isEqualTo(containerId);
		}
		
	}
	
	public class TransferSourceCodeMethod {
		private static final String containerHost = "localhost";
		private static final String sourceCode = "mockSourceCode";
		private static final int mockPort = 1;

		@Before
		public void beforeEach() {
			// Mock inspect cmd to get container sate and keep the mock to improve
			// it
			InspectContainerCmd inspectCmd = mockContainerIsStarted();

			// Use self returning answer to mock the chaining used by the API
			ExecCreateCmdResponse resp = mock(ExecCreateCmdResponse.class);
			when(resp.getId()).thenReturn("");
			
			// Mock access to properties
			when(dockerConfig.getAddress()).thenReturn(containerHost);

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

		}
		
		@Test
		public void shouldCallTcpServiceWithCorrectParameters() {
			// Call method to test
			service.transferSourceCode(sourceCode, containerId);

			// Verify that we connected to the right port and sent the right data
			verify(tcpService).sendData(containerHost, mockPort, sourceCode);
		}
	}
	
	public class CompileMethod {
		
		@Before
		public void beforeEach() {
			mockContainerIsStarted();
			
			// Use self returning answer to mock the chaining used by the API
			ExecCreateCmd createCmd = mock(ExecCreateCmd.class, new AnswerWithSelf(ExecCreateCmd.class));
			when(createCmd.exec()).thenReturn(mock(ExecCreateCmdResponse.class));
			ExecStartCmd startCmd = mock(ExecStartCmd.class, new AnswerWithSelf(ExecStartCmd.class));
			when(docker.execCreateCmd(anyString())).thenReturn(createCmd);
			when(docker.execStartCmd(anyString())).thenReturn(startCmd);
			when(startCmd.exec()).thenReturn(dockerCmdOutput);
		}
		
		@Test
		public void shouldReturnTheCompilationOutput() {
			// Call method to test
			InputStream result = service.compile(containerId);
			
			assertThat(result).isEqualTo(dockerCmdOutput);
		}
	}

	public class CheckProgramOnContainerMethod {
		
		private ExecStartCmd startCmd = mock(ExecStartCmd.class, new AnswerWithSelf(ExecStartCmd.class));
		private ExecCreateCmd createCmd = mock(ExecCreateCmd.class, new AnswerWithSelf(ExecCreateCmd.class));
		
		@Before
		public void beforeEach() {

			mockContainerIsStarted();
			
			when(createCmd.exec()).thenReturn(mock(ExecCreateCmdResponse.class));
			when(docker.execCreateCmd(anyString())).thenReturn(createCmd);
			when(docker.execStartCmd(anyString())).thenReturn(startCmd);

		}
		
		@Test
		public void shouldReturnTrueIfProgramIsPresent() {
			when(startCmd.exec()).thenReturn(IOUtils.toInputStream("/home/execution.exe"));
			// Call method to test
			boolean result = service.checkProgramOnContainer(containerId);

			assertThat(result).isTrue();
		}
		
		@Test
		public void shouldReturnFalseIfProgramIsAbsent() {
			when(startCmd.exec()).thenReturn(IOUtils.toInputStream(""));
			// Call method to test
			boolean result = service.checkProgramOnContainer(containerId);

			assertThat(result).isFalse();
		}
	}

	public class ExecuteMethod {
		
		@Before
		public void beforeEach() {
			mockContainerIsStarted();

			// Use self returning answer to mock the chaining used by the API
			ExecCreateCmd createCmd = mock(ExecCreateCmd.class, new AnswerWithSelf(ExecCreateCmd.class));
			when(createCmd.exec()).thenReturn(mock(ExecCreateCmdResponse.class));
			ExecStartCmd startCmd = mock(ExecStartCmd.class, new AnswerWithSelf(ExecStartCmd.class));
			when(docker.execCreateCmd(anyString())).thenReturn(createCmd);
			when(docker.execStartCmd(anyString())).thenReturn(startCmd);
			when(startCmd.exec()).thenReturn(dockerCmdOutput);
		}
		
		@Test
		public void shouldReturnCorrectOutput() {
			// Call method to test
			InputStream result = service.execute(containerId);
			assertThat(result).isEqualTo(dockerCmdOutput);
		}
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
}
