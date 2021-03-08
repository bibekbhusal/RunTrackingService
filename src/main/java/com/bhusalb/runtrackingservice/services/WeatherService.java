package com.bhusalb.runtrackingservice.services;

import com.bhusalb.runtrackingservice.views.Coordinates;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jsonwebtoken.lang.Maps;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class WeatherService {

    private static final String API_KEY = "1PYNQ6AWUDJE9AFERDCHJHSXK";

    private static final String BASE_URI =
        "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/weatherdata/";

    private final WebClient webClient;

    public Mono<WeatherQueryResponse> getWeather (@NonNull final LocalDate date,
                                                  @NonNull final Coordinates coordinates) {
        log.info("Fetching weather of coordinates: {} from date: {}.", coordinates,
            date.format(DateTimeFormatter.ISO_DATE));

        // Historical weather
        if (date.isBefore(LocalDate.now())) {
            return queryHistoricalWeather(date, coordinates);
        }
        return queryCurrentWeather(coordinates);
    }

    private Mono<WeatherQueryResponse> queryHistoricalWeather (final LocalDate date, final Coordinates coordinates) {
        final Map<String, String> queryMap =
            Maps.of("aggregateHours", "24")
                .and("contentType", "json")
                .and("unitGroup", "us")
                .and("locationMode", "single")
                .and("key", API_KEY)
                .and("locations", formatCoordinates(coordinates))
                .and("startDateTime", date.format(DateTimeFormatter.ISO_DATE))
                .and("endDateTime", date.format(DateTimeFormatter.ISO_DATE))
                .build();

        log.info("Querying historical weather.");

        return webClient.get()
            .uri(BASE_URI,
                uriBuilder -> {
                    uriBuilder.path("history");
                    queryMap.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                })
            .exchangeToMono(clientResponse -> {
                log.info("Response code: {}", clientResponse.statusCode());
                return clientResponse.bodyToMono(WeatherQueryResponse.class);
            })
            .retryWhen(Retry.backoff(2, Duration.ofMillis(100)));
    }

    private Mono<WeatherQueryResponse> queryCurrentWeather (final Coordinates coordinates) {
        final Map<String, String> queryMap = Maps.of("aggregateHours", "24")
            .and("contentType", "json")
            .and("unitGroup", "us")
            .and("locationMode", "single")
            .and("key", API_KEY)
            .and("locations", formatCoordinates(coordinates))
            .and("forecastDays", "1")
            .build();

        log.info("Querying current weather.");

        return webClient.get()
            .uri(BASE_URI,
                uriBuilder -> {
                    uriBuilder.path("forecast");
                    queryMap.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                })
            .exchangeToMono(clientResponse -> {
                log.info("Response code: {}", clientResponse.statusCode());
                return clientResponse.bodyToMono(WeatherQueryResponse.class);
            })
            .retryWhen(Retry.backoff(2, Duration.ofMillis(100)));
    }

    private String formatCoordinates (final Coordinates coordinates) {
        return String.format("%s,%s", coordinates.getLatitude(), coordinates.getLongitude());
    }

    @ToString
    @JsonIgnoreProperties (ignoreUnknown = true)
    public static class WeatherQueryResponse {
        @Setter
        private Integer remainingCost;
        @Setter
        private Integer queryCost;
        @Setter
        private Location location;

        @JsonIgnore
        public Optional<Double> getTemperature () {
            return getValue().map(Value::getTemperature);
        }

        @JsonIgnore
        public Optional<Double> getPrecipitation () {
            return getValue().map(Value::getPrecipitation);
        }

        @JsonIgnore
        public Optional<Double> getHumidity () {
            return getValue().map(Value::getHumidity);
        }

        @JsonIgnore
        private Optional<Value> getValue () {
            return Optional.ofNullable(location)
                .filter(location -> location.getValues() != null && location.getValues().length > 0)
                .map(location -> location.getValues()[0]);
        }
    }
}

@Data
@JsonIgnoreProperties (ignoreUnknown = true)
class Location {
    private Value[] values;
}

@Data
@JsonIgnoreProperties (ignoreUnknown = true)
class Value {
    @JsonProperty ("temp")
    private Double temperature;

    @JsonProperty ("maxt")
    private Double maxTemperature;

    @JsonProperty ("mint")
    private Double minTemperature;

    @JsonProperty ("precip")
    private Double precipitation;

    private Double humidity;
}
