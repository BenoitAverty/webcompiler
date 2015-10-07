package com.baverty.webcompiler.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

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
	 * Retrieve a container suitable for compilation and execution of a program.
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
	 * Transfer the source code of a program to a file in the container.
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
				.withCmd("/bin/sh", "-c", "nc -l -p 8080 > /home/program.c").withTty().exec();

		// Start netcat
		docker.execStartCmd(containerId).withExecId(cmd.getId()).withDetach().exec();

		// Retrieve the port on the host machine
		Integer hostPort = docker.inspectContainerCmd(containerId).exec().getNetworkSettings().getPorts().getBindings()
				.get(new ExposedPort(8080))[0].getHostPort();

		// Connect to the port and send source code
		tcpService.sendData("localhost", hostPort, sourceCode);
	}

	/**
	 * Compile the code in a container.
	 * 
	 * The source code must already be present in the container (see
	 * {@link #transferSourceCode(String, String)}).
	 * 
	 * @param containerId
	 *            the ID of the container in which to compile the code.
	 * @return the output of the compiler
	 */
	public String compile(String containerId) {

		startContainer(containerId);

		// Compile
		ExecCreateCmdResponse cmd = docker.execCreateCmd(containerId)
				.withCmd("gcc", "-o", "/home/program.exe", "/home/program.c").withTty().withAttachStdout()
				.withAttachStderr().exec();

		InputStream cmdStream = docker.execStartCmd(containerId).withExecId(cmd.getId()).exec();

		try {
			String result = IOUtils.toString(cmdStream, "utf-8");
			cmdStream.close();
			return result;
		} catch (IOException e) {
			// Nothing can be done about an IOException at this point. Throw it
			// back as a runtime exception.
			throw new RuntimeException(e);
		}
	}

	/**
	 * Check that the program is present on the container.
	 * 
	 * @param containerId
	 *            the container where the program should be
	 */
	public boolean checkProgramOnContainer(String containerId) {
		startContainer(containerId);

		ExecCreateCmdResponse cmd = docker.execCreateCmd(containerId).withCmd("ls", "/home/program.exe")
				.withAttachStdout().exec();

		InputStream cmdStream = docker.execStartCmd(containerId).withExecId(cmd.getId()).exec();

		try {
			return IOUtils.toString(cmdStream).trim().equals("/home/program.exe");
		} catch (IOException e) {
			// Nothing can be done about an IOException at this point. Throw it
			// back as a runtime exception.
			throw new RuntimeException(e);
		}

	}

	/**
	 * Execute a program on a container.
	 * 
	 * @param containerId
	 *            the container where the program to run is located
	 * @return the output of the program
	 */
	public String execute(String containerId) {
		startContainer(containerId);

		ExecCreateCmdResponse cmd = docker.execCreateCmd(containerId).withCmd("/bin/sh", "-c", "/home/program.exe 2>&1")
				.withAttachStdout().exec();
		InputStream cmdStream = docker.execStartCmd(containerId).withExecId(cmd.getId()).exec();

		try {
			return IOUtils.toString(cmdStream);
		} catch (IOException e) {
			// Nothing can be done about an IOException at this point. Throw it
			// back as a runtime exception.
			throw new RuntimeException(e);
		}

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
