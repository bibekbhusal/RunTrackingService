package com.bhusalb.runtrackingservice.models;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class Roles {
    public static final Role USER = new Role(0, "USER", "End user role.");

    public static final Role USER_MANAGER = new Role(10, "USER_MANAGER",
        "System user with access to manage all users.");

    public static final Role ADMIN = new Role(100, "ADMIN",
        "System user with all system access.");

    private static final Map<String, Role> ROLES = new HashMap<>();

    static {
        ROLES.put(USER.getName(), USER);
        ROLES.put(USER_MANAGER.getName(), USER_MANAGER);
        ROLES.put(ADMIN.getName(), ADMIN);
    }

    public static Role getRoleFromName (final String name) {
        return Optional.ofNullable(name)
            .filter(ROLES::containsKey)
            .map(ROLES::get)
            .orElseThrow(
                () -> new IllegalArgumentException("Role does not exist with given name: " + name));
    }

    /**
     * Returns equivalent {@link Role} of given role names. Ignores undefined role names.
     *
     * @param roles Set of role names
     * @return Set of system roles corresponding to given role names.
     */
    public static Set<Role> getRoles (final Set<String> roles) {
        return Optional.ofNullable(roles).orElse(Collections.emptySet())
            .stream()
            .map(Roles::getRoleFromName)
            .collect(Collectors.toSet());
    }

    public static int getHighestRank (final Set<Role> roles) {
        return Optional.ofNullable(roles).orElse(Collections.emptySet())
            .stream().map(Role::getRank).max(Comparator.naturalOrder())
            .orElse(0);
    }
}
