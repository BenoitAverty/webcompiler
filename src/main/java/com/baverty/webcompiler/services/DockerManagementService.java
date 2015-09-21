package com.baverty.webcompiler.services;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;

@Service
public class DockerManagementService {

	private DockerClient docker;	
	
	@PostConstruct
	private void init() throws DockerCertificateException {
		docker = DefaultDockerClient.fromEnv().build();
	}
	
	public String getContainer() {
		
		try {
			docker.info();
		} catch (DockerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	public void compile(String sourceCode, String containerId) {
		// TODO Auto-generated method stub
		
	}
}
