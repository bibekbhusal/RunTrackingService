package com.bhusalb.runtrackingservice.mappers;

import com.bhusalb.runtrackingservice.models.User;
import com.bhusalb.runtrackingservice.views.CreateUserRequest;
import com.bhusalb.runtrackingservice.views.UpdateUserRequest;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.CollectionUtils;

import javax.validation.ValidationException;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper (componentModel = "spring", uses = {ObjectIdMapper.class})
public abstract class UserEditMapper {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleMapper roleMapper;

    @Mapping (target = "roles", ignore = true)
    @Mapping (target = "password", ignore = true)
    public abstract User create (final CreateUserRequest createUserRequest);

    @BeanMapping (nullValueCheckStrategy = ALWAYS, nullValuePropertyMappingStrategy = IGNORE)
    @Mapping (target = "roles", ignore = true)
    @Mapping (target = "password", ignore = true)
    public abstract void update (final UpdateUserRequest updateUserRequest, @MappingTarget final User user);

    @AfterMapping
    protected void afterCreate (final CreateUserRequest createUserRequest, @MappingTarget final User user) {
        if (createUserRequest != null) {
            if (!CollectionUtils.isEmpty(createUserRequest.getRoles())) {
                user.setRoles(roleMapper.toRoles(createUserRequest.getRoles()));
            }
            user.setPassword(passwordEncoder.encode(createUserRequest.getPassword()));
        }
    }

    @AfterMapping
    protected void afterUpdate (final UpdateUserRequest updateUserRequest, @MappingTarget final User user) {
        if (updateUserRequest != null) {
            if (!CollectionUtils.isEmpty(updateUserRequest.getRoles())) {
                user.setRoles(roleMapper.toRoles(updateUserRequest.getRoles()));
            }

            if (StringUtils.isNotBlank(updateUserRequest.getNewPassword())) {
                if (StringUtils.isBlank(updateUserRequest.getCurrentPassword()) ||
                    !passwordEncoder.matches(updateUserRequest.getCurrentPassword(), user.getPassword())) {
                    throw new ValidationException("Current password does not match to update the password.");
                }
                user.setPassword(passwordEncoder.encode(updateUserRequest.getNewPassword()));
            }
        }
    }
}
