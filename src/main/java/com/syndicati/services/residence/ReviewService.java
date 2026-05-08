package com.syndicati.services.residence;

import com.syndicati.models.residence.Apartment;
import com.syndicati.models.residence.Review;
import com.syndicati.models.residence.data.ApartmentRepository;
import com.syndicati.models.residence.data.ReviewRepository;
import java.util.List;
import java.util.Optional;

/**
 * Review business logic service.
 */
public class ReviewService {

    private final ReviewRepository repository;
    private final ApartmentRepository apartmentRepository;

    public ReviewService() {
        this.repository = new ReviewRepository();
        this.apartmentRepository = new ApartmentRepository();
    }

    /**
     * Retrieve all reviews.
     */
    public List<Review> listAll() {
        return repository.findAll();
    }

    /**
     * Find review by ID.
     */
    public Optional<Review> findById(Integer id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return repository.findById(id);
    }

    /**
     * Get all reviews for an apartment.
     */
    public List<Review> findByApartmentId(Integer apartmentId) {
        if (apartmentId == null || apartmentId <= 0) {
            return List.of();
        }
        return repository.findByApartmentId(apartmentId);
    }

    /**
     * Get all reviews by a user.
     */
    public List<Review> findByUserId(Integer userId) {
        if (userId == null || userId <= 0) {
            return List.of();
        }
        return repository.findByUserId(userId);
    }

    /**
     * Find a user's review for a specific apartment.
     */
    public Optional<Review> findByUserAndApartment(Integer userId, Integer apartmentId) {
        if (userId == null || userId <= 0 || apartmentId == null || apartmentId <= 0) {
            return Optional.empty();
        }
        return repository.findByUserAndApartment(userId, apartmentId);
    }

    /**
     * Create a new review.
     */
    public Integer create(Integer userId, Integer apartmentId, Integer score) {
        ValidationResult validation = validateCreate(userId, apartmentId, score);
        if (!validation.valid) {
            System.out.println("ReviewService.create validation failed: " + validation.message);
            return -1;
        }

        // Check if user already reviewed this apartment
        Optional<Review> existingReview = repository.findByUserAndApartment(userId, apartmentId);
        if (existingReview.isPresent()) {
            return -1;  // User already reviewed this apartment
        }

        Review review = new Review(userId, apartmentId, score);
        return repository.insert(review);
    }

    /**
     * Update an existing review.
     */
    public boolean update(Integer id, Integer score) {
        if (id == null || id <= 0) {
            return false;
        }

        Optional<Review> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) {
            return false;
        }

        ValidationResult validation = validateScore(score);
        if (!validation.valid) {
            System.out.println("ReviewService.update validation failed: " + validation.message);
            return false;
        }

        return repository.update(id, score);
    }

    /**
     * Delete a review by ID.
     */
    public boolean delete(Integer id) {
        if (id == null || id <= 0) {
            return false;
        }

        Optional<Review> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) {
            return false;
        }

        return repository.delete(id);
    }

    /**
     * Get average score for an apartment.
     */
    public Double getAverageScore(Integer apartmentId) {
        if (apartmentId == null || apartmentId <= 0) {
            return 0.0;
        }
        return repository.getAverageScoreForApartment(apartmentId);
    }

    /**
     * Get review count for an apartment.
     */
    public Integer getReviewCount(Integer apartmentId) {
        if (apartmentId == null || apartmentId <= 0) {
            return 0;
        }
        return findByApartmentId(apartmentId).size();
    }

    /**
     * Get formatted average score (rounded to 1 decimal place).
     */
    public String getFormattedAverageScore(Integer apartmentId) {
        Double avgScore = getAverageScore(apartmentId);
        if (avgScore == null || avgScore == 0.0) {
            return "No ratings";
        }
        return String.format("%.1f", avgScore);
    }

    private ValidationResult validateCreate(Integer userId, Integer apartmentId, Integer score) {
        if (userId == null || userId <= 0) {
            return new ValidationResult(false, "User ID is required");
        }

        if (apartmentId == null || apartmentId <= 0) {
            return new ValidationResult(false, "Apartment ID is required");
        }

        Optional<Apartment> aptOpt = apartmentRepository.findById(apartmentId);
        if (aptOpt.isEmpty()) {
            return new ValidationResult(false, "Apartment not found");
        }

        return validateScore(score);
    }

    private ValidationResult validateScore(Integer score) {
        if (score == null) {
            return new ValidationResult(false, "Score is required");
        }
        if (score < 0 || score > 10) {
            return new ValidationResult(false, "Score must be between 0 and 10");
        }
        return new ValidationResult(true, "");
    }

    private static class ValidationResult {
        boolean valid;
        String message;

        ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
    }
}
