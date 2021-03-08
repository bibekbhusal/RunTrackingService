package com.bhusalb.runtrackingservice.mappers;

import com.bhusalb.runtrackingservice.models.Role;
import com.bhusalb.runtrackingservice.models.Roles;
import org.mapstruct.Mapper;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper (componentModel = "spring")
public abstract class RoleMapper {

    public String toRoleString (final Role role) {
        return role.getName();
    }

    public Role toRoleFromString (final String role) {
        return Roles.getRoleFromName(role);
    }

    public Set<String> toStringRoles (final Set<Role> roles) {
        return Optional.ofNullable(roles).orElse(Collections.emptySet())
            .stream().map(this::toRoleString)
            .collect(Collectors.toSet());
    }

    public Set<Role> toRoles (final Set<String> roles) {
        return Optional.ofNullable(roles).orElse(Collections.emptySet())
            .stream().map(this::toRoleFromString)
            .collect(Collectors.toSet());
    }
}
