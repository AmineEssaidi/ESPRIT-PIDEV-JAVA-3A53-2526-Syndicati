package com.syndicati.controllers.user.profile;

import com.syndicati.models.user.Profile;
import com.syndicati.services.user.profile.ProfileService;
import com.syndicati.utils.notifications.GlobalNotificationPillManager;

import java.util.List;
import java.util.Optional;

/**
 * Profile-focused controller for Java-side CRUD orchestration.
 */
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController() {
        this.profileService = new ProfileService();
    }

    public List<Profile> profiles() {
        return profileService.listProfiles();
    }

    public Optional<Profile> profileById(int idProfile) {
        return profileService.findById(idProfile);
    }

    public Optional<Profile> profileByUserId(int userId) {
        return profileService.findOneByUserId(userId);
    }

    public Optional<Profile> findOneByUserId(int userId) {
        return profileService.findOneByUserId(userId);
    }

    public boolean profileEdit(Profile profile) {
        boolean success = profileService.updateProfile(profile);
        if (success) {
            GlobalNotificationPillManager.updated("Profile", "Profile updated successfully.");
        }
        return success;
    }

    public Optional<Integer> profileCreate(Profile profile) {
        Optional<Integer> id = profileService.createProfile(profile);
        if (id.isPresent() && id.get() > 0) {
            GlobalNotificationPillManager.created("Profile", "Profile created successfully.");
        }
        return id;
    }

    public boolean profileUpdate(Profile profile) {
        boolean success = profileService.updateProfile(profile);
        if (success) {
            GlobalNotificationPillManager.updated("Profile", "Profile updated successfully.");
        }
        return success;
    }

    public boolean updateProfile(Profile profile) {
        boolean success = profileService.updateProfile(profile);
        if (success) {
            GlobalNotificationPillManager.updated("Profile", "Profile updated successfully.");
        }
        return success;
    }

    public boolean profileDelete(int idProfile) {
        boolean success = profileService.deleteProfile(idProfile);
        if (success) {
            GlobalNotificationPillManager.deleted("Profile", "Profile deleted successfully.");
        }
        return success;
    }
}
