package com.bhusalb.runtrackingservice.views;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SearchUserQuery implements SearchQuery {
    private String email;
    private String fullName;
    private LocalDate createdDateStart;
    private LocalDate createdDateEnd;
    private String createdBy;
}
