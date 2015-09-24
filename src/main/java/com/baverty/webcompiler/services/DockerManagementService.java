package com.baverty.webcompiler.services;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.stereotype.Service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.core.DockerClientBuilder;

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
		
		CreateContainerResponse container = docker.createContainerCmd("frolvlad/alpine-gcc")
				   .withCmd("tail", "-f", "/dev/null")
				   .withExposedPorts(new ExposedPort(8080))
				   .withPublishAllPorts(true)
				   .withTty(true)
				   .exec();
		
		containers.put(container.getId(), container);

		return container.getId();
	}

	public void compile(String sourceCode, String containerId) {
		// Start the container
		docker.startContainerCmd(containerId).exec();
		
		// Transfer the sourcecode to the container
		this.transferSourceCode(sourceCode, containerId);	
	}
	
	private void transferSourceCode(String sourceCode, String containerId) {
		// Create the command to listen to the file
		ExecCreateCmdResponse cmd = docker.execCreateCmd(containerId)
											.withCmd("/bin/sh", "-c", "nc -l -p 8080 > /root/test")
											.withTty()
											.exec();
		
		// Start listening to inbound connections
		docker.execStartCmd(containerId).withExecId(cmd.getId()).withDetach().exec();
		
		// Retrieve the port on the host machine		
		Integer hostPort = docker.inspectContainerCmd(containerId).exec()
				.getNetworkSettings().getPorts().getBindings().get(new ExposedPort(8080))[0].getHostPort();
		
		// Connect to the port and send sourcecode
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
	
	@PreDestroy
	private void removeContainers() {
		for (String containerId : containers.keySet()) {
			docker.stopContainerCmd(containerId).exec();
			docker.removeContainerCmd(containerId).exec();
		}
	}
}
