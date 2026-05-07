package com.syndicati.services.residence;

import com.syndicati.models.residence.Apartment;
import com.syndicati.models.residence.Residence;
import com.syndicati.models.residence.data.ApartmentRepository;
import com.syndicati.models.residence.data.ResidenceRepository;
import java.util.List;
import java.util.Optional;

/**
 * Apartment business logic service.
 */
public class ApartmentService {

    private final ApartmentRepository repository;
    private final ResidenceRepository residenceRepository;

    public ApartmentService() {
        this.repository = new ApartmentRepository();
        this.residenceRepository = new ResidenceRepository();
    }

    /**
     * Retrieve all apartments.
     */
    public List<Apartment> listAll() {
        return repository.findAll();
    }

    /**
     * Find apartment by ID.
     */
    public Optional<Apartment> findById(Integer id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return repository.findById(id);
    }

    /**
     * Get all apartments in a residence.
     */
    public List<Apartment> findByResidenceId(Integer residenceId) {
        if (residenceId == null || residenceId <= 0) {
            return List.of();
        }
        return repository.findByResidenceId(residenceId);
    }

    /**
     * Get available apartments.
     */
    public List<Apartment> findAvailable() {
        return repository.findByAvailable(1);
    }

    /**
     * Get unavailable apartments.
     */
    public List<Apartment> findUnavailable() {
        return repository.findByAvailable(0);
    }

    /**
     * Create a new apartment.
     */
    public Integer create(Integer residenceId, Integer userId, Integer parking, Integer available,
                         String image, String type, String info, Integer area, Integer rentalPrice,
                         Integer salePrice, String dateConstructed) {
        ValidationResult validation = validateCreate(residenceId, type, area, rentalPrice);
        if (!validation.valid) {
            System.out.println("ApartmentService.create validation failed: " + validation.message);
            return -1;
        }

        Apartment apartment = new Apartment(residenceId, userId, parking, available, image, type, info,
                                           area, rentalPrice, salePrice, dateConstructed);
        return repository.insert(apartment);
    }

    /**
     * Update an existing apartment.
     */
    public boolean update(Integer id, Integer residenceId, Integer userId, Integer parking, Integer available,
                         String image, String type, String info, Integer area, Integer rentalPrice,
                         Integer salePrice, String dateConstructed) {
        if (id == null || id <= 0) {
            return false;
        }

        Optional<Apartment> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) {
            return false;
        }

        ValidationResult validation = validateUpdate(type, area, rentalPrice);
        if (!validation.valid) {
            System.out.println("ApartmentService.update validation failed: " + validation.message);
            return false;
        }

        return repository.update(id, residenceId, userId, parking, available, image, type, info,
                                area, rentalPrice, salePrice, dateConstructed);
    }

    /**
     * Delete an apartment by ID.
     */
    public boolean delete(Integer id) {
        if (id == null || id <= 0) {
            return false;
        }

        Optional<Apartment> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) {
            return false;
        }

        return repository.delete(id);
    }

    /**
     * Mark apartment as available.
     */
    public boolean markAvailable(Integer id) {
        Optional<Apartment> aptOpt = repository.findById(id);
        if (aptOpt.isEmpty()) {
            return false;
        }

        Apartment apt = aptOpt.get();
        return repository.update(id, apt.getIdResidence(), apt.getIdUser(), apt.getParking(), 1,
                                apt.getImageApartment(), apt.getTypeApartment(), apt.getApartmentInfo(),
                                apt.getArea(), apt.getRentalPrice(), apt.getSalePrice(), apt.getDateConstructed());
    }

    /**
     * Mark apartment as unavailable.
     */
    public boolean markUnavailable(Integer id) {
        Optional<Apartment> aptOpt = repository.findById(id);
        if (aptOpt.isEmpty()) {
            return false;
        }

        Apartment apt = aptOpt.get();
        return repository.update(id, apt.getIdResidence(), apt.getIdUser(), apt.getParking(), 0,
                                apt.getImageApartment(), apt.getTypeApartment(), apt.getApartmentInfo(),
                                apt.getArea(), apt.getRentalPrice(), apt.getSalePrice(), apt.getDateConstructed());
    }

    /**
     * Get available apartments in a residence.
     */
    public List<Apartment> getAvailableInResidence(Integer residenceId) {
        List<Apartment> apartments = findByResidenceId(residenceId);
        return apartments.stream().filter(apt -> apt.getAvailable() == 1).toList();
    }

    private ValidationResult validateCreate(Integer residenceId, String type, Integer area, Integer rentalPrice) {
        if (residenceId == null || residenceId <= 0) {
            return new ValidationResult(false, "Residence ID is required");
        }

        Optional<Residence> resOpt = residenceRepository.findById(residenceId);
        if (resOpt.isEmpty()) {
            return new ValidationResult(false, "Residence not found");
        }

        if (type == null || type.isBlank()) {
            return new ValidationResult(false, "Apartment type is required");
        }

        if (area == null || area <= 0) {
            return new ValidationResult(false, "Area must be greater than 0");
        }

        if (rentalPrice == null || rentalPrice < 0) {
            return new ValidationResult(false, "Rental price cannot be negative");
        }

        return new ValidationResult(true, "");
    }

    private ValidationResult validateUpdate(String type, Integer area, Integer rentalPrice) {
        if (type == null || type.isBlank()) {
            return new ValidationResult(false, "Apartment type is required");
        }
        if (area == null || area <= 0) {
            return new ValidationResult(false, "Area must be greater than 0");
        }
        if (rentalPrice == null || rentalPrice < 0) {
            return new ValidationResult(false, "Rental price cannot be negative");
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
