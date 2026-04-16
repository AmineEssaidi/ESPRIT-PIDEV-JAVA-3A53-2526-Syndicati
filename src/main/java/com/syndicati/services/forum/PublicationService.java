package com.syndicati.services.forum;

import com.syndicati.models.forum.Publication;
import com.syndicati.models.forum.data.PublicationRepository;
import com.syndicati.models.user.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Forum publication business logic.
 */
public class PublicationService {

    private final PublicationRepository repository;

    public PublicationService() {
        this.repository = new PublicationRepository();
    }

    public List<Publication> listAll() {
        return repository.findAll();
    }

    public List<Publication> listByUser(User user) {
        if (user == null || user.getIdUser() == null) {
            return List.of();
        }
        return repository.findByUserId(user.getIdUser());
    }

    public List<Publication> listByCategory(String category) {
        if (category == null || category.isBlank()) {
            return List.of();
        }
        return repository.findByCategory(category);
    }

    public Optional<Publication> findById(Integer id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return repository.findById(id);
    }

    public Integer create(String titre, String description, String categorie, String image, User user) {
        ValidationResult validation = validateCreate(titre, description, categorie, user);
        if (!validation.valid) {
            System.out.println("PublicationService.create validation failed: " + validation.message);
            return -1;
        }

        Publication publication = new Publication();
        publication.setTitrePub(titre);
        publication.setDescriptionPub(description);
        publication.setCategoriePub(categorie);
        publication.setImagePub(image);
        publication.setUser(user);
        publication.setDateCreationPub(LocalDateTime.now());

        return repository.create(publication);
    }

    public boolean update(Integer id, String titre, String description, String categorie, String image) {
        if (id == null || id <= 0) {
            return false;
        }

        Optional<Publication> existing = repository.findById(id);
        if (existing.isEmpty()) {
            return false;
        }

        Publication publication = existing.get();

        if (titre != null && !titre.isBlank()) {
            if (titre.length() < 5) {
                return false;
            }
            publication.setTitrePub(titre);
        }

        if (description != null && !description.isBlank()) {
            if (description.length() < 10) {
                return false;
            }
            publication.setDescriptionPub(description);
        }

        if (categorie != null && !categorie.isBlank()) {
            if (!Publication.CATEGORIES.contains(categorie)) {
                return false;
            }
            publication.setCategoriePub(categorie);
        }

        if (image != null) {
            publication.setImagePub(image);
        }

        if (publication.getDateCreationPub() == null) {
            publication.setDateCreationPub(LocalDateTime.now());
        }

        return repository.update(publication);
    }

    public boolean delete(Integer id) {
        if (id == null || id <= 0) {
            return false;
        }
        return repository.delete(id);
    }

    private ValidationResult validateCreate(String titre, String description, String categorie, User user) {
        if (titre == null || titre.isBlank()) {
            return ValidationResult.invalid("titre_pub is required");
        }
        if (titre.length() < 5) {
            return ValidationResult.invalid("titre_pub must be at least 5 characters");
        }
        if (titre.length() > 255) {
            return ValidationResult.invalid("titre_pub must not exceed 255 characters");
        }

        if (description == null || description.isBlank()) {
            return ValidationResult.invalid("description_pub is required");
        }
        if (description.length() < 10) {
            return ValidationResult.invalid("description_pub must be at least 10 characters");
        }
        if (description.length() > 255) {
            return ValidationResult.invalid("description_pub must not exceed 255 characters");
        }

        if (categorie == null || categorie.isBlank()) {
            return ValidationResult.invalid("categorie_pub is required");
        }
        if (!Publication.CATEGORIES.contains(categorie)) {
            return ValidationResult.invalid("Invalid publication category");
        }

        if (user == null || user.getIdUser() == null || user.getIdUser() <= 0) {
            return ValidationResult.invalid("user is required");
        }

        return ValidationResult.valid();
    }

    public PublicationRepository getRepository() {
        return repository;
    }

    private static class ValidationResult {
        final boolean valid;
        final String message;

        ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        static ValidationResult valid() {
            return new ValidationResult(true, "");
        }

        static ValidationResult invalid(String message) {
            return new ValidationResult(false, message);
        }
    }
}