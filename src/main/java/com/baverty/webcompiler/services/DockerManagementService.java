package com.baverty.webcompiler.services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import com.baverty.webcompiler.configuration.DockerHostConfiguration;
import com.baverty.webcompiler.domain.OutputChunk;
import com.baverty.webcompiler.domain.enumtypes.OutputStreamType;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.ExposedPort;

/**
 * Service used for all the interactions of the application with docker
 * containers.
 */
@Service
public class DockerManagementService {

	/**
	 * The docker client used to issue commands to the docker host.
	 */
	@Inject
	private DockerClient docker;
	
	/**
	 * Docker host information
	 */
	@Inject
	private DockerHostConfiguration dockerHostConfig;

	/**
	 * The tcp service used to send data to containers.
	 */
	@Inject
	private TcpService tcpService;

	/**
	 * The set of containers created by the service.
	 */
	private Map<String, CreateContainerResponse> containers;

	public DockerManagementService() {
		containers = new HashMap<String, CreateContainerResponse>();
	}

	/**
	 * Retrieve a container suitable for compilation and execution of a execution.
	 * 
	 * @return the ID of the container that was created
	 */
	public String getContainer() {

		// Create the container in the host. With Tty to be able to receive data
		// via netcat, exposing port 8080 for this purpose.
		CreateContainerResponse container = docker.createContainerCmd("frolvlad/alpine-gcc")
				.withCmd("tail", "-f", "/dev/null").withExposedPorts(new ExposedPort(8080)).withPublishAllPorts(true)
				.withTty(true).exec();

		// Save the container for side functions (destructor, ...)
		containers.put(container.getId(), container);

		return container.getId();
	}

	/**
	 * Transfer the source code of a execution to a file in the container.
	 * 
	 * @param sourceCode
	 *            the source code to send into the container
	 * @param containerId
	 *            The id of the container that will receive
	 */
	public void transferSourceCode(String sourceCode, String containerId) {

		startContainer(containerId);

		// Create the command that will listen to tcp connection and write
		// result to a file
		ExecCreateCmdResponse cmd = docker.execCreateCmd(containerId)
				.withCmd("/bin/sh", "-c", "nc -l -p 8080 > /home/execution.c").withTty().exec();

		// Start netcat
		docker.execStartCmd(containerId).withExecId(cmd.getId()).withDetach().exec();

		// Retrieve the port on the host machine
		Integer hostPort = docker.inspectContainerCmd(containerId).exec().getNetworkSettings().getPorts().getBindings()
				.get(new ExposedPort(8080))[0].getHostPort();

		// Connect to the port and send source code
		tcpService.sendData(dockerHostConfig.getAddress(), hostPort, sourceCode);
	}

	/**
	 * Compile the code in a container.
	 * 
	 * The source code must already be present in the container (see
	 * {@link #transferSourceCode(String, String)}).
	 * 
	 * @param containerId
	 *            the ID of the container in which to compile the code.
	 * @return the output of the compiler as an InputStream
	 */
	public InputStream compile(String containerId) {

		startContainer(containerId);

		// Compile
		ExecCreateCmdResponse cmd = docker.execCreateCmd(containerId)
				.withCmd("gcc", "-o", "/home/execution.exe", "/home/execution.c").withTty().withAttachStdout()
				.withAttachStderr().exec();

		InputStream cmdStream = docker.execStartCmd(containerId).withExecId(cmd.getId()).exec();

		return cmdStream;
	}

	/**
	 * Check that the execution is present on the container.
	 * 
	 * @param containerId
	 *            the container where the execution should be
	 * @return true if the execution is present, false otherwise
	 */
	public boolean checkProgramOnContainer(String containerId) {
		startContainer(containerId);

		ExecCreateCmdResponse cmd = docker.execCreateCmd(containerId).withCmd("ls", "/home/execution.exe")
				.withAttachStdout().exec();

		InputStream cmdStream = docker.execStartCmd(containerId).withExecId(cmd.getId()).exec();

		try {
			return IOUtils.toString(cmdStream).trim().equals("/home/execution.exe");
		} catch (IOException e) {
			// Nothing can be done about an IOException at this point. Throw it
			// back as a runtime exception.
			throw new RuntimeException(e);
		}

	}

	/**
	 * Execute a execution on a container.
	 * 
	 * Returns an InputStream containing the output of the execution.
	 * 
	 * @param containerId
	 *            the container where the execution to run is located
	 * @return the output of the execution
	 */
	public InputStream execute(String containerId) {
		startContainer(containerId);

		ExecCreateCmdResponse cmd = docker.execCreateCmd(containerId).withCmd("/bin/sh", "-c", "/home/execution.exe 2>&1")
				.withAttachStdout().exec();
		InputStream cmdStream = docker.execStartCmd(containerId).withExecId(cmd.getId()).exec();

		return cmdStream;

	}
	
	/**
	 * Split the output of a docker command into chunks.
	 * 
	 * @see OutputChunk
	 * @param cmdStream
	 *            the inputstream resulting from the docker command.
	 * @return a set of orphan output chunks belonging to no execution or
	 *         compilation.
	 */
	public Set<OutputChunk> splitOutput(InputStream cmdStream) {

		byte[] headerBuffer = new byte[8];
		
		Set<OutputChunk> result = new HashSet<OutputChunk>();
		int currentIndex = 0;

		try {
			while (cmdStream.read(headerBuffer) > 0) {

				OutputChunk chunk = new OutputChunk();
				
				// Determine the type of the chunk based on the first byte of the header
				switch (headerBuffer[0]) {
				case 0:
					chunk.setType(OutputStreamType.STDIN);
					break;
				case 1:
					chunk.setType(OutputStreamType.STDOUT);
					break;
				case 2:
					chunk.setType(OutputStreamType.STDERR);
					break;
				default:
					throw new InvalidParameterException(
							"Invalid header format in the input stream. The stream might not come from docker API");
				}
				
				// Determine the size of the chunk based on the last 4 bytes of the header
				ByteBuffer bb = ByteBuffer.wrap(headerBuffer);
				int size = bb.getInt(4);
				
				// Read the chunk
				byte[] contentBuffer = new byte[size];
				if(cmdStream.read(contentBuffer) < size) {
					throw new InvalidParameterException(
							"Not enough bytes in the input stream. The stream might not come from docker API");
				}
				chunk.setContent(new String(contentBuffer, StandardCharsets.UTF_8));
				
				// Remember the index of this chunk related to ther chunks
				chunk.setIndex(currentIndex++);
				
				result.add(chunk);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return result;
	}

	/**
	 * Test if a container is up.
	 * 
	 * @param containerId
	 *            the container to test
	 * @return true if the container is up, false otherwise
	 */
	private boolean isUp(String containerId) {
		return docker.inspectContainerCmd(containerId).exec().getState().isRunning();
	}

	/**
	 * Start a container if it's not up.
	 */
	private void startContainer(String containerId) {
		if (!isUp(containerId))
			docker.startContainerCmd(containerId).exec();
	}

	/**
	 * Clean containers created by this service.
	 */
	@PreDestroy
	private void removeContainers() {
		for (String containerId : containers.keySet()) {
			docker.stopContainerCmd(containerId).exec();
			docker.removeContainerCmd(containerId).exec();
		}
	}
}
