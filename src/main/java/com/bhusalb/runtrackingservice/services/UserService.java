package com.bhusalb.runtrackingservice.services;

import com.bhusalb.runtrackingservice.mappers.UserEditMapper;
import com.bhusalb.runtrackingservice.mappers.UserViewMapper;
import com.bhusalb.runtrackingservice.models.Roles;
import com.bhusalb.runtrackingservice.models.User;
import com.bhusalb.runtrackingservice.repos.UserRepository;
import com.bhusalb.runtrackingservice.views.CreateUserRequest;
import com.bhusalb.runtrackingservice.views.Page;
import com.bhusalb.runtrackingservice.views.SearchUserQuery;
import com.bhusalb.runtrackingservice.views.UpdateUserRequest;
import com.bhusalb.runtrackingservice.views.UserView;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.validation.ValidationException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserEditMapper userEditMapper;
    private final UserViewMapper userViewMapper;

    @Transactional
    public UserView create (final CreateUserRequest createUserRequest) {
        if (userRepository.existsByEmail(createUserRequest.getEmail())) {
            final String message = String.format("User with email: %s already exists.", createUserRequest.getEmail());
            log.error(message);
            throw new ValidationException(message);
        }

        final User user = userEditMapper.create(createUserRequest);

        // Set default role if no role is provided.
        if (CollectionUtils.isEmpty(user.getRoles())) {
            user.setRoles(Sets.newHashSet(Roles.USER));
        }

        return userViewMapper.toUserView(userRepository.save(user));
    }

    @Transactional
    public UserView update (final ObjectId id, final UpdateUserRequest updateUserRequest) {
        final User user = userRepository.getById(id);
        userEditMapper.update(updateUserRequest, user);
        return userViewMapper.toUserView(userRepository.save(user));
    }

    @Transactional
    public UserView delete (final ObjectId id) {
        final User user = userRepository.getById(id);
        userRepository.deleteById(id);
        return userViewMapper.toUserView(user);
    }

    @Override
    public UserDetails loadUserByUsername (final String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username).orElseThrow(
            () -> {
                final String message = String.format("User does not exist with given username: %s.", username);
                log.error(message);
                return new UsernameNotFoundException(message);
            }
        );
    }

    public UserView getUser (final ObjectId objectId) {
        return userViewMapper.toUserView(userRepository.getById(objectId));
    }

    public User getUserById(final ObjectId objectId) {
        return userRepository.getById(objectId);
    }

    public boolean doesUserExist(final ObjectId objectId) {
        return userRepository.existsById(objectId);
    }

    public List<UserView> searchUsers (final Page page, final SearchUserQuery query) {
        final List<User> users = userRepository.searchUsers(page, query);
        return userViewMapper.toUserViews(users);
    }
}
