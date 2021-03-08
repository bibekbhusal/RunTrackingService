package com.bhusalb.runtrackingservice.views;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Coordinates {

    @Max (value = 90)
    @Min (value = -90)
    private Double latitude;

    @Max (value = 180)
    @Min (value = -180)
    private Double longitude;

    public static Coordinates fromGeoJSONPoint (@NonNull final GeoJsonPoint point) {
        // GeoJSON point uses longitude, latitude convention.
        final double longitude = point.getX();
        final double latitude = point.getY();
        return new Coordinates(latitude, longitude);
    }

    public static GeoJsonPoint toGeoJSONPoint (@NonNull final Coordinates coordinates) {
        return new GeoJsonPoint(coordinates.longitude, coordinates.latitude);
    }
}
