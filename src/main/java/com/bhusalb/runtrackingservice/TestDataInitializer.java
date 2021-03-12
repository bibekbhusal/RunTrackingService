package com.bhusalb.runtrackingservice;

import com.bhusalb.runtrackingservice.mappers.ObjectIdMapper;
import com.bhusalb.runtrackingservice.services.UserService;
import com.bhusalb.runtrackingservice.views.CreateUserRequest;
import com.bhusalb.runtrackingservice.views.UserView;
import io.jsonwebtoken.lang.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@Slf4j
public class TestDataInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private static final Map<String, String> TEST_USERS =
        Maps.of("user@user.test", "USER")
            .and("manager@manager.test", "USER_MANAGER")
            .and("admin@admin.test", "ADMIN")
            .build();

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectIdMapper objectIdMapper;

    private final Set<String> objectIds = new HashSet<>();

    @Override
    public void onApplicationEvent (final ApplicationReadyEvent event) {
        log.info("Creating test users....");
        TEST_USERS.forEach((k, v) -> {
            final CreateUserRequest request = new CreateUserRequest();
            request.setEmail(k);
            request.setPassword("password");
            request.setFullName(k);
            request.setRoles(Collections.singleton(v));

            final UserView user = userService.create(request);
            objectIds.add(user.getId());
        });
    }

    @PreDestroy
    private void onDestroy () {
        log.info("Removing test users....");
        objectIds.forEach(id -> userService.delete(objectIdMapper.stringToObjectId(id)));
    }
}
