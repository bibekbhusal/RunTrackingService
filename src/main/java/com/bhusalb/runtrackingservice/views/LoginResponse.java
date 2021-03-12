package com.bhusalb.runtrackingservice.views;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    @NonNull
    private String authToken;

    @NonNull
    private UserView userView;
}
