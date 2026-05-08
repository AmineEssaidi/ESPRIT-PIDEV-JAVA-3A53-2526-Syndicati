package com.syndicati.controllers.user.profile;

import com.syndicati.utils.image.imagekit.ImageKitConfig;
import com.syndicati.utils.image.imagekit.ImageKitStorageService;
import com.syndicati.utils.image.imagekit.ImageKitUploadResult;
import com.syndicati.models.user.Profile;
import com.syndicati.services.ProfileImageService;
import java.io.File;

/**
 * Handles profile avatar upload/update workflow for views.
 */
public class ProfileAvatarController {

    private final ProfileController profileController;

    public ProfileAvatarController() {
        this.profileController = new ProfileController();
    }

    public AvatarUpdateResult updateAvatar(Profile profile, byte[] fileData, String originalFileName) {
        if (profile == null) {
            return AvatarUpdateResult.failure("No profile loaded");
        }

        String localPath = ProfileImageService.saveAvatarImage(fileData, originalFileName, profile.getIdProfile());
        if (localPath == null) {
            return AvatarUpdateResult.failure("Failed to save image");
        }

        // Prefer ImageKit URL, but keep local as fallback.
        String finalPath = tryUploadToImageKit(localPath);
        profile.setAvatar(finalPath);
        boolean updated = profileController.updateProfile(profile);
        if (!updated) {
            return AvatarUpdateResult.failure("Failed to update profile");
        }

        return AvatarUpdateResult.success(finalPath);
    }

    public AvatarUpdateResult generateAvatar(Profile profile, String prompt) {
        if (profile == null) {
            return AvatarUpdateResult.failure("No profile loaded");
        }

        String localPath = ProfileImageService.saveGeneratedAvatarImage(prompt, profile.getIdProfile());
        if (localPath == null) {
            String serviceMsg = ProfileImageService.getLastErrorMessage();
            String message = (serviceMsg == null || serviceMsg.isBlank()) ? "Failed to generate image" : serviceMsg;
            return AvatarUpdateResult.failure(message);
        }

        // Prefer ImageKit URL, but keep local as fallback.
        String finalPath = tryUploadToImageKit(localPath);
        profile.setAvatar(finalPath);
        boolean updated = profileController.updateProfile(profile);
        if (!updated) {
            return AvatarUpdateResult.failure("Failed to update profile");
        }

        return AvatarUpdateResult.success(finalPath);
    }

    private String tryUploadToImageKit(String localDbPathOrRelative) {
        if (localDbPathOrRelative == null || localDbPathOrRelative.isBlank()) {
            return localDbPathOrRelative;
        }

        try {
            ImageKitConfig cfg = ImageKitConfig.fromEnv();
            if (cfg == null || !cfg.isEnabled() || cfg.getPrivateKey() == null) {
                return localDbPathOrRelative;
            }

            ImageKitStorageService svc = new ImageKitStorageService(cfg);

            // local path is stored in DB like "uploads/profile_images/avatar_1_xxx.png" (forward slashes)
            File file = new File(System.getProperty("user.dir"), localDbPathOrRelative.replace("/", File.separator));
            if (!file.exists() || !file.isFile()) {
                return localDbPathOrRelative;
            }

            ImageKitUploadResult res = svc.uploadFile(file, "/syndicati/profile_images/");
            if (res != null && res.url() != null && !res.url().isBlank()) {
                return res.url();
            }
        } catch (Exception e) {
            System.err.println("ImageKit upload avatar failed, fallback to local: " + e.getMessage());
        }

        return localDbPathOrRelative;
    }

    public static class AvatarUpdateResult {
        private final boolean success;
        private final String imagePath;
        private final String message;

        private AvatarUpdateResult(boolean success, String imagePath, String message) {
            this.success = success;
            this.imagePath = imagePath;
            this.message = message;
        }

        public static AvatarUpdateResult success(String imagePath) {
            return new AvatarUpdateResult(true, imagePath, "");
        }

        public static AvatarUpdateResult failure(String message) {
            return new AvatarUpdateResult(false, "", message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getImagePath() {
            return imagePath;
        }

        public String getMessage() {
            return message;
        }
    }
}