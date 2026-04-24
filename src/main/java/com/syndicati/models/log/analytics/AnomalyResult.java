package com.syndicati.models.log.analytics;

import java.time.LocalDateTime;

public class AnomalyResult {

    private final long eventId;
    private final String eventType;
    private final Integer userId;
    private final double anomalyScore;
    private final String anomalyLabel;
    private final String anomalyReason;
    private final LocalDateTime detectedAt;

    public AnomalyResult(long eventId, String eventType, Integer userId, double anomalyScore, String anomalyLabel, String anomalyReason, LocalDateTime detectedAt) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.userId = userId;
        this.anomalyScore = anomalyScore;
        this.anomalyLabel = anomalyLabel;
        this.anomalyReason = anomalyReason;
        this.detectedAt = detectedAt;
    }

    public long getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public Integer getUserId() {
        return userId;
    }

    public double getAnomalyScore() {
        return anomalyScore;
    }

    public String getAnomalyLabel() {
        return anomalyLabel;
    }

    public String getAnomalyReason() {
        return anomalyReason;
    }

    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }
}
