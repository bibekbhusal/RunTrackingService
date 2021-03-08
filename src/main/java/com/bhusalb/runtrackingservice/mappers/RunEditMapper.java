package com.bhusalb.runtrackingservice.mappers;

import com.bhusalb.runtrackingservice.models.Run;
import com.bhusalb.runtrackingservice.views.Coordinates;
import com.bhusalb.runtrackingservice.views.CreateRunRequest;
import com.bhusalb.runtrackingservice.views.UpdateRunRequest;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import javax.validation.ValidationException;
import java.time.LocalDateTime;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper (componentModel = "spring", uses = {ObjectIdMapper.class})
@Slf4j
public abstract class RunEditMapper {

    private static final int YEAR_THRESHOLD = 1;

    public abstract Run create (final CreateRunRequest createRunRequest);

    @BeanMapping (nullValueCheckStrategy = ALWAYS, nullValuePropertyMappingStrategy = IGNORE)
    public abstract void update (final UpdateRunRequest updateRunRequest, @MappingTarget final Run run);

    @AfterMapping
    protected void afterCreate (final CreateRunRequest createRunRequest, @MappingTarget final Run run) {
        run.setLocation(Coordinates.toGeoJSONPoint(createRunRequest.getCoordinates()));
        if (createRunRequest.getStartDate().isBefore(LocalDateTime.now().minusYears(YEAR_THRESHOLD))) {
            log.warn("Tried to create run from more than a year ago. Date: {}", createRunRequest.getStartDate());
            throw new ValidationException("Cannot create run from more than a year ago.");
        }

        // Do not allow creating run from future dates.
        // Adding 5 minutes to cover clock skew.
        if (createRunRequest.getStartDate().isAfter(LocalDateTime.now().plusMinutes(5))) {
            log.warn("Tried to create run from future date. Date: {}", createRunRequest.getStartDate());
            throw new ValidationException("Start date cannot be in future.");
        }
    }

    @AfterMapping
    protected void afterUpdate (final UpdateRunRequest updateRunRequest, @MappingTarget final Run run) {
        if (updateRunRequest.getCoordinates() != null) {
            run.setLocation(Coordinates.toGeoJSONPoint(updateRunRequest.getCoordinates()));
        }

        if (updateRunRequest.getStartDate() != null &&
            updateRunRequest.getStartDate().isBefore(LocalDateTime.now().minusYears(YEAR_THRESHOLD))) {
            log.warn("Tried to update run to start more than a year ago. Date: {}", updateRunRequest.getStartDate());
            throw new ValidationException("Cannot update run to start more than a year ago.");
        }

        // Do not allow updating run start date to future dates.
        // Adding 5 minutes to cover clock skew.
        if (updateRunRequest.getStartDate() != null &&
            updateRunRequest.getStartDate().isAfter(LocalDateTime.now().plusMinutes(5))) {
            log.warn("Tried to update run to future date. Date: {}", updateRunRequest.getStartDate());
            throw new ValidationException("Start date cannot be in future.");
        }
    }
}
