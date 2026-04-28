package com.syndicati.models.weather;

import java.time.LocalDateTime;

/**
 * Data model for weather forecast
 */
public class ForecastData {
    private LocalDateTime dateTime;
    private double temperature;
    private String description;
    private String iconCode;

    public ForecastData() {}

    public ForecastData(LocalDateTime dateTime, double temperature, String description, String iconCode) {
        this.dateTime = dateTime;
        this.temperature = temperature;
        this.description = description;
        this.iconCode = iconCode;
    }

    // Getters and Setters
    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIconCode() { return iconCode; }
    public void setIconCode(String iconCode) { this.iconCode = iconCode; }

    public String getIconUrl() {
        return "https://openweathermap.org/img/wn/" + iconCode + ".png";
    }
}
