package com.syndicati.controllers.frontend.auth;

import com.syndicati.models.entities.User;
import com.syndicati.models.services.AuthCodeService;
import com.syndicati.models.services.UserService;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Frontend auth controller that provides functional login/signup behavior.
 */
public class AuthController {

    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-ZÀ-ÿ\\s-]+$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern SPECIAL_PATTERN = Pattern.compile(".*[!@#$%^&*(),.?\":{}|<>].*");

    private final UserService userService;
    private final AuthCodeService authCodeService;

    public AuthController() {
        this.userService = new UserService();
        this.authCodeService = new AuthCodeService();
    }

    public AuthResult login(String email, String rawPassword) {
        if (email == null || email.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            return AuthResult.failure("Email and password are required.");
        }

        Optional<User> userOpt = userService.findByEmail(email.trim());
        if (userOpt.isEmpty()) {
            return AuthResult.failure("No account found for this email.");
        }

        User user = userOpt.get();
        if (user.isDisabled()) {
            return AuthResult.failure("This account is disabled.");
        }

        if (!matchesPassword(rawPassword, user.getPasswordUser())) {
            return AuthResult.failure("Invalid credentials.");
        }

        return AuthResult.success(user, "Login successful.");
    }

    public AuthResult signUp(String firstName, String lastName, String email, String rawPassword, String confirmPassword) {
        String cleanFirst = safeTrim(firstName);
        String cleanLast = safeTrim(lastName);
        String cleanEmail = safeTrim(email).toLowerCase();

        if (cleanFirst.isEmpty() || cleanLast.isEmpty() || cleanEmail.isEmpty()) {
            return AuthResult.failure("First name, last name and email are required.");
        }

        if (!NAME_PATTERN.matcher(cleanFirst).matches() || !NAME_PATTERN.matcher(cleanLast).matches()) {
            return AuthResult.failure("Names can only contain letters, spaces and hyphens.");
        }

        if (!EMAIL_PATTERN.matcher(cleanEmail).matches()) {
            return AuthResult.failure("Please enter a valid email address.");
        }

        if (rawPassword == null || confirmPassword == null || rawPassword.isBlank() || confirmPassword.isBlank()) {
            return AuthResult.failure("Password and confirmation are required.");
        }

        if (!rawPassword.equals(confirmPassword)) {
            return AuthResult.failure("Password confirmation does not match.");
        }

        if (rawPassword.length() < 8) {
            return AuthResult.failure("Password must be at least 8 characters.");
        }

        if (!UPPERCASE_PATTERN.matcher(rawPassword).matches()) {
            return AuthResult.failure("Password must include at least one uppercase letter.");
        }

        if (!SPECIAL_PATTERN.matcher(rawPassword).matches()) {
            return AuthResult.failure("Password must include at least one special character.");
        }

        if (userService.findByEmail(cleanEmail).isPresent()) {
            return AuthResult.failure("This email is already registered.");
        }

        User user = new User();
        user.setFirstName(cleanFirst);
        user.setLastName(cleanLast);
        user.setEmailUser(cleanEmail);
        user.setPasswordUser(rawPassword);
        user.setRoleUser("RESIDENT");
        user.setVerified(false);
        user.setDisabled(false);

        int newId = userService.createUser(user);
        if (newId <= 0) {
            return AuthResult.failure("Failed to create account. Please try again.");
        }

        user.setIdUser(newId);
        return AuthResult.success(user, "Account created successfully. You can now sign in.");
    }

    private boolean matchesPassword(String raw, String stored) {
        if (stored == null || stored.isBlank()) {
            return false;
        }

        try {
            if (stored.startsWith("$2y$")) {
                return BCrypt.checkpw(raw, "$2a$" + stored.substring(4));
            }
            if (stored.startsWith("$2a$") || stored.startsWith("$2b$")) {
                return BCrypt.checkpw(raw, stored);
            }
        } catch (IllegalArgumentException ignored) {
            return false;
        }

        return raw.equals(stored);
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    public AuthResult requestPasswordReset(String recovery) {
        AuthCodeService.AuthFlowResult result = authCodeService.requestReset(recovery);
        return result.isSuccess()
            ? AuthResult.success(result.getUser(), result.getMessage())
            : AuthResult.failure(result.getMessage());
    }

    public AuthResult verifyPasswordReset(String recovery, String code) {
        AuthCodeService.AuthFlowResult result = authCodeService.verifyReset(recovery, code);
        return result.isSuccess()
            ? AuthResult.success(result.getUser(), result.getMessage())
            : AuthResult.failure(result.getMessage());
    }

    public AuthResult requestLoginOtp(String email) {
        AuthCodeService.AuthFlowResult result = authCodeService.requestLoginCode(email);
        return result.isSuccess()
            ? AuthResult.success(result.getUser(), result.getMessage())
            : AuthResult.failure(result.getMessage());
    }

    public AuthResult verifyLoginOtp(String email, String code) {
        AuthCodeService.AuthFlowResult result = authCodeService.verifyLoginCode(email, code);
        return result.isSuccess()
            ? AuthResult.success(result.getUser(), result.getMessage())
            : AuthResult.failure(result.getMessage());
    }

    public static class AuthResult {
        private final boolean success;
        private final String message;
        private final User user;

        private AuthResult(boolean success, String message, User user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }

        public static AuthResult success(User user, String message) {
            return new AuthResult(true, message, user);
        }

        public static AuthResult failure(String message) {
            return new AuthResult(false, message, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public User getUser() {
            return user;
        }
    }
}
