package com.syndicati.controllers.residence;

import com.syndicati.models.residence.Apartment;
import com.syndicati.models.residence.Residence;
import com.syndicati.services.residence.ApartmentService;
import com.syndicati.services.residence.ResidenceService;
import java.util.List;
import java.util.Optional;

/**
 * Controller facade for residences.
 */
public class ResidenceController {

    private final ResidenceService residenceService;
    private final ApartmentService apartmentService;

    public ResidenceController() {
        this.residenceService = new ResidenceService();
        this.apartmentService = new ApartmentService();
    }

    // ===== RESIDENCE METHODS =====

    public List<Residence> residences() {
        return residenceService.listAll();
    }

    public Optional<Residence> residenceById(Integer id) {
        return residenceService.findById(id);
    }

    public List<Residence> searchResidences(String name) {
        return residenceService.searchByName(name);
    }

    public Integer residenceCreate(String name, String address, String image, Integer numberApartments,
                                   Integer numberFloors, String numberBlocks) {
        return residenceService.create(name, address, image, numberApartments, numberFloors, numberBlocks);
    }

    public boolean residenceUpdate(Integer id, String name, String address, String image, Integer numberApartments,
                                   Integer numberFloors, String numberBlocks) {
        return residenceService.update(id, name, address, image, numberApartments, numberFloors, numberBlocks);
    }

    public boolean residenceDelete(Integer id) {
        return residenceService.delete(id);
    }

    public Integer residenceApartmentCount(Integer residenceId) {
        return residenceService.getApartmentCount(residenceId);
    }

    // ===== APARTMENT METHODS =====

    public List<Apartment> apartments() {
        return apartmentService.listAll();
    }

    public Optional<Apartment> apartmentById(Integer id) {
        return apartmentService.findById(id);
    }

    public List<Apartment> apartmentsByResidence(Integer residenceId) {
        return apartmentService.findByResidenceId(residenceId);
    }

    public List<Apartment> availableApartments() {
        return apartmentService.findAvailable();
    }

    public List<Apartment> unavailableApartments() {
        return apartmentService.findUnavailable();
    }

    public List<Apartment> availableApartmentsByResidence(Integer residenceId) {
        return apartmentService.getAvailableInResidence(residenceId);
    }

    public Integer apartmentCreate(Integer residenceId, Integer userId, Integer parking, Integer available,
                                   String image, String type, String info, Integer area, Integer rentalPrice,
                                   Integer salePrice, String dateConstructed) {
        return apartmentService.create(residenceId, userId, parking, available, image, type, info,
                                      area, rentalPrice, salePrice, dateConstructed);
    }

    public boolean apartmentUpdate(Integer id, Integer residenceId, Integer userId, Integer parking, Integer available,
                                   String image, String type, String info, Integer area, Integer rentalPrice,
                                   Integer salePrice, String dateConstructed) {
        return apartmentService.update(id, residenceId, userId, parking, available, image, type, info,
                                      area, rentalPrice, salePrice, dateConstructed);
    }

    public boolean apartmentDelete(Integer id) {
        return apartmentService.delete(id);
    }

    public boolean apartmentMarkAvailable(Integer id) {
        return apartmentService.markAvailable(id);
    }

    public boolean apartmentMarkUnavailable(Integer id) {
        return apartmentService.markUnavailable(id);
    }

    // ===== SERVICE GETTERS =====

    public ResidenceService getResidenceService() {
        return residenceService;
    }

    public ApartmentService getApartmentService() {
        return apartmentService;
    }
}
