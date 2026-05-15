package com.syndicati.utils.session;

import com.syndicati.models.user.User;
import com.syndicati.models.user.Profile;
import com.syndicati.models.user.UserRelationship;
import com.syndicati.models.user.UserStanding;
import com.syndicati.controllers.user.standing.UserStandingController;

import java.util.List;

/**
 * Session manager to track the currently logged-in user.
 * Caches expensive relationship data to avoid redundant DB round-trips.
 */
public class SessionManager {
    private static SessionManager instance;

    // â”€â”€ Core session â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private User currentUser;
    private Profile currentProfile;
    private UserStanding currentStanding;
    private long lastXpAwardAt;

    /** Timestamp (ms) when profile was last fetched from DB. */
    private long profileFetchedAt = 0L;
    /** Timestamp (ms) when standing was last fetched from DB. */
    private long standingFetchedAt = 0L;

    // â”€â”€ Circle / relationship cache â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    /** How long (ms) cached circle data stays valid before a refresh. */
    private static final long CIRCLE_TTL_MS = 60_000L; // 60 seconds

    private List<User> cachedFriends;
    private List<UserRelationship> cachedPendingRequests;
    private int cachedFriendCount = -1;
    private int cachedPendingCount = -1;
    private long circleFetchedAt = 0L;

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    // â”€â”€ User â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null && user.getIdUser() != null) {
            com.syndicati.utils.shared.AppPreferences.setLocalOnly("LOCAL_LOGGED_IN_USER_ID", String.valueOf(user.getIdUser()));
        }
    }
    public User getCurrentUser() { return currentUser; }

    // â”€â”€ Profile â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    public void setCurrentProfile(Profile profile) {
        this.currentProfile = profile;
        this.profileFetchedAt = System.currentTimeMillis();
        if (profile != null) {
            com.syndicati.utils.shared.AppPreferences.syncFromProfile(profile);
        }
    }

    public Profile getCurrentProfile() { return currentProfile; }

    /**
     * Returns true if the profile state is fresh (explicitly fetched/checked within the last 60s).
     */
    public boolean isProfileFresh() {
        return profileFetchedAt > 0
            && (System.currentTimeMillis() - profileFetchedAt) < CIRCLE_TTL_MS;
    }

    // â”€â”€ Standing â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    public void setCurrentStanding(UserStanding standing) {
        this.currentStanding = standing;
        this.standingFetchedAt = System.currentTimeMillis();
    }

    public UserStanding getCurrentStanding() { return currentStanding; }

    /** Returns true if the standing state is fresh (explicitly fetched/checked within the last 60s). */
    public boolean isStandingFresh() {
        return standingFetchedAt > 0
            && (System.currentTimeMillis() - standingFetchedAt) < CIRCLE_TTL_MS;
    }

    // â”€â”€ Circle / relationship cache â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /** Returns true if the circle cache is still valid (within TTL). */
    public boolean isCircleCacheFresh() {
        return circleFetchedAt > 0
            && (System.currentTimeMillis() - circleFetchedAt) < CIRCLE_TTL_MS;
    }

    public synchronized void setCircleData(
            List<User> friends,
            List<UserRelationship> pendingRequests,
            int friendCount,
            int pendingCount) {
        this.cachedFriends = friends;
        this.cachedPendingRequests = pendingRequests;
        this.cachedFriendCount = friendCount;
        this.cachedPendingCount = pendingCount;
        this.circleFetchedAt = System.currentTimeMillis();
    }

    public List<User> getCachedFriends() { return cachedFriends; }
    public List<UserRelationship> getCachedPendingRequests() { return cachedPendingRequests; }
    public int getCachedFriendCount() { return cachedFriendCount; }
    public int getCachedPendingCount() { return cachedPendingCount; }

    /** Invalidates circle cache â€” call after accepting/rejecting a friend request. */
    public void invalidateCircleCache() {
        circleFetchedAt = 0L;
        cachedFriends = null;
        cachedPendingRequests = null;
        cachedFriendCount = -1;
        cachedPendingCount = -1;
    }

    // â”€â”€ XP â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    public synchronized UserStanding awardXp(int xpDelta) {
        if (currentUser == null || currentUser.getIdUser() == null || currentUser.getIdUser() <= 0)
            return currentStanding;
        int safeDelta = Math.max(0, xpDelta);
        if (safeDelta <= 0) return currentStanding;
        long now = System.currentTimeMillis();
        if (now - lastXpAwardAt < 120) return currentStanding;
        lastXpAwardAt = now;
        try {
            UserStandingController controller = new UserStandingController();
            currentStanding = controller.addExperience(currentUser.getIdUser(), safeDelta);
            standingFetchedAt = System.currentTimeMillis();
        } catch (Throwable ex) {
            // XP is nice-to-have; never let a missing packaged standing class break UI clicks.
            if (currentStanding == null) {
                currentStanding = new UserStanding();
                currentStanding.setUserId(currentUser.getIdUser());
                currentStanding.setLevel(1);
                currentStanding.setPoints(0);
                currentStanding.setStandingLabel("NORMAL");
            }
        }
        return currentStanding;
    }

    // â”€â”€ Session lifecycle â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    public void clear() {
        currentUser = null;
        currentProfile = null;
        currentStanding = null;
        lastXpAwardAt = 0L;
        profileFetchedAt = 0L;
        standingFetchedAt = 0L;
        invalidateCircleCache();
        com.syndicati.utils.shared.AppPreferences.removeLocal("LOCAL_LOGGED_IN_USER_ID");
    }

    public boolean isLoggedIn() { return currentUser != null; }

    public String getCurrentUserEmail() {
        return currentUser != null ? currentUser.getEmailUser() : null;
    }

    public String getCurrentUserName() {
        if (currentUser == null) return null;
        String firstName = currentUser.getFirstName() != null ? currentUser.getFirstName() : "";
        String lastName  = currentUser.getLastName()  != null ? currentUser.getLastName()  : "";
        String fullName  = (firstName + " " + lastName).trim();
        return fullName.isEmpty() ? null : fullName;
    }
}
