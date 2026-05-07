package com.syndicati.services.residence;

import com.syndicati.models.residence.Apartment;
import com.syndicati.models.residence.Residence;
import com.syndicati.models.residence.data.ApartmentRepository;
import com.syndicati.models.residence.data.ResidenceRepository;
import java.util.List;
import java.util.Optional;

/**
 * Residence business logic service.
 */
public class ResidenceService {

    private final ResidenceRepository repository;
    private final ApartmentRepository apartmentRepository;

    public ResidenceService() {
        this.repository = new ResidenceRepository();
        this.apartmentRepository = new ApartmentRepository();
    }

    /**
     * Retrieve all residences.
     */
    public List<Residence> listAll() {
        return repository.findAll();
    }

    /**
     * Find residence by ID.
     */
    public Optional<Residence> findById(Integer id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return repository.findById(id);
    }

    /**
     * Search residences by name (contains).
     */
    public List<Residence> searchByName(String name) {
        if (name == null || name.isBlank()) {
            return List.of();
        }
        return repository.findByNameContains(name);
    }

    /**
     * Create a new residence.
     */
    public Integer create(String name, String address, String image, Integer numberApartments, Integer numberFloors, String numberBlocks) {
        ValidationResult validation = validateCreate(name, address);
        if (!validation.valid) {
            System.out.println("ResidenceService.create validation failed: " + validation.message);
            return -1;
        }

        Residence residence = new Residence(name, address, image, getCurrentDate(), numberApartments, numberFloors, numberBlocks);
        return repository.insert(residence);
    }

    /**
     * Update an existing residence.
     */
    public boolean update(Integer id, String name, String address, String image, Integer numberApartments, Integer numberFloors, String numberBlocks) {
        if (id == null || id <= 0) {
            return false;
        }

        Optional<Residence> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) {
            return false;
        }

        ValidationResult validation = validateUpdate(name, address);
        if (!validation.valid) {
            System.out.println("ResidenceService.update validation failed: " + validation.message);
            return false;
        }

        return repository.update(id, name, address, image, numberApartments, numberFloors, numberBlocks);
    }

    /**
     * Delete a residence by ID.
     */
    public boolean delete(Integer id) {
        if (id == null || id <= 0) {
            return false;
        }

        Optional<Residence> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) {
            return false;
        }

        // Delete all apartments in this residence first
        List<Apartment> apartments = apartmentRepository.findByResidenceId(id);
        for (Apartment apartment : apartments) {
            apartmentRepository.delete(apartment.getIdApartment());
        }

        return repository.delete(id);
    }

    /**
     * Get apartments for a residence.
     */
    public List<Apartment> getApartmentsByResidence(Integer residenceId) {
        if (residenceId == null || residenceId <= 0) {
            return List.of();
        }
        return apartmentRepository.findByResidenceId(residenceId);
    }

    /**
     * Get apartment count for a residence.
     */
    public Integer getApartmentCount(Integer residenceId) {
        if (residenceId == null || residenceId <= 0) {
            return 0;
        }
        return getApartmentsByResidence(residenceId).size();
    }

    private ValidationResult validateCreate(String name, String address) {
        if (name == null || name.isBlank()) {
            return new ValidationResult(false, "Residence name is required");
        }
        if (name.length() < 3) {
            return new ValidationResult(false, "Residence name must be at least 3 characters");
        }
        if (address == null || address.isBlank()) {
            return new ValidationResult(false, "Address is required");
        }
        return new ValidationResult(true, "");
    }

    private ValidationResult validateUpdate(String name, String address) {
        return validateCreate(name, address);
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
