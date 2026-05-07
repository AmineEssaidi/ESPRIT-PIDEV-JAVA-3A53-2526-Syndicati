package com.syndicati.services.residence;

import com.syndicati.models.residence.Apartment;
import com.syndicati.models.residence.Maintenance;
import com.syndicati.models.residence.data.ApartmentRepository;
import com.syndicati.models.residence.data.MaintenanceRepository;
import java.util.List;
import java.util.Optional;

/**
 * Maintenance business logic service.
 */
public class MaintenanceService {

    private final MaintenanceRepository repository;
    private final ApartmentRepository apartmentRepository;

    public MaintenanceService() {
        this.repository = new MaintenanceRepository();
        this.apartmentRepository = new ApartmentRepository();
    }

    /**
     * Retrieve all maintenance records.
     */
    public List<Maintenance> listAll() {
        return repository.findAll();
    }

    /**
     * Find maintenance record by ID.
     */
    public Optional<Maintenance> findById(Integer id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return repository.findById(id);
    }

    /**
     * Get all maintenance records for an apartment.
     */
    public List<Maintenance> findByApartmentId(Integer apartmentId) {
        if (apartmentId == null || apartmentId <= 0) {
            return List.of();
        }
        return repository.findByApartmentId(apartmentId);
    }

    /**
     * Create a new maintenance record.
     */
    public Integer create(Integer apartmentId, String generalCondition, String plumbingCondition,
                         String electricalCondition, String heatingCondition, String description,
                         String aiRecommendation) {
        ValidationResult validation = validateCreate(apartmentId, generalCondition);
        if (!validation.valid) {
            System.out.println("MaintenanceService.create validation failed: " + validation.message);
            return -1;
        }

        Maintenance maintenance = new Maintenance(
            apartmentId,
            generalCondition,
            plumbingCondition,
            electricalCondition,
            heatingCondition,
            getCurrentDate(),
            description,
            aiRecommendation
        );
        return repository.insert(maintenance);
    }

    /**
     * Update an existing maintenance record.
     */
    public boolean update(Integer id, String generalCondition, String plumbingCondition,
                         String electricalCondition, String heatingCondition, String description,
                         String aiRecommendation) {
        if (id == null || id <= 0) {
            return false;
        }

        Optional<Maintenance> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) {
            return false;
        }

        ValidationResult validation = validateUpdate(generalCondition);
        if (!validation.valid) {
            System.out.println("MaintenanceService.update validation failed: " + validation.message);
            return false;
        }

        return repository.update(id, generalCondition, plumbingCondition, electricalCondition,
                                heatingCondition, getCurrentDate(), description, aiRecommendation);
    }

    /**
     * Delete a maintenance record by ID.
     */
    public boolean delete(Integer id) {
        if (id == null || id <= 0) {
            return false;
        }

        Optional<Maintenance> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) {
            return false;
        }

        return repository.delete(id);
    }

    /**
     * Delete all maintenance records for an apartment (e.g., when apartment is deleted).
     */
    public boolean deleteByApartmentId(Integer apartmentId) {
        if (apartmentId == null || apartmentId <= 0) {
            return false;
        }
        return repository.deleteByApartmentId(apartmentId);
    }

    /**
     * Get latest maintenance record for an apartment.
     */
    public Optional<Maintenance> getLatestMaintenance(Integer apartmentId) {
        if (apartmentId == null || apartmentId <= 0) {
            return Optional.empty();
        }

        List<Maintenance> records = repository.findByApartmentId(apartmentId);
        if (records.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(records.get(0));  // Already sorted DESC by date
    }

    /**
     * Check if apartment needs maintenance based on last maintenance date.
     * Returns true if last maintenance was more than 6 months ago.
     */
    public boolean needsMaintenance(Integer apartmentId) {
        Optional<Maintenance> latestOpt = getLatestMaintenance(apartmentId);
        if (latestOpt.isEmpty()) {
            return true;  // No maintenance record, needs maintenance
        }

        Maintenance latest = latestOpt.get();
        // Simple check: if last maintenance was more than 180 days ago
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.util.Date lastDate = sdf.parse(latest.getLastMaintenanceDate());
            long daysDifference = (System.currentTimeMillis() - lastDate.getTime()) / (1000 * 60 * 60 * 24);
            return daysDifference > 180;
        } catch (Exception e) {
            return true;  // If we can't parse, assume it needs maintenance
        }
    }

    private ValidationResult validateCreate(Integer apartmentId, String generalCondition) {
        if (apartmentId == null || apartmentId <= 0) {
            return new ValidationResult(false, "Apartment ID is required");
        }

        Optional<Apartment> aptOpt = apartmentRepository.findById(apartmentId);
        if (aptOpt.isEmpty()) {
            return new ValidationResult(false, "Apartment not found");
        }

        if (generalCondition == null || generalCondition.isBlank()) {
            return new ValidationResult(false, "General condition is required");
        }

        return new ValidationResult(true, "");
    }

    private ValidationResult validateUpdate(String generalCondition) {
        if (generalCondition == null || generalCondition.isBlank()) {
            return new ValidationResult(false, "General condition is required");
        }
        return new ValidationResult(true, "");
    }

    private String getCurrentDate() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
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
