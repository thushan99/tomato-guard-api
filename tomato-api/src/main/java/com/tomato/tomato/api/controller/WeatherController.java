package com.tomato.tomato.api.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    @Value("${openweathermap.api.key}")
    private String apiKey;

    @GetMapping
    public Map<String, Object> getWeatherFromCoordinates(@RequestParam Double latitude, @RequestParam Double longitude) {
        // Round coordinates to 4 decimal places for API compatibility
        double roundedLatitude = Math.round(latitude * 10000.0) / 10000.0;
        double roundedLongitude = Math.round(longitude * 10000.0) / 10000.0;

        String apiUrl = String.format(
                "https://api.openweathermap.org/data/2.5/weather?lat=%.4f&lon=%.4f&appid=%s&units=metric",
                roundedLatitude, roundedLongitude, apiKey
        );

        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> weatherData = restTemplate.getForObject(apiUrl, Map.class);

        // Safely extract wind speed
        Double windSpeed = null;
        Map<String, Object> windData = (Map<String, Object>) weatherData.get("wind");
        if (windData != null && windData.get("speed") instanceof Number) {
            windSpeed = ((Number) windData.get("speed")).doubleValue();
        }

        // Safely extract rainfall
        Double rainfall = 0.0;
        Map<String, Object> rainData = (Map<String, Object>) weatherData.get("rain");
        if (rainData != null && rainData.get("1h") instanceof Number) {
            rainfall = ((Number) rainData.get("1h")).doubleValue();
        }

        return Map.of(
                "wind_speed", windSpeed,
                "rainfall", rainfall
        );
    }
}
