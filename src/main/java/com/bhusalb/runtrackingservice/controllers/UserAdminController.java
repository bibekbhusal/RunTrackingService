package com.bhusalb.runtrackingservice.controllers;

import com.bhusalb.runtrackingservice.mappers.ObjectIdMapper;
import com.bhusalb.runtrackingservice.models.Roles;
import com.bhusalb.runtrackingservice.models.User;
import com.bhusalb.runtrackingservice.services.UserService;
import com.bhusalb.runtrackingservice.views.ListResponse;
import com.bhusalb.runtrackingservice.views.SearchRequest;
import com.bhusalb.runtrackingservice.views.SearchUserQuery;
import com.bhusalb.runtrackingservice.views.UpdateUserRequest;
import com.bhusalb.runtrackingservice.views.UserView;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import javax.validation.Valid;

@Tag (name = "User")
@RestController
@RequestMapping (path = "/v1/users/")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserAdminController {

    private final UserService userService;
    private final ObjectIdMapper objectIdMapper;

    @PreAuthorize ("hasRole('USER_MANAGER') || hasRole('ADMIN') || " +
        "(@objectIdMapperImpl.stringToObjectId(#id) == principal.id)")
    @PutMapping ("{id}")
    public UserView update (@PathVariable String id, @RequestBody @Valid UpdateUserRequest request,
                            final Authentication authentication) {
        final User updatingUser = userService.getUserById(objectIdMapper.stringToObjectId(id));
        final User currentUser = (User) authentication.getPrincipal();

        if (!hasWritePermission(currentUser, updatingUser)) {
            log.warn("Current user {} does not have update permission on updating user {}.",
                authentication.getPrincipal(), updatingUser);
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN,
                "Not authorized to update user with higher/equal privileges.");
        }

        final int highestRankToUpdate = Roles.getHighestRank(Roles.getRoles(request.getRoles()));
        log.info("Highest rank to update: {}.", highestRankToUpdate);

        final int currentUserHighestRank = Roles.getHighestRank(currentUser.getRoles());
        log.info("Current users highest rank: {}.", currentUserHighestRank);

        if (highestRankToUpdate > 0 && currentUserHighestRank <= highestRankToUpdate) {
            log.warn("Cannot update to higher privileges. Current roles: {}, requested roles: {}.",
                currentUser.getRoles(), request.getRoles());
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN,
                "Not authorized to update to higher/equals privileges.");
        }

        return userService.update(new ObjectId(id), request);
    }

    @PreAuthorize ("hasRole('USER_MANAGER') || hasRole('ADMIN') || " +
        "(@objectIdMapperImpl.stringToObjectId(#id) == principal.id)")
    @DeleteMapping ("{id}")
    public UserView delete (@PathVariable String id, final Authentication authentication) {
        final User updatingUser = userService.getUserById(objectIdMapper.stringToObjectId(id));
        final User currentUser = (User) authentication.getPrincipal();

        if (!hasWritePermission(currentUser, updatingUser)) {
            log.warn("Current user {} does not have delete permission on updating user {}.",
                authentication.getPrincipal(), updatingUser);
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "Not authorized to delete user with equal/higher" +
                " privileges.");
        }
        return userService.delete(new ObjectId(id));
    }

    @PreAuthorize ("hasRole('USER_MANAGER') || hasRole('ADMIN') || " +
        "(@objectIdMapperImpl.stringToObjectId(#id) == principal.id)")
    @GetMapping ("{id}")
    public UserView get (@PathVariable String id) {
        return userService.getUser(objectIdMapper.stringToObjectId(id));
    }

    @PreAuthorize ("hasRole('USER_MANAGER') || hasRole('ADMIN')")
    @PostMapping ("search")
    public ListResponse<UserView> search (@RequestBody @Valid SearchRequest<SearchUserQuery> request) {
        return new ListResponse<>(userService.searchUsers(request.getPage(), request.getQuery()));
    }

    private static boolean hasWritePermission (final User currentUser, final User updatingUser) {
        if (currentUser.getId().equals(updatingUser.getId())) return true;

        final int updatingUserHighestRank = Roles.getHighestRank(updatingUser.getRoles());
        final int currentUserHighestRank = Roles.getHighestRank((currentUser).getRoles());
        return updatingUserHighestRank < currentUserHighestRank;
    }
}
