package com.bhusalb.runtrackingservice.models;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Document (collection = "Runs")
@CompoundIndex (name = "owner_date", def = "{'ownerId': 1, 'date': 1}")
@CompoundIndex (name = "owner_distance", def = "{'ownerId': 1, 'distance': 1}")
@CompoundIndex (name = "owner_duration", def = "{'ownerId': 1, 'duration': 1}")
@CompoundIndex (name = "owner_location", def = "{'ownerId': 1, 'location': 1}")
public class Run implements Serializable {

    @Id
    private ObjectId id;

    @NotNull
    @Indexed
    private ObjectId ownerId;

    @NotNull
    private LocalDateTime startDate;

    @NotNull
    private Integer duration;

    @NotNull
    private GeoJsonPoint location;

    @NotNull
    private Double distance;

    private Weather weather;

    @CreatedDate
    private LocalDateTime created;

    @LastModifiedDate
    private LocalDateTime updated;

    @CreatedBy
    private ObjectId createdBy;

    @LastModifiedBy
    private ObjectId lastModifiedBy;
}
