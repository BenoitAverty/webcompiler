package com.baverty.webcompiler.services;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.stereotype.Service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
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
				   .withCmd("touch", "/test")
				   .exec();
		
		containers.put(container.getId(), container);

		return container.getId();
	}

	public void compile(String sourceCode, String containerId) {
		// TODO Auto-generated method stub
		
	}
	
	@PreDestroy
	private void removeContainers() {
		for (String containerId : containers.keySet()) {
			docker.removeContainerCmd(containerId).exec();
		}
	}
}
