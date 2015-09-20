package com.baverty.webcompiler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class WebcompilerApplication {
	
    public static void main(String[] args) {
        SpringApplication.run(WebcompilerApplication.class, args);
    }
}
