package com.baverty.webcompiler;

import java.sql.SQLException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class WebcompilerApplication {
	
    public static void main(String[] args) {
    	

        try {
			org.h2.tools.Server.createWebServer("-webPort", "10500").start();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
        SpringApplication.run(WebcompilerApplication.class, args);
        
    }
}
