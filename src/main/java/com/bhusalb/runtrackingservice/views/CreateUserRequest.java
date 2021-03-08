package com.bhusalb.runtrackingservice.views;

import com.bhusalb.runtrackingservice.Constants;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Set;

@Data
public class CreateUserRequest {

    @NotBlank
    @Email
    @Size (max = Constants.MAX_EMAIL_SIZE)
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String fullName;

    private Set<String> roles;
}
