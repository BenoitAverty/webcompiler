package com.baverty.webcompiler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

import com.baverty.webcompiler.configuration.DockerClientConfiguration;

@SpringBootApplication
@EnableAsync
public class WebcompilerApplication {
	
    public static void main(String[] args) {    	
        SpringApplication.run(WebcompilerApplication.class, args);
    }
}
