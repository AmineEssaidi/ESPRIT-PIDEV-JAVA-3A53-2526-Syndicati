package com.syndicati.services.syndicat;

import com.syndicati.models.syndicat.Reclamation;
import com.syndicati.models.syndicat.data.ReclamationRepository;
import com.syndicati.models.user.User;
import com.syndicati.services.mail.AsyncMailerService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for Reclamation business logic.
 * Handles validation and CRUD operations aligned with Horizon patterns.
 */
public class ReclamationService {
    private static final String RECLAMATION_NOTIFICATION_EMAIL = "medbahahamdi2002@gmail.com";

    private final ReclamationRepository repository;

    public ReclamationService() {
        this.repository = new ReclamationRepository();
    }

    public List<Reclamation> listAll() {
        return repository.findAll();
    }

    public List<Reclamation> listByUser(User user) {
        if (user == null || user.getIdUser() == null) {
            return List.of();
        }
        return repository.findByUserId(user.getIdUser());
    }

    public List<Reclamation> listByStatut(String statut) {
        if (statut == null || statut.isBlank()) {
            return List.of();
        }
        return repository.findByStatut(statut);
    }

    public Optional<Reclamation> findById(Integer id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return repository.findById(id);
    }

    public Integer create(String titre, String description, LocalDateTime date, String image, User user) {
        ValidationResult validation = validateCreate(titre, description, date, user);
        if (!validation.valid) {
            System.out.println("ReclamationService.create validation failed: " + validation.message);
            return -1;
        }

        Reclamation reclamation = new Reclamation();
        reclamation.setTitreReclamations(titre);
        reclamation.setDescReclamation(description);
        reclamation.setDateReclamation(date);
        reclamation.setImageReclamation(image);
        reclamation.setUser(user);
        reclamation.setStatutReclamation("en_attente");

        Integer createdId = repository.create(reclamation);
        if (createdId != null && createdId > 0) {
            sendCreationNotification(reclamation, createdId);
        }
        return createdId;
    }

    public boolean update(Integer id, String titre, String description, LocalDateTime date, String statut) {
        if (id == null || id <= 0) {
            return false;
        }

        Optional<Reclamation> existing = repository.findById(id);
        if (existing.isEmpty()) {
            return false;
        }

        Reclamation rec = existing.get();

        if (titre != null && !titre.isBlank()) {
            if (titre.length() < 5) {
                System.out.println("ReclamationService.update: titre must be at least 5 characters");
                return false;
            }
            rec.setTitreReclamations(titre);
        }

        if (description != null && !description.isBlank()) {
            if (description.length() < 10) {
                System.out.println("ReclamationService.update: description must be at least 10 characters");
                return false;
            }
            rec.setDescReclamation(description);
        }

        if (date != null) {
            rec.setDateReclamation(date);
        }

        if (statut != null && Reclamation.STATUTS.contains(statut)) {
            rec.setStatutReclamation(statut);
        }

        rec.setUpdatedAt(LocalDateTime.now());
        return repository.update(rec);
    }

    public boolean updateStatut(Integer id, String statut) {
        if (id == null || id <= 0 || statut == null || !Reclamation.STATUTS.contains(statut)) {
            return false;
        }

        Optional<Reclamation> existing = repository.findById(id);
        if (existing.isEmpty()) {
            return false;
        }

        Reclamation rec = existing.get();
        rec.setStatutReclamation(statut);
        rec.setUpdatedAt(LocalDateTime.now());
        return repository.update(rec);
    }

    public boolean delete(Integer id) {
        if (id == null || id <= 0) {
            return false;
        }
        return repository.delete(id);
    }

    private ValidationResult validateCreate(String titre, String description, LocalDateTime date, User user) {
        if (titre == null || titre.isBlank()) {
            return ValidationResult.invalid("titre_reclamations is required");
        }
        if (titre.length() < 5) {
            return ValidationResult.invalid("titre_reclamations must be at least 5 characters");
        }
        if (titre.length() > 255) {
            return ValidationResult.invalid("titre_reclamations must not exceed 255 characters");
        }

        if (description == null || description.isBlank()) {
            return ValidationResult.invalid("desc_reclamation is required");
        }
        if (description.length() < 10) {
            return ValidationResult.invalid("desc_reclamation must be at least 10 characters");
        }
        if (description.length() > 255) {
            return ValidationResult.invalid("desc_reclamation must not exceed 255 characters");
        }

        if (date == null) {
            return ValidationResult.invalid("date_reclamation is required");
        }

        if (user == null || user.getIdUser() == null || user.getIdUser() <= 0) {
            return ValidationResult.invalid("user is required");
        }

        return ValidationResult.valid();
    }

    public ReclamationRepository getRepository() {
        return repository;
    }

    private void sendCreationNotification(Reclamation reclamation, Integer reclamationId) {
        try {
            String subject = "Nouvelle reclamation creee";
            String html = buildCreationEmailHtml(reclamation, reclamationId);
            AsyncMailerService.getInstance().sendHtmlAsync(
                RECLAMATION_NOTIFICATION_EMAIL,
                subject,
                html
            );
        } catch (Exception e) {
            // Never block reclamation creation if email delivery fails.
            System.err.println("Reclamation notification email failed: " + e.getMessage());
        }
    }

    private String buildCreationEmailHtml(Reclamation reclamation, Integer reclamationId) {
        String titre = safe(reclamation.getTitreReclamations());
        String description = safe(reclamation.getDescReclamation());
        String userEmail = reclamation.getUser() != null ? safe(reclamation.getUser().getEmailUser()) : "N/A";
        String date = reclamation.getDateReclamation() != null ? reclamation.getDateReclamation().toString() : "N/A";

        return "<html><body>"
            + "<h2>Nouvelle reclamation creee</h2>"
            + "<p><b>Titre:</b> " + titre + "</p>"
            + "<p><b>Description:</b> " + description + "</p>"
            + "<p><b>Email utilisateur:</b> " + userEmail + "</p>"
            + "<p><b>Date reclamation:</b> " + date + "</p>"
            + "<p><b>Statut:</b> en_attente</p>"
            + "</body></html>";
    }

    private String safe(String value) {
        return value == null ? "" : value;
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
