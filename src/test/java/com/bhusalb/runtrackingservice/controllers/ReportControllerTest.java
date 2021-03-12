package com.bhusalb.runtrackingservice.controllers;

import com.bhusalb.runtrackingservice.helper.data.RunTestDataHelper;
import com.bhusalb.runtrackingservice.helper.data.UserTestDataHelper;
import com.bhusalb.runtrackingservice.views.Coordinates;
import com.bhusalb.runtrackingservice.views.RunReport;
import com.bhusalb.runtrackingservice.views.UserView;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

import static com.bhusalb.runtrackingservice.helper.JsonHelper.fromJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RunTestDataHelper runTestDataHelper;
    @Autowired
    private UserTestDataHelper userTestDataHelper;

    @WithUserDetails ("admin@admin.test")
    @Test
    void get_report () throws Exception {
        final String email = String.format("%s@email", ThreadLocalRandom.current().nextDouble(1002113.0422120));
        final UserView user = userTestDataHelper.createUser(email, "password", "fullname");

        final LocalDateTime startDate = LocalDate.now().minusDays(10).atStartOfDay();
        final Coordinates coordinates = new Coordinates(45.5426916, -122.724366);
        final double distance = 1000.0;
        final int duration = 200;

        runTestDataHelper.create(user.getEmail(), distance, duration, startDate, coordinates);
        runTestDataHelper.create(user.getEmail(), distance + 100, duration + 10,
            startDate.minusDays(2), coordinates);
        runTestDataHelper.create(user.getEmail(), distance + 200, duration + 20,
            startDate.plusDays(1), coordinates);
        runTestDataHelper.create(user.getEmail(), distance + 300, duration + 30,
            startDate.plusDays(3), coordinates);

        final String url = String.format("/v1/reports/%s/?startDate=%s", user.getId(),
            startDate.minusDays(10).toLocalDate().format(DateTimeFormatter.ISO_DATE));

        final MvcResult reportResult = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();

        final RunReport report = fromJson(objectMapper, reportResult.getResponse().getContentAsString(),
            RunReport.class);

        assertThat(report).isNotNull();
        assertThat(report.getTotalRunsInPeriod()).isEqualTo(4);
        assertThat(report.getEarliestRunDate()).isEqualTo(startDate.minusDays(2));
        assertThat(report.getLatestRunDate()).isEqualTo(startDate.plusDays(3));
        assertThat(report.getAverageDistance()).isEqualTo(1150);
        assertThat(report.getAverageDuration()).isEqualTo(215);
        assertThat(report.getAveragePace()).isEqualTo(5.33, within(0.2));
    }
}