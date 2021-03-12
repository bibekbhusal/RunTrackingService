package com.bhusalb.runtrackingservice.controllers;

import com.bhusalb.runtrackingservice.helper.data.UserTestDataHelper;
import com.bhusalb.runtrackingservice.views.AdvanceSearchQuery;
import com.bhusalb.runtrackingservice.views.CreateUserRequest;
import com.bhusalb.runtrackingservice.views.ListResponse;
import com.bhusalb.runtrackingservice.views.Page;
import com.bhusalb.runtrackingservice.views.SearchRequest;
import com.bhusalb.runtrackingservice.views.SearchUserQuery;
import com.bhusalb.runtrackingservice.views.UpdateUserRequest;
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
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

import static com.bhusalb.runtrackingservice.helper.JsonHelper.fromJson;
import static com.bhusalb.runtrackingservice.helper.JsonHelper.toJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserTestDataHelper factory;

    @WithUserDetails ("user@user.test")
    @Test
    void update () throws Exception {
        final UserView user = factory.load("user@user.test");
        final String userId = user.getId();

        final UpdateUserRequest request = new UpdateUserRequest();
        request.setFullName("Temporary");

        final MvcResult result = mockMvc.perform(put("/v1/users/" + userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(objectMapper, request))
        ).andExpect(status().isOk()).andReturn();

        final UserView view = fromJson(objectMapper, result.getResponse().getContentAsString(), UserView.class);

        assertThat(view).isNotNull();
        assertThat(view.getFullName()).isEqualTo("Temporary");
    }

    @WithUserDetails ("user@user.test")
    @Test
    void update_unauthorized () throws Exception {
        final UserView user = factory.load("manager@manager.test");
        final String userId = user.getId();

        final UpdateUserRequest request = new UpdateUserRequest();
        request.setFullName("Temporary");

        mockMvc.perform(put("/v1/users/" + userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(objectMapper, request))
        ).andExpect(status().isForbidden()).andReturn();
    }

    @WithUserDetails ("manager@manager.test")
    @Test
    void delete_user () throws Exception {
        final String email = String.format("%s@email", ThreadLocalRandom.current().nextInt(10000000));

        final CreateUserRequest request = new CreateUserRequest();
        request.setEmail(email);
        request.setPassword("password");
        request.setFullName("name");
        request.setRoles(Collections.singleton("USER"));

        final MvcResult result = mockMvc.perform(post("/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(objectMapper, request))
        ).andExpect(status().isOk()).andReturn();

        final UserView createdUser = fromJson(objectMapper, result.getResponse().getContentAsString(), UserView.class);

        final MvcResult deleteResult = mockMvc.perform(delete("/v1/users/" + createdUser.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(objectMapper, request))
        ).andExpect(status().isOk()).andReturn();

        final UserView deletedUser = fromJson(objectMapper, deleteResult.getResponse().getContentAsString(), UserView.class);

        assertThat(deletedUser).isEqualTo(createdUser);
    }

    @WithUserDetails ("admin@admin.test")
    @Test
    void get_user () throws Exception {
        final UserView user = factory.load("manager@manager.test");
        final String userId = user.getId();

        final MvcResult result = mockMvc.perform(get("/v1/users/" + userId))
            .andExpect(status().isOk()).andReturn();

        final UserView getUser = fromJson(objectMapper, result.getResponse().getContentAsString(), UserView.class);
        assertThat(getUser).isNotNull();
        assertThat(getUser.getEmail()).isEqualTo("manager@manager.test");
    }

    @WithUserDetails ("manager@manager.test")
    @Test
    void search () throws Exception {
        final SearchRequest<SearchUserQuery> request = new SearchRequest<>();
        request.setQuery(new SearchUserQuery());
        request.getQuery().setEmail("@");
        request.setPage(new Page());
        request.getPage().setNumber(1);
        request.getPage().setLimit(10);

        final MvcResult result = mockMvc.perform(post("/v1/users/search")
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(objectMapper, request))
        ).andExpect(status().isOk()).andReturn();

        final ListResponse<UserView> list =
            fromJson(objectMapper, result.getResponse().getContentAsString(), ListResponse.class);

        assertThat(list).isNotNull();
        assertThat(list.getItems()).isNotEmpty();
    }

    @WithUserDetails ("admin@admin.test")
    @Test
    void advanceSearch () throws Exception {

        final UserView user = factory.load("admin@admin.test");
        final String creatorId = user.getId();
        final LocalDateTime beforeCreation = LocalDate.now().atStartOfDay();

        final String email = String.format("%s@email", ThreadLocalRandom.current().nextInt(10000000));

        final CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setEmail(email);
        createUserRequest.setPassword("password");
        createUserRequest.setFullName("advance search user");
        createUserRequest.setRoles(Collections.singleton("USER"));

        mockMvc.perform(post("/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(objectMapper, createUserRequest))
        ).andExpect(status().isOk()).andReturn();

        final String query = String.format("(createdBy eq %s) AND (created gt %s)", creatorId,
            beforeCreation.format(DateTimeFormatter.ISO_DATE_TIME));

        final SearchRequest<AdvanceSearchQuery> request = new SearchRequest<>();
        request.setQuery(new AdvanceSearchQuery());
        request.getQuery().setQueryString(query);
        request.setPage(new Page());
        request.getPage().setNumber(1);
        request.getPage().setLimit(10);

        final MvcResult searchResult = mockMvc.perform(post("/v1/users/advanceSearch")
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(objectMapper, request))
        ).andExpect(status().isOk()).andDo(print()).andReturn();

        final ListResponse<UserView> list = fromJson(objectMapper, searchResult.getResponse().getContentAsString(),
            ListResponse.class);

        assertThat(list).isNotNull();
        assertThat(list.getItems()).isNotEmpty();
    }
}