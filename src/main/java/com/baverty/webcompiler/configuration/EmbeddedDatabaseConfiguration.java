package com.baverty.webcompiler.configuration;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddedDatabaseConfiguration implements ServletContextInitializer {

	private final Logger log = LoggerFactory.getLogger(EmbeddedDatabaseConfiguration.class);
	
	@Override
	public void onStartup(ServletContext ctx) throws ServletException {
		log.debug("Initialize H2 console");
        ServletRegistration.Dynamic h2ConsoleServlet = ctx.addServlet("H2Console", new org.h2.server.web.WebServlet());
        h2ConsoleServlet.addMapping("/console/*");
        h2ConsoleServlet.setInitParameter("-properties", "src/main/resources");
        h2ConsoleServlet.setLoadOnStartup(1);
	}
	
	
	
}
