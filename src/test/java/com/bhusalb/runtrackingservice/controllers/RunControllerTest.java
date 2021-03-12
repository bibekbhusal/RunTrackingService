package com.bhusalb.runtrackingservice.controllers;

import com.bhusalb.runtrackingservice.helper.data.RunTestDataHelper;
import com.bhusalb.runtrackingservice.helper.data.UserTestDataHelper;
import com.bhusalb.runtrackingservice.views.AdvanceSearchQuery;
import com.bhusalb.runtrackingservice.views.Coordinates;
import com.bhusalb.runtrackingservice.views.CreateRunRequest;
import com.bhusalb.runtrackingservice.views.ListResponse;
import com.bhusalb.runtrackingservice.views.Page;
import com.bhusalb.runtrackingservice.views.RunView;
import com.bhusalb.runtrackingservice.views.SearchRequest;
import com.bhusalb.runtrackingservice.views.SearchRunQuery;
import com.bhusalb.runtrackingservice.views.UpdateRunRequest;
import com.bhusalb.runtrackingservice.views.UserView;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.bhusalb.runtrackingservice.helper.JsonHelper.fromJson;
import static com.bhusalb.runtrackingservice.helper.JsonHelper.toJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RunControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserTestDataHelper userTestDataHelper;
    @Autowired
    private RunTestDataHelper runTestDataHelper;

    @WithUserDetails ("user@user.test")
    @Test
    void create_run_success () throws Exception {
        final UserView userView = userTestDataHelper.load("user@user.test");

        final LocalDateTime startDate = LocalDate.now().atStartOfDay();
        final Coordinates coordinates = new Coordinates(47.12222, -127.323135);
        final double distance = 1111.1;
        final int duration = 1100;

        final CreateRunRequest request = new CreateRunRequest();
        request.setOwnerId(userView.getId());
        request.setDistance(distance);
        request.setDuration(duration);
        request.setStartDate(startDate);
        request.setCoordinates(coordinates);

        final MvcResult result = mockMvc.perform(post("/v1/runs/")
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(objectMapper, request))
        ).andExpect(status().isOk()).andReturn();

        final RunView view = fromJson(objectMapper, result.getResponse().getContentAsString(), RunView.class);

        assertThat(view).isNotNull();
        assertThat(view.getOwner()).isEqualTo(userView);
        assertThat(view.getDistance()).isEqualTo(distance);
        assertThat(view.getDuration()).isEqualTo(duration);
        assertThat(view.getCoordinates()).isEqualTo(coordinates);
        assertThat(view.getStartDate()).isEqualTo(startDate);
    }

    @WithUserDetails ("manager@manager.test")
    @Test
    void create_run_forbidden () throws Exception {
        final UserView userView = userTestDataHelper.load("user@user.test");

        final LocalDateTime startDate = LocalDate.now().atStartOfDay();
        final Coordinates coordinates = new Coordinates(47.12222, -127.323135);
        final double distance = 1111.1;
        final int duration = 1100;

        final CreateRunRequest request = new CreateRunRequest();
        request.setOwnerId(userView.getId());
        request.setDistance(distance);
        request.setDuration(duration);
        request.setStartDate(startDate);
        request.setCoordinates(coordinates);

        mockMvc.perform(post("/v1/runs/")
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(objectMapper, request))
        ).andExpect(status().isForbidden()).andReturn();
    }

    @WithUserDetails ("user@user.test")
    @Test
    void update () throws Exception {
        final UserView userView = userTestDataHelper.load("user@user.test");

        final LocalDateTime startDate = LocalDate.now().atStartOfDay();
        final Coordinates coordinates = new Coordinates(47.12222, -127.323135);
        final double distance = 1111.1;
        final int duration = 1100;

        final RunView createView = runTestDataHelper.create(userView.getEmail(), distance, duration, startDate,
            coordinates);

        final UpdateRunRequest updateRunRequest = new UpdateRunRequest();
        updateRunRequest.setDistance(2001.5);
        updateRunRequest.setDuration(421);

        final MvcResult updateResult = mockMvc.perform(put("/v1/runs/" + createView.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(objectMapper, updateRunRequest))
        ).andExpect(status().isOk()).andReturn();

        final RunView updateView = fromJson(objectMapper, updateResult.getResponse().getContentAsString(),
            RunView.class);

        assertThat(updateView).isNotNull();
        assertThat(updateView.getOwner()).isEqualTo(userView);
        assertThat(updateView.getStartDate()).isEqualTo(startDate);
        assertThat(updateView.getDuration()).isEqualTo(421);
        assertThat(updateView.getDistance()).isEqualTo(2001.5);
        assertThat(updateView.getCoordinates()).isEqualTo(coordinates);
    }

    @WithUserDetails ("user@user.test")
    @Test
    void delete_run () throws Exception {
        final UserView userView = userTestDataHelper.load("user@user.test");

        final LocalDateTime startDate = LocalDate.now().atStartOfDay();
        final Coordinates coordinates = new Coordinates(47.12222, -127.323135);
        final double distance = 1111.1;
        final int duration = 1100;

        final RunView createView = runTestDataHelper.create(userView.getEmail(), distance, duration, startDate,
            coordinates);

        final MvcResult deleteResult = mockMvc.perform(
            delete("/v1/runs/" + createView.getId()))
            .andExpect(status().isOk()).andReturn();

        final RunView deletedView = fromJson(objectMapper, deleteResult.getResponse().getContentAsString(),
            RunView.class);

        assertThat(deletedView).isNotNull();
        assertThat(deletedView).isEqualTo(createView);
    }

    @WithUserDetails ("user@user.test")
    @Test
    void get_run () throws Exception {
        final UserView userView = userTestDataHelper.load("user@user.test");

        final LocalDateTime startDate = LocalDate.now().atStartOfDay();
        final Coordinates coordinates = new Coordinates(45.5426916, -122.724366);
        final double distance = 1111.1;
        final int duration = 1100;

        final RunView createView = runTestDataHelper.create(userView.getEmail(), distance, duration, startDate,
            coordinates);

        // Waiting for weather query to complete...
        Thread.sleep(6000);

        final MvcResult getResult = mockMvc.perform(
            get("/v1/runs/" + createView.getId()))
            .andExpect(status().isOk()).andReturn();

        final RunView getView = fromJson(objectMapper, getResult.getResponse().getContentAsString(), RunView.class);

        assertThat(getView).isNotNull();
        assertThat(getView.getOwner()).isEqualTo(createView.getOwner());
        assertThat(getView.getId()).isEqualTo(createView.getId());
        assertThat(getView.getDistance()).isEqualTo(createView.getDistance());
        assertThat(getView.getDuration()).isEqualTo(createView.getDuration());
        assertThat(getView.getCoordinates()).isEqualTo(createView.getCoordinates());
        assertThat(getView.getWeather()).isNotNull();
    }

    @WithUserDetails ("admin@admin.test")
    @Test
    void search () throws Exception {
        final LocalDateTime startDate = LocalDate.now().atStartOfDay();
        final Coordinates coordinates = new Coordinates(45.5426916, -122.724366);
        final double distance = 1111.1;
        final int duration = 1100;

        runTestDataHelper.create("admin@admin.test", distance, duration, startDate,
            coordinates);
        runTestDataHelper.create("admin@admin.test", distance + 1, duration + 3,
            startDate.minusDays(2), coordinates);
        runTestDataHelper.create("admin@admin.test", distance + 2, duration + 5,
            startDate.minusDays(1), coordinates);

        final SearchRequest<SearchRunQuery> request = new SearchRequest<>();
        request.setQuery(new SearchRunQuery());
        request.getQuery().setDateEnd(startDate.toLocalDate());
        request.getQuery().setMinDistance(distance);
        request.getQuery().setMinDuration(duration);
        request.setPage(new Page());
        request.getPage().setNumber(1);
        request.getPage().setLimit(10);

        final MvcResult updateResult = mockMvc.perform(post("/v1/runs/search")
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(objectMapper, request))
        ).andExpect(status().isOk()).andReturn();

        final ListResponse<UserView> list =
            fromJson(objectMapper, updateResult.getResponse().getContentAsString(), ListResponse.class);

        assertThat(list).isNotNull();
        assertThat(list.getItems()).isNotEmpty();
        assertThat(list.getItems().size()).isGreaterThanOrEqualTo(3);
    }

    @WithUserDetails ("admin@admin.test")
    @Test
    void search_advance () throws Exception {
        final LocalDateTime startDate = LocalDate.now().atStartOfDay();
        final Coordinates coordinates = new Coordinates(45.5426916, -122.724366);
        final double distance = 1111.1;
        final int duration = 1100;

        runTestDataHelper.create("admin@admin.test", distance, duration, startDate,
            coordinates);
        runTestDataHelper.create("admin@admin.test", distance + 1, duration - 3,
            startDate.minusDays(2), coordinates);
        runTestDataHelper.create("admin@admin.test", distance + 2, duration + 5,
            startDate.minusDays(1), coordinates);

        final String query = String.format("((duration gt %s) OR (duration lt %s)) AND (startDate lt %s) " +
            "AND (distance lt %s )", duration - 4, duration + 6,
            startDate.plusDays(1).format(DateTimeFormatter.ISO_DATE_TIME), distance + 5);

        final SearchRequest<AdvanceSearchQuery> request = new SearchRequest<>();
        request.setQuery(new AdvanceSearchQuery());
        request.getQuery().setQueryString(query);
        request.setPage(new Page());
        request.getPage().setNumber(1);
        request.getPage().setLimit(10);

        final MvcResult updateResult = mockMvc.perform(post("/v1/runs/advanceSearch")
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(objectMapper, request))
        ).andExpect(status().isOk()).andReturn();

        final ListResponse<UserView> list =
            fromJson(objectMapper, updateResult.getResponse().getContentAsString(), ListResponse.class);

        assertThat(list).isNotNull();
        assertThat(list.getItems()).isNotEmpty();
        assertThat(list.getItems().size()).isGreaterThanOrEqualTo(3);
    }
}