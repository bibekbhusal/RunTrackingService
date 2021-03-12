package com.bhusalb.runtrackingservice.helper.data;

import com.bhusalb.runtrackingservice.services.UserService;
import com.bhusalb.runtrackingservice.views.CreateUserRequest;
import com.bhusalb.runtrackingservice.views.UserView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

@Service
public class UserTestDataHelper {

    @Autowired
    private UserService userService;

    public UserView createUser (final String email,
                                final String password,
                                final String fullName,
                                final Set<String> roles) {
        final CreateUserRequest request = new CreateUserRequest();
        request.setEmail(email);
        request.setPassword(password);
        request.setFullName(fullName);
        request.setRoles(roles);

        return userService.create(request);
    }

    public UserView createUser (final String email,
                                final String password,
                                final String fullName) {
        return createUser(email, password, fullName, Collections.emptySet());
    }

    public UserView load (final String email) {
        return userService.getByEmail(email);
    }
}
