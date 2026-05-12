package com.syndicati.controllers.residence;

import com.syndicati.models.residence.Apartment;
import com.syndicati.models.residence.Maintenance;
import com.syndicati.models.residence.Review;
import com.syndicati.services.ai.MistralAIService;
import com.syndicati.services.residence.MaintenanceService;
import com.syndicati.services.residence.ReviewService;
import java.util.List;
import java.util.Optional;

/**
 * Controller facade for maintenance and reviews.
 */
public class MaintenanceController {

    private final MaintenanceService maintenanceService;
    private final ReviewService reviewService;
    private final MistralAIService mistralAIService;

    public MaintenanceController() {
        this.maintenanceService = new MaintenanceService();
        this.reviewService = new ReviewService();
        this.mistralAIService = new MistralAIService();
    }

    // ===== MAINTENANCE METHODS =====

    public List<Maintenance> maintenanceRecords() {
        return maintenanceService.listAll();
    }

    public Optional<Maintenance> maintenanceById(Integer id) {
        return maintenanceService.findById(id);
    }

    public List<Maintenance> maintenanceByApartment(Integer apartmentId) {
        return maintenanceService.findByApartmentId(apartmentId);
    }

    public Optional<Maintenance> latestMaintenance(Integer apartmentId) {
        return maintenanceService.getLatestMaintenance(apartmentId);
    }

    public boolean apartmentNeedsMaintenance(Integer apartmentId) {
        return maintenanceService.needsMaintenance(apartmentId);
    }

    public Integer maintenanceCreate(Integer apartmentId, String generalCondition, String plumbingCondition,
                                    String electricalCondition, String heatingCondition, String description,
                                    String aiRecommendation) {
        return maintenanceService.create(apartmentId, generalCondition, plumbingCondition,
                                        electricalCondition, heatingCondition, description, aiRecommendation);
    }

    public boolean maintenanceUpdate(Integer id, String generalCondition, String plumbingCondition,
                                    String electricalCondition, String heatingCondition, String description,
                                    String aiRecommendation) {
        return maintenanceService.update(id, generalCondition, plumbingCondition,
                                        electricalCondition, heatingCondition, description, aiRecommendation);
    }

    public String generateMistralRecommendation(Apartment apartment, Maintenance maintenance) {
        return mistralAIService.generateRecommendation(apartment, maintenance);
    }

    public boolean maintenanceDelete(Integer id) {
        return maintenanceService.delete(id);
    }

    // ===== REVIEW METHODS =====

    public List<Review> reviews() {
        return reviewService.listAll();
    }

    public Optional<Review> reviewById(Integer id) {
        return reviewService.findById(id);
    }

    public List<Review> reviewsByApartment(Integer apartmentId) {
        return reviewService.findByApartmentId(apartmentId);
    }

    public List<Review> reviewsByUser(Integer userId) {
        return reviewService.findByUserId(userId);
    }

    public Optional<Review> userReviewForApartment(Integer userId, Integer apartmentId) {
        return reviewService.findByUserAndApartment(userId, apartmentId);
    }

    public Double averageApartmentScore(Integer apartmentId) {
        return reviewService.getAverageScore(apartmentId);
    }

    public String formattedApartmentScore(Integer apartmentId) {
        return reviewService.getFormattedAverageScore(apartmentId);
    }

    public Integer apartmentReviewCount(Integer apartmentId) {
        return reviewService.getReviewCount(apartmentId);
    }

    public Integer reviewCreate(Integer userId, Integer apartmentId, Integer score) {
        return reviewService.create(userId, apartmentId, score);
    }

    public boolean reviewUpdate(Integer id, Integer score) {
        return reviewService.update(id, score);
    }

    public boolean reviewDelete(Integer id) {
        return reviewService.delete(id);
    }

    // ===== SERVICE GETTERS =====

    public MaintenanceService getMaintenanceService() {
        return maintenanceService;
    }

    public ReviewService getReviewService() {
        return reviewService;
    }
}
