package com.bhusalb.runtrackingservice.services;

import com.bhusalb.runtrackingservice.exceptions.ResourceNotFoundException;
import com.bhusalb.runtrackingservice.mappers.RunEditMapper;
import com.bhusalb.runtrackingservice.mappers.RunViewMapper;
import com.bhusalb.runtrackingservice.models.Run;
import com.bhusalb.runtrackingservice.models.Weather;
import com.bhusalb.runtrackingservice.repos.RunRepository;
import com.bhusalb.runtrackingservice.views.Coordinates;
import com.bhusalb.runtrackingservice.views.CreateRunRequest;
import com.bhusalb.runtrackingservice.views.Page;
import com.bhusalb.runtrackingservice.views.RunView;
import com.bhusalb.runtrackingservice.views.SearchRunQuery;
import com.bhusalb.runtrackingservice.views.UpdateRunRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RunService {

    private final UserService userService;
    private final RunRepository runRepository;
    private final RunViewMapper runViewMapper;
    private final RunEditMapper runEditMapper;
    private final WeatherService weatherService;

    @Transactional
    public RunView create (final CreateRunRequest createRunRequest) {
        final Run run = runEditMapper.create(createRunRequest);
        if (!userService.doesUserExist(run.getOwnerId())) {
            final String message = String.format("User does not exist with given id: %s.",
                createRunRequest.getOwnerId());
            throw new ResourceNotFoundException(message);
        }
        final Run savedRun = runRepository.save(run);
        weatherService.getWeather(run.getStartDate().toLocalDate(), createRunRequest.getCoordinates())
            .subscribe(response -> {
                log.info("Adding weather after run is created in db. Response: {}", response);
                savedRun.setWeather(createWeatherFromResponse(response));
                runRepository.save(savedRun);
            });
        return runViewMapper.toRunView(savedRun);
    }

    @Transactional
    public RunView update (final ObjectId objectId, final UpdateRunRequest updateRunRequest) {
        final Run run = runRepository.getById(objectId);

        final LocalDate currentDate = run.getStartDate().toLocalDate();
        final Coordinates currentCoordinates = Coordinates.fromGeoJSONPoint(run.getLocation());

        runEditMapper.update(updateRunRequest, run);
        final Run updated = runRepository.save(run);

        if (updateWeather(currentDate, currentCoordinates, updateRunRequest)) {
            final LocalDate searchDate = Optional.ofNullable(updateRunRequest.getStartDate())
                .map(LocalDateTime::toLocalDate).orElse(currentDate);
            final Coordinates searchCoordinates = Optional.ofNullable(updateRunRequest.getCoordinates())
                .orElse(currentCoordinates);
            weatherService.getWeather(searchDate, searchCoordinates)
                .subscribe(response -> {
                    log.info("Updating weather of a run. Response: {}", response);
                    updated.setWeather(createWeatherFromResponse(response));
                    runRepository.save(updated);
                });
        }
        return runViewMapper.toRunView(updated);
    }

    @Transactional
    public RunView delete (final ObjectId objectId) {
        final Run run = runRepository.getById(objectId);
        runRepository.delete(run);
        return runViewMapper.toRunView(run);
    }

    public RunView getRunById (final ObjectId objectId) {
        return runViewMapper.toRunView(runRepository.getById(objectId));
    }

    public List<RunView> findRuns (final ObjectId ownerId, final LocalDate startDate, final LocalDate endDate) {

        if (!userService.doesUserExist(ownerId)) {
            final String message = String.format("User with id %s does not exist.", ownerId.toString());
            log.warn("Tried to find runs of a user {} which does not exist.", ownerId);
            throw new ResourceNotFoundException(message);
        }

        final List<Run> runs = runRepository.findByOwnerIdAndStartDateBetween(ownerId,
            startDate.atStartOfDay(),
            endDate.plusDays(1).atStartOfDay());

        return runViewMapper.toRunViews(runs);
    }

    public List<RunView> searchRuns (final Page page, final SearchRunQuery query) {
        final List<Run> runs = runRepository.searchRuns(page, query);
        return runViewMapper.toRunViews(runs);
    }

    private Weather createWeatherFromResponse (final WeatherService.WeatherQueryResponse response) {
        final Weather weather = new Weather();
        weather.setTemperature(response.getTemperature().orElse(null));
        weather.setHumidity(response.getHumidity().orElse(null));
        weather.setPrecipitation(response.getPrecipitation().orElse(null));
        return weather;
    }

    private boolean updateWeather (final LocalDate currentDate,
                                   final Coordinates currentCoordinates,
                                   final UpdateRunRequest request) {

        log.info("Current date: {} and updated date: {}", currentDate,
            Optional.ofNullable(request.getStartDate()).map(LocalDateTime::toLocalDate).orElse(null));
        log.info("Current coordinates: {} updated coordinates: {}.", currentCoordinates, request.getCoordinates());

        final boolean dateChanged = Optional.ofNullable(request.getStartDate())
            .map(LocalDateTime::toLocalDate)
            .map(date -> !currentDate.equals(date))
            .orElse(false);

        final boolean coordinatesChanged = Optional.ofNullable(request.getCoordinates())
            .map(coordinates -> !currentCoordinates.equals(coordinates))
            .orElse(false);

        return dateChanged || coordinatesChanged;
    }
}
