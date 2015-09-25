package com.baverty.webcompiler.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.core.DockerClientBuilder;

/**
 * Service used for all the interactions of the application with docker
 * containers.
 * 
 * @author baverty
 */
@Service
public class DockerManagementService {

	/**
	 * The docker client used to issue commands to the docker host.
	 */
	private DockerClient docker;

	/**
	 * The set of containers created by the service.
	 */
	private Map<String, CreateContainerResponse> containers;

	@PostConstruct
	private void init() {
		docker = DockerClientBuilder.getInstance("unix:///var/run/docker.sock").build();
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
		try {
			Socket s = new Socket("localhost", hostPort);
			OutputStreamWriter os = new OutputStreamWriter(s.getOutputStream());
			os.write(sourceCode);
			os.flush();
			s.close();

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Compile the code in a container.
	 * 
	 * The source code must already be present in the container (see
	 * {@link #transferSourceCode(String, String)}).
	 * 
	 * @param containerId
	 *            the ID of the container in which to compile the code.
	 * @return TODO
	 */
	public String compile(String containerId) {

		startContainer(containerId);

		// Compile
		ExecCreateCmdResponse cmd = docker.execCreateCmd(containerId)
				.withCmd("gcc", "-o", "/home/program.exe", "/home/program.c").withTty().exec();

		InputStream cmdStream = docker.execStartCmd(containerId).withExecId(cmd.getId()).exec();
		
		try {
			String result = IOUtils.toString(cmdStream, "utf-8");
			cmdStream.close();
			return result;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "failure";
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
