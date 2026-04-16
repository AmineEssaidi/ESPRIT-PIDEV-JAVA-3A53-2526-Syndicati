package com.syndicati.controllers.frontend.services.syndicat;

import com.syndicati.models.entities.Reclamation;
import com.syndicati.services.ReclamationService;
import java.time.LocalDate;
import java.util.function.Consumer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class ReclamationController {

    private final ReclamationService reclamationService;

    public ReclamationController() {
        this.reclamationService = ReclamationService.getInstance();
    }

    public void handleSubmit(String subject, String desc, File imageFile, Runnable onSuccess,
            Consumer<String> onError) {
        // Validate subject (title)
        if (subject == null || subject.trim().isEmpty()) {
            onError.accept("Subject is required.");
            return;
        }
        String trimmedSubject = subject.trim();
        if (trimmedSubject.length() < 4 || trimmedSubject.length() > 30) {
            onError.accept("Subject must be between 4 and 30 characters long.");
            return;
        }
        if (!Character.isLetter(trimmedSubject.charAt(0))) {
            onError.accept("Subject must start with a letter.");
            return;
        }

        // Validate description
        if (desc == null || desc.trim().isEmpty()) {
            onError.accept("Description is required.");
            return;
        }
        String trimmedDesc = desc.trim();
        if (trimmedDesc.length() < 5 || trimmedDesc.length() > 200) {
            onError.accept("Description must be between 5 and 200 characters long.");
            return;
        }

        String savedImageName = null;
        if (imageFile != null && imageFile.exists()) {
            try {
                String originalName = imageFile.getName();
                String extension = originalName.substring(originalName.lastIndexOf("."));
                savedImageName = UUID.randomUUID().toString() + extension;

                Path targetDir = Paths.get("uploads", "reclamation_images");
                if (!Files.exists(targetDir)) {
                    Files.createDirectories(targetDir);
                }

                Path targetPath = targetDir.resolve(savedImageName);
                Files.copy(imageFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            } catch (Exception e) {
                System.err.println("Failed to upload file: " + e.getMessage());
                onError.accept("Failed to upload the image file.");
                return;
            }
        }

        com.syndicati.models.entities.User currentUser = com.syndicati.utils.session.SessionManager.getInstance()
                .getCurrentUser();
        int userId = (currentUser != null && currentUser.getIdUser() != null) ? currentUser.getIdUser() : 6;

        Reclamation reclamation = new Reclamation(
                trimmedSubject,
                trimmedDesc,
                java.time.LocalDateTime.now(),
                userId);
        reclamation.setImagereclamation(savedImageName);

        boolean success = reclamationService.createReclamation(reclamation);

        if (success) {
            System.out.println("Reclamation submitted successfully to DB for User 6.");
            if (onSuccess != null) {
                onSuccess.run();
            }
        } else {
            System.err.println("Database insertion failed for reclamation.");
            if (onError != null) {
                onError.accept("Failed to submit reclamation. Please try again.");
            }
        }
    }
}
