package com.bhusalb.runtrackingservice.controllers;

import com.bhusalb.runtrackingservice.helper.data.UserTestDataHelper;
import com.bhusalb.runtrackingservice.views.CreateUserRequest;
import com.bhusalb.runtrackingservice.views.LoginRequest;
import com.bhusalb.runtrackingservice.views.LoginResponse;
import com.bhusalb.runtrackingservice.views.UserView;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

import static com.bhusalb.runtrackingservice.helper.JsonHelper.fromJson;
import static com.bhusalb.runtrackingservice.helper.JsonHelper.toJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserTestDataHelper factory;

    @Test
    void create () throws Exception {
        final String email = "user@email";
        final String name = "name";

        final CreateUserRequest request = new CreateUserRequest();
        request.setEmail(email);
        request.setPassword("password");
        request.setFullName(name);

        final MvcResult result = mockMvc.perform(post("/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(objectMapper, request))
        ).andExpect(status().isOk()).andReturn();

        final UserView user = fromJson(objectMapper, result.getResponse().getContentAsString(), UserView.class);
        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getFullName()).isEqualTo(name);
        assertThat(user.getRoles()).isNotEmpty();
        assertThat(user.getRoles()).contains("USER");
    }

    @Test
    void create_admin_failure () throws Exception {
        final CreateUserRequest request = new CreateUserRequest();
        request.setEmail("email@email");
        request.setPassword("password");
        request.setFullName("name");
        request.setRoles(Collections.singleton("ADMIN"));

        final MvcResult result = mockMvc.perform(post("/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(objectMapper, request))
        ).andExpect(status().isForbidden()).andReturn();
    }

    @Test
    void create_user_manager_failure () throws Exception {
        final CreateUserRequest request = new CreateUserRequest();
        request.setEmail("email@email");
        request.setPassword("password");
        request.setFullName("name");
        request.setRoles(Collections.singleton("USER_MANAGER"));

        final MvcResult result = mockMvc.perform(post("/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(objectMapper, request))
        ).andExpect(status().isForbidden()).andReturn();
    }

    @Test
    void create_admin_success () throws Exception {
        final String email = "admin@email";
        final String name = "name";

        final CreateUserRequest request = new CreateUserRequest();
        request.setEmail(email);
        request.setPassword("password");
        request.setFullName(name);
        request.setRoles(Collections.singleton("ADMIN"));

        final MvcResult result = mockMvc.perform(post("/v1/auth/register/secured")
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(objectMapper, request))
        ).andExpect(status().isOk()).andReturn();

        final UserView user = fromJson(objectMapper, result.getResponse().getContentAsString(), UserView.class);
        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getFullName()).isEqualTo(name);
        assertThat(user.getRoles()).isNotEmpty();
        assertThat(user.getRoles()).contains("ADMIN");
    }

    @Test
    void login () throws Exception {

        final String email = String.format("%s@email", ThreadLocalRandom.current().nextInt(1000));

        final CreateUserRequest request = new CreateUserRequest();
        request.setEmail(email);
        request.setPassword("password");
        request.setFullName("name");
        request.setRoles(Collections.singleton("USER_MANAGER"));

        mockMvc.perform(post("/v1/auth/register/secured")
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(objectMapper, request))
        ).andExpect(status().isOk()).andReturn();

        final LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword("password");

        final MvcResult result = mockMvc.perform(post("/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(objectMapper, loginRequest))
        ).andExpect(status().isOk()).andReturn();

        final LoginResponse response = fromJson(objectMapper, result.getResponse().getContentAsString(),
            LoginResponse.class);
        assertThat(response).isNotNull();
        assertThat(response.getAuthToken()).isNotBlank();
        assertThat(response.getUserView()).isNotNull();
        assertThat(response.getUserView().getEmail()).isEqualTo(email);
        assertThat(response.getUserView().getFullName()).isEqualTo("name");
        assertThat(response.getUserView().getRoles()).contains("USER_MANAGER");
    }
}