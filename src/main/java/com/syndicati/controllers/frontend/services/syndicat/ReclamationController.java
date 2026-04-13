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
    
    public void handleSubmit(String subject, String desc, File imageFile, Runnable onSuccess, Consumer<String> onError) {
        if (subject == null || subject.trim().isEmpty()) {
            onError.accept("Subject is required.");
            return;
        }
        
        if (desc == null || desc.trim().isEmpty()) {
            onError.accept("Description is required.");
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

        Reclamation reclamation = new Reclamation(
            subject.trim(), 
            desc.trim(), 
            java.time.LocalDateTime.now(), 
            6 // hardcoded user_id per instruction
        );
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
