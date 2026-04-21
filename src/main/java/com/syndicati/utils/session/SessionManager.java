package com.syndicati.utils.session;

import com.syndicati.models.user.User;
import com.syndicati.models.user.Profile;

/**
 * Session manager to track the currently logged-in user.
 * Singleton pattern for global access.
 */
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    private Profile currentProfile;

    private SessionManager() {
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentProfile(Profile profile) {
        this.currentProfile = profile;
    }

    public Profile getCurrentProfile() {
        return currentProfile;
    }

    public void clear() {
        currentUser = null;
        currentProfile = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public String getCurrentUserEmail() {
        return currentUser != null ? currentUser.getEmailUser() : null;
    }

    public String getCurrentUserName() {
        if (currentUser == null) return null;
        String firstName = currentUser.getFirstName() != null ? currentUser.getFirstName() : "";
        String lastName = currentUser.getLastName() != null ? currentUser.getLastName() : "";
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isEmpty() ? null : fullName;
    }
}
