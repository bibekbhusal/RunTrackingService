package com.bhusalb.runtrackingservice.views;

import com.bhusalb.runtrackingservice.Constants;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.LocalDate;

@Data
public class SearchRunQuery implements SearchQuery {

    private String ownerId;

    private LocalDate dateStart;
    private LocalDate dateEnd;

    @Min(value = 1)
    private Integer minDuration;

    @Min(value = 1)
    private Integer maxDuration;

    private Double minDistance;
    private Double maxDistance;

    private Coordinates queryPoint;

    @Min(value = 1)
    @Max (value = Constants.MAX_RADIUS_TO_QUERY)
    private Integer withinDistance;
}
