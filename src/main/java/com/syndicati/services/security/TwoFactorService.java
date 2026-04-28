package com.syndicati.services.security;

import com.syndicati.models.user.User;
import com.syndicati.models.user.data.UserRepository;
import com.syndicati.utils.session.SessionManager;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base32;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import java.util.HashMap;
import java.util.Map;

/**
 * Two-Factor Authentication Service
 * Handles TOTP (Time-based One-Time Password) setup and verification
 */
public class TwoFactorService {
    private static final String HMAC_ALGORITHM = "HmacSHA1";
    private static final int DIGITS = 6;
    private static final int WINDOW = 1; // Allow ±30 seconds
    
    private final UserRepository userRepository;
    
    public TwoFactorService() {
        this.userRepository = new UserRepository();
    }
    
    /**
     * Generate a new TOTP secret for the user
     */
    public String generateTotpSecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        Base32 base32 = new Base32();
        return base32.encodeToString(bytes).replace("=", "");
    }
    
    /**
     * Generate TOTP QR code data URL
     */
    public String generateQRCodeDataUrl(String secret, String email, String issuer) {
        try {
            String label = issuer + ":" + email;
            String otpAuthUrl = String.format("otpauth://totp/%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30", 
                label, secret, issuer);
            return generatePngDataUrlFromText(otpAuthUrl, 300, 300);
        } catch (Exception e) {
            System.err.println("Error generating QR code: " + e.getMessage());
            return null;
        }
    }
    private String generatePngDataUrlFromText(String data, int width, int height) throws WriterException {
        try {
            QRCodeWriter qrWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.MARGIN, 1);
            BitMatrix matrix = qrWriter.encode(data, BarcodeFormat.QR_CODE, width, height, hints);
            MatrixToImageConfig config = new MatrixToImageConfig(MatrixToImageConfig.BLACK, MatrixToImageConfig.WHITE);
            BufferedImage img = MatrixToImageWriter.toBufferedImage(matrix, config);

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                ImageIO.write(img, "png", baos);
                String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
                return "data:image/png;base64," + base64;
            }
        } catch (WriterException we) {
            throw we;
        } catch (Exception e) {
            System.err.println("QR generation failed: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Verify a TOTP code
     */
    public boolean verifyTotpCode(String secret, String code) {
        try {
            long timeWindow = System.currentTimeMillis() / 1000 / 30;
            
            for (int i = -WINDOW; i <= WINDOW; i++) {
                String generatedCode = generateTotpCode(secret, timeWindow + i);
                if (generatedCode.equals(code)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error verifying TOTP: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Generate TOTP code for a specific time window
     */
    private String generateTotpCode(String secret, long timeWindow) throws Exception {
        Base32 base32 = new Base32();
        byte[] decodedSecret = base32.decode(secret);
        byte[] timeBytes = new byte[8];
        
        for (int i = 7; i >= 0; i--) {
            timeBytes[i] = (byte) (timeWindow & 0xff);
            timeWindow >>= 8;
        }
        
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(decodedSecret, 0, decodedSecret.length, HMAC_ALGORITHM));
        byte[] hash = mac.doFinal(timeBytes);
        
        int offset = hash[hash.length - 1] & 0xf;
        int truncated = 0;
        
        for (int i = 0; i < 4; i++) {
            truncated = (truncated << 8) | (hash[offset + i] & 0xff);
        }
        
        truncated = (truncated & 0x7fffffff) % (int) Math.pow(10, DIGITS);
        return String.format("%0" + DIGITS + "d", truncated);
    }
    
    /**
     * Enable TOTP for current user
     */
    public boolean enableTotpForUser(String totpSecret) {
        try {
            User user = SessionManager.getInstance().getCurrentUser();
            if (user == null || user.getIdUser() == null) {
                return false;
            }
            
            user.setTotpSecret(totpSecret);
            user.setTwoFactorEnabled(true);
            userRepository.update(user);
            
            // Update session
            SessionManager.getInstance().setCurrentUser(user);
            return true;
        } catch (Exception e) {
            System.err.println("Error enabling TOTP: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Disable TOTP for current user
     */
    public boolean disableTotpForUser() {
        try {
            User user = SessionManager.getInstance().getCurrentUser();
            if (user == null || user.getIdUser() == null) {
                return false;
            }
            
            user.setTotpSecret(null);
            user.setTwoFactorEnabled(false);
            userRepository.update(user);
            
            // Update session
            SessionManager.getInstance().setCurrentUser(user);
            return true;
        } catch (Exception e) {
            System.err.println("Error disabling TOTP: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if TOTP is configured for current user
     */
    public boolean isTotpConfigured() {
        User user = SessionManager.getInstance().getCurrentUser();
        return user != null && user.getTotpSecret() != null && !user.getTotpSecret().isBlank();
    }
    
    /**
     * Get current TOTP status
     */
    public String getTotpStatus() {
        return isTotpConfigured() ? "Configured" : "Not Configured";
    }
}
