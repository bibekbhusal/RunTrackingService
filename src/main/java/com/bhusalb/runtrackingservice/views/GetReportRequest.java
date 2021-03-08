package com.bhusalb.runtrackingservice.views;

import lombok.Data;

import java.time.LocalDate;

@Data
public class GetReportRequest {
    private LocalDate startDate;
    private LocalDate endDate;
}
