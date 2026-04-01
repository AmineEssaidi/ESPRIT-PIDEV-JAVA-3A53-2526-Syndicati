package com.syndicati.controllers.frontend.auth;

import com.syndicati.models.entities.User;
import com.syndicati.models.services.AuthCodeService;
import com.syndicati.models.services.UserService;
import com.syndicati.services.security.LoginRateLimiter;
import com.syndicati.services.security.TOTPService;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
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
        String cleanEmail = safeTrim(email).toLowerCase();
        
        if (cleanEmail.isEmpty() || rawPassword == null || rawPassword.isBlank()) {
            return AuthResult.failure("Email and password are required.");
        }

        // Check rate limiting (brute force protection)
        if (LoginRateLimiter.isRateLimited(cleanEmail)) {
            long secondsRemaining = LoginRateLimiter.getLockoutTimeRemaining(cleanEmail);
            return AuthResult.failure(
                String.format("Too many failed login attempts. Please try again in %d seconds.", secondsRemaining)
            );
        }

        Optional<User> userOpt = userService.findByEmail(cleanEmail);
        if (userOpt.isEmpty()) {
            LoginRateLimiter.recordFailedAttempt(cleanEmail);
            return AuthResult.failure("Invalid email or password.");
        }

        User user = userOpt.get();
        
        // Check if disabled
        if (user.isDisabled()) {
            LoginRateLimiter.recordFailedAttempt(cleanEmail);
            return AuthResult.failure(
                String.format("This account is disabled. Reason: %s", user.getDisabledReason())
            );
        }

        // Check if verified (admin approval required)
        if (!user.isVerified()) {
            LoginRateLimiter.recordFailedAttempt(cleanEmail);
            return AuthResult.failure("Your account is not yet verified by an administrator. You cannot log in until an admin verifies your account.");
        }

        // Verify password
        if (!matchesPassword(rawPassword, user.getPasswordUser())) {
            int remaining = LoginRateLimiter.getRemainingAttempts(cleanEmail);
            LoginRateLimiter.recordFailedAttempt(cleanEmail);
            
            return AuthResult.failure(
                remaining <= 0 ?
                "Invalid email or password. Your account is temporarily locked." :
                String.format("Invalid email or password. %d attempts remaining.", remaining - 1)
            );
        }

        // Clear rate limit on successful login
        LoginRateLimiter.clearAttempts(cleanEmail);

        // Check if 2FA is enabled
        if (user.isTwoFactorEnabled()) {
            // Don't grant full access yet, user must verify 2FA code
            return AuthResult.mfaRequired(user, "2FA code required.");
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

    /**
     * Verify TOTP code (from authenticator app)
     * @param user User attempting authentication
     * @param code 6-digit code from authenticator app
     * @return AuthResult with MFA verification status
     */
    public AuthResult verifyTOTPCode(User user, String code) {
        if (user == null || !user.isTwoFactorEnabled()) {
            return AuthResult.failure("TOTP is not enabled for this account.");
        }

        if (user.getTotpSecret() == null || user.getTotpSecret().isBlank()) {
            return AuthResult.failure("TOTP secret is not configured.");
        }

        if (!TOTPService.verifyCode(user.getTotpSecret(), code)) {
            return AuthResult.failure("Invalid or expired TOTP code.");
        }

        return AuthResult.success(user, "2FA verification successful.");
    }

    /**
     * Setup TOTP for user (returns secret and QR URI)
     * @param user User setting up TOTP
     * @return TOTP secret and OTPAuth URI
     */
    public TOTPSetupResult setupTOTP(User user) {
        String secret = TOTPService.generateSecret();
        String otpUri = TOTPService.generateOTPAuthURI(user.getEmailUser(), secret);
        return new TOTPSetupResult(secret, otpUri);
    }

    /**
     * Enable TOTP for user
     * @param user User enabling TOTP
     * @param secret TOTP secret to store
     * @return AuthResult
     */
    public AuthResult enableTOTP(User user, String secret) {
        if (secret == null || secret.isBlank()) {
            return AuthResult.failure("TOTP secret is required.");
        }

        user.setTotpSecret(secret);
        user.setTwoFactorEnabled(true);
        
        boolean updated = userService.updateUser(user);
        return updated ?
            AuthResult.success(user, "TOTP enabled successfully.") :
            AuthResult.failure("Failed to enable TOTP.");
    }

    /**
     * Disable TOTP for user
     * @param user User disabling TOTP
     * @return AuthResult
     */
    public AuthResult disableTOTP(User user) {
        user.setTotpSecret(null);
        user.setTwoFactorEnabled(false);
        
        boolean updated = userService.updateUser(user);
        return updated ?
            AuthResult.success(user, "TOTP disabled successfully.") :
            AuthResult.failure("Failed to disable TOTP.");
    }

    /**
     * Setup 2FA with Email OTP
     * @param user User setting up email 2FA
     * @return AuthResult with sent status
     */
    public AuthResult enableEmailTwoFactor(User user) {
        user.setTwoFactorEnabled(true);
        
        boolean updated = userService.updateUser(user);
        if (!updated) {
            return AuthResult.failure("Failed to enable 2FA.");
        }

        // Send test code
        AuthCodeService.AuthFlowResult result = authCodeService.requestLoginCode(user.getEmailUser());
        return result.isSuccess() ?
            AuthResult.success(user, "2FA enabled. Verify the code sent to your email.") :
            AuthResult.failure(result.getMessage());
    }

    /**
     * Disable 2FA
     * @param user User disabling 2FA
     * @return AuthResult
     */
    public AuthResult disableTwoFactor(User user) {
        user.setTwoFactorEnabled(false);
        user.setTotpSecret(null);
        
        boolean updated = userService.updateUser(user);
        return updated ?
            AuthResult.success(user, "2FA disabled successfully.") :
            AuthResult.failure("Failed to disable 2FA.");
    }

    /**
     * TOTP setup response
     */
    public static class TOTPSetupResult {
        private final String secret;
        private final String otpUri;

        public TOTPSetupResult(String secret, String otpUri) {
            this.secret = secret;
            this.otpUri = otpUri;
        }

        public String getSecret() {
            return secret;
        }

        public String getOtpUri() {
            return otpUri;
        }
    }

    public static class AuthResult {
        private final boolean success;
        private final String message;
        private final User user;
        private final boolean mfaRequired;

        private AuthResult(boolean success, String message, User user, boolean mfaRequired) {
            this.success = success;
            this.message = message;
            this.user = user;
            this.mfaRequired = mfaRequired;
        }

        public static AuthResult success(User user, String message) {
            return new AuthResult(true, message, user, false);
        }

        public static AuthResult failure(String message) {
            return new AuthResult(false, message, null, false);
        }

        public static AuthResult mfaRequired(User user, String message) {
            return new AuthResult(true, message, user, true);
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

        public boolean isMfaRequired() {
            return mfaRequired;
        }
    }
}
