package com.syndicati.services;

import com.syndicati.models.entities.Evenement;
import com.syndicati.models.entities.Participation;
import java.util.*;
import java.util.stream.Collectors;

public class RecommendationService {

    public RecommendationService() {
        // No more API needed
    }

    /**
     * Recommends events based on local logic (Category matching + Popularity)
     */
    public List<Evenement> getRecommendations(List<Evenement> allEvents, List<Participation> userHistory) {
        if (allEvents == null || allEvents.isEmpty()) return new ArrayList<>();

        // 1. Identify user's favorite categories from history
        Map<String, Long> categoryCounts = userHistory.stream()
                .map(p -> p.getEvenement().getTypeEvent())
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(type -> type, Collectors.counting()));

        // 2. Score each event
        return allEvents.stream()
                // Filter out events user is already participating in
                .filter(e -> userHistory.stream().noneMatch(p -> p.getEvenement().getIdEvent() == e.getIdEvent()))
                .sorted((e1, e2) -> {
                    double score1 = calculateScore(e1, categoryCounts);
                    double score2 = calculateScore(e2, categoryCounts);
                    return Double.compare(score2, score1); // Higher score first
                })
                .limit(2)
                .collect(Collectors.toList());
    }

    private double calculateScore(Evenement e, Map<String, Long> favorites) {
        double score = 0;

        // Boost score if category matches user's favorites
        if (favorites.containsKey(e.getTypeEvent())) {
            score += 10.0 * favorites.get(e.getTypeEvent());
        }

        // Boost score based on popularity (percentage of seats taken)
        if (e.getNbPlaces() > 0) {
            double popularity = (double) (e.getNbPlaces() - e.getNbRestants()) / e.getNbPlaces();
            score += popularity * 5.0;
        }

        // Slight boost for newer events (higher ID usually means newer in simple DBs)
        score += e.getIdEvent() * 0.01;

        return score;
    }
}
