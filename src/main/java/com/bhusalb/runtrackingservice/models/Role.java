package com.bhusalb.runtrackingservice.models;

import lombok.Value;
import org.springframework.security.core.GrantedAuthority;

@Value
public class Role implements GrantedAuthority {
    int rank;
    String name;
    String description;

    @Override
    public String getAuthority () {
        return name;
    }
}
