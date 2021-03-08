package com.bhusalb.runtrackingservice.views;

import com.bhusalb.runtrackingservice.Constants;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
public class SearchRunQuery implements SearchQuery {

    @NotBlank
    @Email
    @Size (max = Constants.MAX_EMAIL_SIZE)
    private String ownerId;

    private LocalDateTime dateStart;
    private LocalDateTime dateEnd;

    private Integer minDuration;
    private Integer maxDuration;

    private Double minDistance;
    private Double maxDistance;

    private Coordinates queryPoint;

    @Max (value = Constants.MAX_RADIUS_TO_QUERY)
    private Integer withinDistance;
}
