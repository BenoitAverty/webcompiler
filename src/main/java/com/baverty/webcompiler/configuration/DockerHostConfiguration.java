package com.baverty.webcompiler.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix="docker.host", ignoreUnknownFields=false)
public class DockerHostConfiguration {
	@Getter
	@Setter
	private String address;
}
