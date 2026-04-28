package com.syndicati.views.frontend.weather;

import com.syndicati.MainApplication;
import com.syndicati.models.weather.ForecastData;
import com.syndicati.models.weather.WeatherData;
import com.syndicati.services.WeatherService;
import com.syndicati.utils.theme.ThemeManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Advanced Weather Page View - Premium UI for weather tracking
 */
public class WeatherPageView {

    private final StackPane root;
    private final WeatherService weatherService;
    private final VBox contentBox;
    private final String defaultCity = "Tunis,TN";

    public WeatherPageView() {
        this.root = new StackPane();
        this.weatherService = new WeatherService();
        this.contentBox = new VBox(30);
        
        setupLayout();
        loadWeatherData();
    }

    private void setupLayout() {
        root.setPadding(new Insets(20, 40, 40, 40));
        
        contentBox.setAlignment(Pos.TOP_CENTER);
        contentBox.setMaxWidth(1100);

        ScrollPane scrollPane = new ScrollPane(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        root.getChildren().add(scrollPane);
    }

    private void loadWeatherData() {
        contentBox.getChildren().clear();
        
        ProgressIndicator loader = new ProgressIndicator();
        loader.setMaxSize(50, 50);
        contentBox.getChildren().add(loader);

        Thread.ofVirtual().start(() -> {
            try {
                WeatherData current = weatherService.getCurrentWeather(defaultCity);
                List<ForecastData> forecast = weatherService.getForecast(defaultCity);

                Platform.runLater(() -> {
                    contentBox.getChildren().remove(loader);
                    buildUI(current, forecast);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    contentBox.getChildren().remove(loader);
                    Label errorLabel = new Label("Unable to load weather data. Please check your API key.");
                    errorLabel.setTextFill(Color.RED);
                    contentBox.getChildren().add(errorLabel);
                });
            }
        });
    }

    private void buildUI(WeatherData current, List<ForecastData> forecast) {
        // Hero Section
        VBox heroCard = createHeroCard(current);
        
        // Forecast Section
        VBox forecastSection = createForecastSection(forecast);
        
        // Details & Recommendations
        HBox bottomSection = new HBox(30);
        bottomSection.setAlignment(Pos.CENTER);
        bottomSection.getChildren().addAll(
            createDetailsCard(current),
            createRecommendationsCard(current)
        );

        contentBox.getChildren().addAll(heroCard, forecastSection, bottomSection);
    }

    private VBox createHeroCard(WeatherData data) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(40));
        card.setAlignment(Pos.CENTER);
        card.setStyle(getGlassyStyle("#1e293b", 0.6));
        card.setEffect(new javafx.scene.effect.DropShadow(20, Color.rgb(0, 0, 0, 0.3)));

