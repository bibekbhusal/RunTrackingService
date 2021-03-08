package com.bhusalb.runtrackingservice.views;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class RunReport {
    @NotNull
    private LocalDateTime earliestRunDate;

    @NotNull
    private LocalDateTime latestRunDate;

    @NotNull
    private Double averageDuration;

    @NotNull
    private Double averageDistance;

    @NotNull
    private Double averagePace;
}
