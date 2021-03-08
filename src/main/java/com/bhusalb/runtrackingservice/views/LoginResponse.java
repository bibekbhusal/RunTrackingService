package com.bhusalb.runtrackingservice.views;

import lombok.Data;
import lombok.NonNull;

@Data
public class LoginResponse {

    @NonNull
    private String authToken;

    @NonNull
    private UserView userView;
}
