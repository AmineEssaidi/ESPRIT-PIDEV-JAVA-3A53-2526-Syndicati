package com.syndicati.services.residence;

import com.syndicati.models.residence.Appartement;

import java.util.Comparator;
import java.util.List;

public class ServiceRecommendationResidence {

    public List<Appartement> AppartementsSimilaires(Appartement ref, List<Appartement> all, int limit) {
        return all.stream()
                .filter(a -> a.getId_app() != ref.getId_app() && a.getResidence_id() != ref.getResidence_id())
                .sorted(Comparator.comparingDouble((Appartement a) -> -scoreAppartement(ref, a)))
                .limit(limit)
                .toList();
    }

    public double scoreAppartement(Appartement ref, Appartement apt) {
        double score = 0;

        if (ref.getType_a().equals(apt.getType_a())) {
            score += 50;
        } else {
            Integer n1 = extraireType(ref.getType_a()), n2 = extraireType(apt.getType_a());
            if (n1 != null && n2 != null) score += switch (Math.abs(n1 - n2)) {
                case 1 -> 30;
                case 2 -> 15;
                default -> 0;
            };
        }

        if (ref.getPrix_location() > 0)
            score += switch (bucket(Math.abs(ref.getPrix_location() - apt.getPrix_location()) / (double) ref.getPrix_location())) {
                case 1 -> 40;
                case 2 -> 28;
                case 3 -> 16;
                case 4 -> 6;
                default -> 0;
            };

        if (ref.getSuperficie() > 0)
            score += switch (bucket(Math.abs(ref.getSuperficie() - apt.getSuperficie()) / (double) ref.getSuperficie())) {
                case 1 -> 10;
                case 2 -> 7;
                case 3 -> 4;
                default -> 0;
            };

        return score;
    }

    private int bucket(double diff) {
        if (diff <= 0.1) return 1;
        if (diff <= 0.2) return 2;
        if (diff <= 0.3) return 3;
        if (diff <= 0.5) return 4;
        return 0;
    }

    private Integer extraireType(String type) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("S\\+(\\d+)").matcher(type);
        return m.find() ? Integer.parseInt(m.group(1)) : null;
    }
}