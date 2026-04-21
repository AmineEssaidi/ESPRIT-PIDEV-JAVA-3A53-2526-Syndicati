package com.syndicati.controllers.users;

import com.syndicati.models.user.Onboarding;
import com.syndicati.models.user.Profile;
import com.syndicati.models.user.User;
import com.syndicati.services.user.OnboardingService;
import com.syndicati.services.user.ProfileService;
import com.syndicati.services.user.UserService;

import java.util.List;
import java.util.Optional;

/**
 * Backend users controller for Java-side CRUD orchestration.
 *
 * This mirrors the web naming and action style:
 * - list users
 * - add user
 * - edit user
 * - delete user
 */
public class UserController {

    private final UserService userService;
    private final ProfileService profileService;
    private final OnboardingService onboardingService;

    public UserController() {
        this.userService = new UserService();
        this.profileService = new ProfileService();
        this.onboardingService = new OnboardingService();
    }

    public List<User> users() {
        return userService.listUsers();
    }

    public Optional<User> userById(int idUser) {
        return userService.findById(idUser);
    }

    public Optional<User> userByEmail(String emailUser) {
        return userService.findByEmail(emailUser);
    }

    public int userAdd(User user) {
        return userService.createUser(user);
    }

    public boolean userEdit(User user) {
        return userService.updateUser(user);
    }

    public boolean userDelete(int idUser) {
        return userService.deleteUser(idUser);
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

    public boolean profileEdit(Profile profile) {
        return profileService.updateProfile(profile);
    }

    public Optional<Integer> profileCreate(Profile profile) {
        return profileService.createProfile(profile);
    }

    public boolean profileUpdate(Profile profile) {
        return profileService.updateProfile(profile);
    }

    public boolean profileDelete(int idProfile) {
        return profileService.deleteProfile(idProfile);
    }

    public List<Onboarding> onboardings() {
        return onboardingService.listOnboardings();
    }

    public Optional<Onboarding> onboardingById(int idOnboarding) {
        return onboardingService.findById(idOnboarding);
    }

    public Optional<Onboarding> onboardingByUserId(int userId) {
        return onboardingService.findOneByUserId(userId);
    }

    public boolean onboardingEdit(Onboarding onboarding) {
        return onboardingService.updateOnboarding(onboarding);
    }
}
