package com.bhusalb.runtrackingservice.views;

import com.bhusalb.runtrackingservice.Constants;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.Set;

@Data
public class UserView {
    @NotBlank
    private String id;

    @Email
    @Size (max = Constants.MAX_EMAIL_SIZE)
    @NotBlank
    private String email;

    @NotBlank
    private String fullName;

    @NotEmpty
    private Set<String> roles;
}
