package com.bhusalb.runtrackingservice.views;

import com.bhusalb.runtrackingservice.models.Weather;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class RunView {
    @NotBlank
    private String id;

    @NotNull
    private UserView owner;

    @NotNull
    private LocalDateTime startDate;

    @NotNull
    private Integer duration;

    @NotNull
    private Double distance;

    @NotNull
    private Coordinates coordinates;

    private Weather weather;
}
