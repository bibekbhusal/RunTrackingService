package com.bhusalb.runtrackingservice.views;

import lombok.Data;

import javax.validation.constraints.Email;
import java.time.LocalDateTime;

@Data
public class SearchUserQuery implements SearchQuery {
//    private String id;
    private String email;
    private String fullName;
    private LocalDateTime createdDateStart;
    private LocalDateTime createdDateEnd;
    @Email
    private String createdBy;
}
