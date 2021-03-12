package com.bhusalb.runtrackingservice;

import com.bhusalb.runtrackingservice.controllers.ReportController;
import com.bhusalb.runtrackingservice.controllers.RunController;
import com.bhusalb.runtrackingservice.controllers.UserAdminController;
import com.bhusalb.runtrackingservice.controllers.UserAuthController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SmokeTest {

	@Autowired
	private ReportController reportController;

	@Autowired
	private RunController runController;

	@Autowired
	private UserAdminController userAdminController;

	@Autowired
	private UserAuthController userAuthController;

	@Test
	void contextLoads() {
		assertThat(reportController).isNotNull();
		assertThat(runController).isNotNull();
		assertThat(userAdminController).isNotNull();
		assertThat(userAuthController).isNotNull();
	}

}
