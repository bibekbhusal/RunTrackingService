package com.bhusalb.runtrackingservice.views;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class CreateRunRequest {

    @NotBlank
    private String ownerId;

    @NotNull
    private LocalDateTime startDate;

    @NotNull
    private Integer duration;

    @NotNull
    private Double distance;

    @NotNull
    private Coordinates coordinates;
}
