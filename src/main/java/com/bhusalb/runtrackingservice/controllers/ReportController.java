package com.bhusalb.runtrackingservice.controllers;


import com.bhusalb.runtrackingservice.mappers.ObjectIdMapper;
import com.bhusalb.runtrackingservice.services.RunService;
import com.bhusalb.runtrackingservice.views.RunReport;
import com.bhusalb.runtrackingservice.views.RunView;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Tag (name = "Report")
@RestController
@RequestMapping ("/v1/reports/")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ReportController {

    private final RunService runService;
    private final ObjectIdMapper objectIdMapper;

    @PreAuthorize ("hasRole('ADMIN') || (@objectIdMapperImpl.stringToObjectId(#ownerId) == principal.id)")
    @GetMapping ("{ownerId}/")
    public ResponseEntity<RunReport> get (@PathVariable @NotBlank String ownerId,
                                          @RequestParam @NotNull LocalDate startDate,
                                          @RequestParam (required = false) LocalDate endDate) {
        endDate = Optional.ofNullable(endDate).orElse(LocalDate.now());
        final List<RunView> runs = runService.findRuns(objectIdMapper.stringToObjectId(ownerId), startDate, endDate);

        log.info("Total runs {} found between period {} and {} for user {}", runs.size(),
            startDate.format(DateTimeFormatter.ISO_DATE), endDate.format(DateTimeFormatter.ISO_DATE), ownerId);

        if (runs.isEmpty()) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok(generateReport(runs));
    }

    private RunReport generateReport (final List<RunView> runs) {
        if (runs.isEmpty()) return new RunReport();

        LocalDateTime earliest = LocalDateTime.MAX;
        LocalDateTime latest = LocalDateTime.MIN;

        double totalDistance = 0.0;
        int totalDuration = 0;
        double totalSpeed = 0.0;

        for (final RunView run : runs) {

            log.info("Run view: {}", run);

            if (earliest.isAfter(run.getStartDate())) {
                earliest = run.getStartDate();
            }

            if (latest.isBefore(run.getStartDate())) {
                latest = run.getStartDate();
            }

            totalDistance += run.getDistance();
            totalDuration += run.getDuration();
            totalSpeed += (run.getDistance() / run.getDuration());
        }

        log.info("Total distance {} total duration {} and total speed {}",
            totalDistance, totalDuration, totalSpeed);

        final Double averageDuration = totalDuration / (1.0 * runs.size());
        final Double averageDistance = totalDistance / runs.size();
        final Double averagePace = totalSpeed / runs.size();

        final RunReport report = new RunReport();
        report.setTotalRunsInPeriod(runs.size());
        report.setEarliestRunDate(earliest);
        report.setLatestRunDate(latest);
        report.setAverageDuration(averageDuration);
        report.setAverageDistance(averageDistance);
        report.setAveragePace(averagePace);

        return report;
    }
}
