package com.bhusalb.runtrackingservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import io.jsonwebtoken.lang.Maps;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class VisualCrossingTest {

    private static final String BASE_URI = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/weatherdata/";

    public static void main (String[] args) throws Exception {
//        retrieveWeatherForecastAsJson();
        System.out.println();
        retrieveHistoricalWeather();
    }

    private static ObjectMapper objectMapper () {
        ObjectMapper objectMapper = new ObjectMapper();

        final JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDateTime.class,
            new LocalDateTimeDeserializer(DateTimeFormatter.ISO_DATE_TIME));
        objectMapper.registerModule(javaTimeModule);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return objectMapper;
    }

    private static WebClient getClient () throws SSLException {
        final SslContext sslContext = SslContextBuilder.forClient()
            .trustManager(InsecureTrustManagerFactory.INSTANCE)
            .build();
        final HttpClient httpClient = HttpClient.create().secure(t -> t.sslContext(sslContext));

        final Jackson2JsonDecoder decoder = new Jackson2JsonDecoder(objectMapper(), MediaType.APPLICATION_JSON);
        final Jackson2JsonEncoder encoder = new Jackson2JsonEncoder(objectMapper(), MediaType.APPLICATION_JSON);

        final ExchangeStrategies exchangeStrategies =
            ExchangeStrategies.builder().codecs(
                clientCodecConfigurer -> {
                    clientCodecConfigurer.defaultCodecs().jackson2JsonDecoder(decoder);
                    clientCodecConfigurer.defaultCodecs().jackson2JsonEncoder(encoder);
                }
            ).build();

        return WebClient.builder()
            .exchangeStrategies(exchangeStrategies)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
//            .baseUrl(BASE_URI)
            .build();
    }

    private static void retrieveHistoricalWeather () throws SSLException {
        final LocalDate date = LocalDate.of(2020, 10, 13);
        final Coordinates coordinates = new Coordinates(47.845805, -122.1882534);

        final Map<String, String> queryMap =
            Maps.of("aggregateHours", "24")
                .and("contentType", "json")
                .and("unitGroup", "us")
                .and("locationMode", "single")
                .and("key", "1PYNQ6AWUDJE9AFERDCHJHSXK")
                .and("locations", String.format("%s,%s", coordinates.latitude, coordinates.longitude))
                .and("startDateTime", date.format(DateTimeFormatter.ISO_DATE))
                .and("endDateTime", date.format(DateTimeFormatter.ISO_DATE))
                .build();

        final Mono<InternalWeatherResponse> response = getClient().get()
            .uri(BASE_URI,
                uriBuilder -> {
                    uriBuilder.path("history");
                    queryMap.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                })
            .retrieve()
            .bodyToMono(InternalWeatherResponse.class);

        System.out.println("History: " + response.block());
    }

    private static void retrieveWeatherForecastAsJson () throws Exception {

        final Map<String, String> queryMap = Maps.of("aggregateHours", "24")
            .and("contentType", "json")
            .and("unitGroup", "us")
            .and("locationMode", "single")
            .and("key", "1PYNQ6AWUDJE9AFERDCHJHSXK")
            .and("locations", "47.845805,-122.1882534")
            .and("forecastDays", "1").build();

        final Mono<InternalWeatherResponse> response = getClient().get()
            .uri(BASE_URI,
                uriBuilder -> {
                    uriBuilder.path("forecast");
                    queryMap.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                })
            .retrieve()
            .bodyToMono(InternalWeatherResponse.class);

        System.out.println("Forecast: " + response.block());
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class Coordinates {
        private Double latitude;
        private Double longitude;
    }

    @Data
    @JsonIgnoreProperties (ignoreUnknown = true)
    static class InternalWeatherResponse {
        private Integer remainingCost;
        private Integer queryCost;
        private Location location;
    }

    @Data
    @JsonIgnoreProperties (ignoreUnknown = true)
    static class Location {
        private Value[] values;
    }

    @Data
    @JsonIgnoreProperties (ignoreUnknown = true)
    static class Value {
        @JsonProperty ("temp")
        private Double temperature;

        @JsonProperty ("maxt")
        private Double maxTemperature;

        @JsonProperty ("mint")
        private Double minTemperature;

        private LocalDateTime datetimeStr;

        @JsonProperty ("precip")
        private Double precipitation;

        private Double humidity;
    }
}


