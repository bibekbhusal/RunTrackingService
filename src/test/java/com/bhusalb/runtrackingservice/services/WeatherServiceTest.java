package com.bhusalb.runtrackingservice.services;

import com.bhusalb.runtrackingservice.views.Coordinates;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
class WeatherServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec mockUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> mockHeaderSpec;

    @InjectMocks
    private WeatherService weatherService;

    @Test
    void get_current_weather () {
        final LocalDate today = LocalDate.now();
        final Coordinates coordinates = new Coordinates(47.12345, -120.12345);

        final WeatherService.WeatherQueryResponse expected = new WeatherService.WeatherQueryResponse();

        when(webClient.get()).thenReturn(mockUriSpec);
        when(mockUriSpec.uri(anyString(), any(Function.class))).thenReturn(mockHeaderSpec);
        when(mockHeaderSpec.exchangeToMono(any(Function.class))).thenReturn(Mono.just(expected));

        final Mono<WeatherService.WeatherQueryResponse> actual = weatherService.getWeather(today, coordinates);

        assertThat(actual.block()).isSameAs(expected);
    }

    @Test
    void get_past_weather () {
        final LocalDate today = LocalDate.now().minusDays(10);
        final Coordinates coordinates = new Coordinates(47.12345, -120.12345);

        final WeatherService.WeatherQueryResponse expected = new WeatherService.WeatherQueryResponse();

        when(webClient.get()).thenReturn(mockUriSpec);
        when(mockUriSpec.uri(anyString(), any(Function.class))).thenReturn(mockHeaderSpec);
        when(mockHeaderSpec.exchangeToMono(any(Function.class))).thenReturn(Mono.just(expected));

        final Mono<WeatherService.WeatherQueryResponse> actual = weatherService.getWeather(today, coordinates);

        assertThat(actual.block()).isSameAs(expected);
    }
}