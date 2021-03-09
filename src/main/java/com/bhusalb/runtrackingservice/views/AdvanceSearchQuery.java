package com.bhusalb.runtrackingservice.views;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AdvanceSearchQuery implements SearchQuery {
    @NotBlank
    private String queryString;
}
