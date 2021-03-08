package com.bhusalb.runtrackingservice.views;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateRunRequest {
    private LocalDateTime startDate;
    private Integer duration;
    private Double distance;
    private Coordinates coordinates;
}
