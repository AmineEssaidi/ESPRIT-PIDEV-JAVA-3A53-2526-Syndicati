package com.syndicati.services;

import com.syndicati.models.weather.ForecastData;
import com.syndicati.models.weather.WeatherData;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to fetch weather data from OpenWeatherMap API
 */
public class WeatherService {
    // Replace with your actual API key
    private static final String API_KEY = "088783230648224bb1cb8022d248a123"; 
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";
    private final OkHttpClient client;

    public WeatherService() {
        this.client = new OkHttpClient();
    }

    /**
     * Fetch current weather for a city
     * @param city City name (e.g., "Tunis,TN")
     * @return WeatherData object
     */
    public WeatherData getCurrentWeather(String city) throws IOException {
        String encodedCity = java.net.URLEncoder.encode(city, java.nio.charset.StandardCharsets.UTF_8);
        String url = BASE_URL + "weather?q=" + encodedCity + "&units=metric&appid=" + API_KEY;
        return fetchWeather(url);
    }

    /**
     * Get current weather by coordinates
     */
    public WeatherData getCurrentWeather(double lat, double lon) throws IOException {
        String url = BASE_URL + "weather?lat=" + lat + "&lon=" + lon + "&units=metric&appid=" + API_KEY;
        return fetchWeather(url);
    }

    private WeatherData fetchWeather(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No body";
                System.err.println("Weather API Error: " + response.code() + " - " + errorBody);
                throw new IOException("Weather API error: " + response.code() + " " + errorBody);
            }

            JSONObject json = new JSONObject(response.body().string());
            JSONObject main = json.getJSONObject("main");
            JSONObject wind = json.getJSONObject("wind");
            JSONArray weatherArray = json.getJSONArray("weather");
            JSONObject weather = weatherArray.getJSONObject(0);

            return new WeatherData(
                json.optString("name", "Unknown Location"),
                main.optDouble("temp", 0.0),
                main.optDouble("feels_like", 0.0),
                main.optInt("humidity", 0),
                wind.optDouble("speed", 0.0),
                weather.getString("description"),
                weather.getString("icon"),
                weather.optString("main", "Clear")
            );
        }
    }

    /**
     * Fetch 5-day forecast for a city
     * @param city City name
     * @return List of ForecastData
     */
    public List<ForecastData> getForecast(String city) throws IOException {
        String encodedCity = java.net.URLEncoder.encode(city, java.nio.charset.StandardCharsets.UTF_8);
        String url = BASE_URL + "forecast?q=" + encodedCity + "&units=metric&appid=" + API_KEY;
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No body";
                System.err.println("Weather API Forecast Error: " + response.code() + " - " + errorBody);
                throw new IOException("Weather API error: " + response.code() + " " + errorBody);
            }

            JSONObject json = new JSONObject(response.body().string());
            JSONArray list = json.getJSONArray("list");
            List<ForecastData> forecasts = new ArrayList<>();

            // The API returns data every 3 hours. We'll take one per day (around noon).
            for (int i = 0; i < list.length(); i++) {
                JSONObject item = list.getJSONObject(i);
                String dtTxt = item.getString("dt_txt");
                
                // Only take forecasts for 12:00:00 to represent the day
                if (dtTxt.contains("12:00:00")) {
                    JSONObject main = item.getJSONObject("main");
                    JSONArray weatherArray = item.getJSONArray("weather");
                    JSONObject weather = weatherArray.getJSONObject(0);
                    long dt = item.getLong("dt");

                    forecasts.add(new ForecastData(
                        LocalDateTime.ofInstant(Instant.ofEpochSecond(dt), ZoneId.systemDefault()),
                        main.optDouble("temp", 0.0),
                        weather.getString("description"),
                        weather.getString("icon")
                    ));
                }
            }
            return forecasts;
        }
    }
}
