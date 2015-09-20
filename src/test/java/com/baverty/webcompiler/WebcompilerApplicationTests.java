package com.baverty.webcompiler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.web.WebAppConfiguration;

import com.baverty.webcompiler.WebcompilerApplication;

import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = WebcompilerApplication.class)
@WebAppConfiguration
public class WebcompilerApplicationTests {

	@Test
	public void contextLoads() {
	}

}
