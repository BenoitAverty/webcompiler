package com.baverty.webcompiler.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix="docker.client", ignoreUnknownFields=false)
public class DockerClientConfiguration {

	@Getter
	@Setter
	private String socket;
	
	@Bean
	public DockerClient dockerClient() {
		
		DockerClientBuilder builder = DockerClientBuilder.getInstance(socket);
		
		return builder.build();
	}
}
