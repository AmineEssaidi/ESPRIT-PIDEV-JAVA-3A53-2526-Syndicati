package com.syndicati.services;

import com.syndicati.models.evenement.Evenement;
import com.syndicati.models.evenement.Participation;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service to provide event recommendations based on user history and popularity.
 */
public class RecommendationService {

    public RecommendationService() {}

    /**
     * Recommends events based on local logic (Category matching + Popularity)
     */
    public List<Evenement> getRecommendations(List<Evenement> allEvents, List<Participation> userHistory) {
        if (allEvents == null || allEvents.isEmpty()) return new ArrayList<>();
        if (userHistory == null) userHistory = new ArrayList<>();

        // 1. Identify user's favorite categories from history
        Map<String, Long> categoryCounts = userHistory.stream()
                .map(p -> p.getEvenement() != null ? p.getEvenement().getTypeEvent() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(type -> type, Collectors.counting()));

        final List<Participation> history = userHistory;

        // 2. Deduplicate events by ID (in case DB returns duplicates)
        List<Evenement> uniqueEvents = allEvents.stream()
                .filter(e -> e != null && e.getIdEvent() != null)
                .collect(Collectors.toMap(
                        Evenement::getIdEvent,
                        e -> e,
                        (a, b) -> a, // Keep first occurrence
                        LinkedHashMap::new
                ))
                .values()
                .stream()
                .collect(Collectors.toList());

        // 3. Score each unique event
        return uniqueEvents.stream()
                // Filter out events user is already participating in
                .filter(e -> history.stream().noneMatch(p -> p.getEvenement() != null && p.getEvenement().getIdEvent().equals(e.getIdEvent())))
                .sorted((e1, e2) -> {
                    double score1 = calculateScore(e1, categoryCounts);
                    double score2 = calculateScore(e2, categoryCounts);
                    return Double.compare(score2, score1); // Higher score first
                })
                .limit(4) // Show up to 4 recommendations
                .collect(Collectors.toList());
    }

    private double calculateScore(Evenement e, Map<String, Long> favorites) {
        double score = 0;

        // Boost score if category matches user's favorites
        if (favorites.containsKey(e.getTypeEvent())) {
            score += 10.0 * favorites.get(e.getTypeEvent());
        }

        // Boost score based on popularity (percentage of seats taken)
        if (e.getNbPlaces() != null && e.getNbPlaces() > 0 && e.getNbRestants() != null) {
            double popularity = (double) (e.getNbPlaces() - e.getNbRestants()) / e.getNbPlaces();
            score += popularity * 5.0;
        }

        // Slight boost for newer events
        if (e.getIdEvent() != null) {
            score += e.getIdEvent() * 0.01;
        }

        return score;
    }
}