        Label cityLabel = new Label(data.getCity().toUpperCase());
        cityLabel.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 18));
        cityLabel.setTextFill(Color.WHITE);
        cityLabel.setOpacity(0.8);

        HBox mainWeather = new HBox(30);
        mainWeather.setAlignment(Pos.CENTER);
        
        ImageView icon = new ImageView(new Image(data.getIconUrl(), true));
        icon.setFitWidth(120);
        icon.setFitHeight(120);

        VBox tempBox = new VBox(-10);
        tempBox.setAlignment(Pos.CENTER_LEFT);
        Label tempLabel = new Label(Math.round(data.getTemperature()) + "°");
        tempLabel.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 80));
        tempLabel.setTextFill(Color.WHITE);
        
        Label descLabel = new Label(data.getDescription().substring(0, 1).toUpperCase() + data.getDescription().substring(1));
        descLabel.setFont(Font.font(MainApplication.getInstance().getLightFontFamily(), 22));
        descLabel.setTextFill(Color.WHITE);
        tempBox.getChildren().addAll(tempLabel, descLabel);

        mainWeather.getChildren().addAll(icon, tempBox);

        card.getChildren().addAll(cityLabel, mainWeather);
        return card;
    }

    private VBox createForecastSection(List<ForecastData> forecasts) {
        VBox section = new VBox(20);
        section.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("5-Day Forecast");
        title.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 22));
        title.setTextFill(ThemeManager.getInstance().isDarkMode() ? Color.WHITE : Color.rgb(30, 41, 59));

        HBox forecastBox = new HBox(20);
        forecastBox.setAlignment(Pos.CENTER);

        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE");

        for (ForecastData f : forecasts) {
            VBox fCard = new VBox(10);
            fCard.setPadding(new Insets(20));
            fCard.setAlignment(Pos.CENTER);
            fCard.setPrefWidth(180);
            fCard.setStyle(getGlassyStyle(ThemeManager.getInstance().getAccentHex(), 0.15));
            
            Label day = new Label(f.getDateTime().format(dayFormatter).toUpperCase());
            day.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 14));
            day.setTextFill(ThemeManager.getInstance().isDarkMode() ? Color.WHITE : Color.rgb(71, 85, 105));

            ImageView fIcon = new ImageView(new Image(f.getIconUrl(), true));
            fIcon.setFitWidth(60);
            fIcon.setFitHeight(60);

            Label fTemp = new Label(Math.round(f.getTemperature()) + "°");
            fTemp.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 20));
            fTemp.setTextFill(ThemeManager.getInstance().isDarkMode() ? Color.WHITE : Color.rgb(30, 41, 59));

            fCard.getChildren().addAll(day, fIcon, fTemp);
            forecastBox.getChildren().add(fCard);
        }

        section.getChildren().addAll(title, forecastBox);
        return section;
    }

    private VBox createDetailsCard(WeatherData data) {
        VBox card = new VBox(20);
        card.setPadding(new Insets(25));
        card.setPrefWidth(535);
        card.setStyle(getGlassyStyle("#1e293b", 0.05));
        
        Label title = new Label("Conditions Summary");
        title.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 18));
        
        GridPane grid = new GridPane();
        grid.setHgap(40);
        grid.setVgap(20);
        
        grid.add(detailItem("Feels Like", Math.round(data.getFeelsLike()) + "°"), 0, 0);
        grid.add(detailItem("Humidity", data.getHumidity() + "%"), 1, 0);
        grid.add(detailItem("Wind Speed", data.getWindSpeed() + " km/h"), 0, 1);
        grid.add(detailItem("General", data.getCondition()), 1, 1);

        card.getChildren().addAll(title, grid);
        return card;
    }

    private VBox createRecommendationsCard(WeatherData data) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(25));
        card.setPrefWidth(535);
        card.setStyle(getGlassyStyle(ThemeManager.getInstance().getAccentHex(), 0.1));

        Label title = new Label("Smart Recommendations");
        title.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 18));

        String recommendation;
        String icon;

        if (data.getCondition().contains("Rain")) {
            recommendation = "Indoor activities recommended. Perfect time to check the building's drainage system.";
            icon = "☂️";
        } else if (data.getTemperature() > 25) {
            recommendation = "Warm day ahead! Great for outdoor terrace gatherings. Ensure garden irrigation is active.";
            icon = "☀️";
        } else if (data.getTemperature() < 10) {
            recommendation = "Chilly weather. Check the residence heating efficiency and ensure all common area windows are closed.";
            icon = "❄️";
        } else {
            recommendation = "Pleasant weather for a community meeting in the park or garden areas. Enjoy the fresh air!";
            icon = "🌿";
        }

        Label recLabel = new Label(icon + " " + recommendation);
        recLabel.setWrapText(true);
        recLabel.setFont(Font.font(MainApplication.getInstance().getLightFontFamily(), 14));
        recLabel.setLineSpacing(5);

        card.getChildren().addAll(title, recLabel);
        return card;
    }

    private VBox detailItem(String label, String value) {
        VBox box = new VBox(5);
        Label l = new Label(label);
        l.setFont(Font.font(MainApplication.getInstance().getLightFontFamily(), 12));
        l.setOpacity(0.6);
        
        Label v = new Label(value);
        v.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 16));
        
        box.getChildren().addAll(l, v);
        return box;
    }

    private String getGlassyStyle(String color, double opacity) {
        ThemeManager tm = ThemeManager.getInstance();
        boolean dark = tm.isDarkMode();
        String bg = dark ? "rgba(255,255,255,0.05)" : "rgba(255,255,255,0.7)";
        String border = dark ? "rgba(255,255,255,0.1)" : "rgba(0,0,0,0.05)";
        
        if (color.startsWith("#")) {
            // Convert simple hex to rgba if needed, but here we just use it for the accent blocks
            return "-fx-background-color: " + color + "22;" +
                   "-fx-background-radius: 24px;" +
                   "-fx-border-color: " + color + "44;" +
                   "-fx-border-width: 1px;" +
                   "-fx-border-radius: 24px;" +
                   "-fx-backdrop-filter: blur(20px);";
        }

        return "-fx-background-color: " + bg + ";" +
               "-fx-background-radius: 24px;" +
               "-fx-border-color: " + border + ";" +
               "-fx-border-width: 1.5px;" +
               "-fx-border-radius: 24px;";
    }

    public StackPane getRoot() {
        return root;
    }
}
