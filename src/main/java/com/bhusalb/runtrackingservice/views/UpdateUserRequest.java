package com.bhusalb.runtrackingservice.views;

import lombok.Data;

import java.util.Set;

@Data
public class UpdateUserRequest {
    private String fullName;
    private String currentPassword;
    private String newPassword;
    private Set<String> roles;
}
