package com.bhusalb.runtrackingservice.controllers;

import com.bhusalb.runtrackingservice.libs.jwt.JsonWebTokenHelper;
import com.bhusalb.runtrackingservice.mappers.UserViewMapper;
import com.bhusalb.runtrackingservice.models.Roles;
import com.bhusalb.runtrackingservice.models.User;
import com.bhusalb.runtrackingservice.services.UserService;
import com.bhusalb.runtrackingservice.views.CreateUserRequest;
import com.bhusalb.runtrackingservice.views.LoginRequest;
import com.bhusalb.runtrackingservice.views.LoginResponse;
import com.bhusalb.runtrackingservice.views.UserView;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import javax.validation.Valid;
import java.util.Optional;

@Tag (name = "Auth")
@RestController
@RequestMapping (path = "/v1/auth/")
@RequiredArgsConstructor
@Slf4j
public class UserAuthController {
    private final UserService userService;
    private final JsonWebTokenHelper jwtHelper;
    private final AuthenticationManager authenticationManager;
    private final UserViewMapper userViewMapper;

    @PostMapping ("register")
    public UserView create (@RequestBody @Valid final CreateUserRequest createUserRequest,
                            final Authentication authentication) {

        final Optional<User> loggedInUser = Optional.ofNullable(authentication)
            .map(Authentication::getPrincipal)
            .filter(p -> p instanceof User)
            .map(p -> (User) p);

        log.info("Logged in user: {}.", loggedInUser.orElse(null));
        log.info("Roles to use: {}.", createUserRequest.getRoles());

        final int highestRankToCreate = Roles.getHighestRank(Roles.getRoles(createUserRequest.getRoles()));

        log.info("Highest rank to create: {}.", highestRankToCreate);

        if (highestRankToCreate > 0) {
            if (!loggedInUser.isPresent()) {
                log.warn("User not logged in to create user of higher privileges. Highest rank to create: {}.",
                    highestRankToCreate);
                throw new HttpClientErrorException(HttpStatus.FORBIDDEN,
                    "Please login to create user of higher privileges.");
            }
            final int userHighestRank = Roles.getHighestRank(loggedInUser.get().getRoles());
            if (userHighestRank <= highestRankToCreate) {
                log.warn("User with lower/equal rank requested to create user with higher/equal rank. " +
                    "Requested rank: {}, current rank: {}.", highestRankToCreate, userHighestRank);
                throw new HttpClientErrorException(HttpStatus.FORBIDDEN,
                    "Not authorized to create users with equal/higher privileges.");
            }
        }
        return userService.create(createUserRequest);
    }

    @PostMapping ("register/secured")
    public UserView createUserAdmin (@RequestBody @Valid final CreateUserRequest createUserRequest) {
        log.info("Roles to use: {}.", createUserRequest.getRoles());
        return userService.create(createUserRequest);
    }

    @PostMapping ("login")
    public LoginResponse login (@RequestBody @Valid final LoginRequest loginRequest) {
        try {
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(), loginRequest.getPassword());
            authentication = authenticationManager.authenticate(authentication);

            final User user = (User) authentication.getPrincipal();
            return new LoginResponse(jwtHelper.generateToken(user), userViewMapper.toUserView(user));
        } catch (final BadCredentialsException ex) {
            log.warn("Invalid login request {}", loginRequest);
            throw new BadCredentialsException("Incorrect username-password combination.");
        }
    }
}
