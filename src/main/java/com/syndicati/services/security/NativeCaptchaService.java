package com.syndicati.services.security;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelFormat;
import io.github.humbleui.skija.*;
import io.github.humbleui.types.*;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Modern native captcha service using Skija: generates a short code and runs behavioral heuristics.
 * Renders high-quality distorted images using Google's Skia engine.
 */
public class NativeCaptchaService implements AutoCloseable {

    private static final float PASS_THRESHOLD = 0.56f;

    private final Random random = new Random();
    private final ConcurrentMap<String, SessionState> sessions = new ConcurrentHashMap<>();
    private Typeface typeface;

    public NativeCaptchaService() {
        try {
            // Load a system font; fallback to default if unavailable
            typeface = Typeface.makeFromName("Arial", FontStyle.BOLD);
        } catch (Exception e) {
            typeface = Typeface.makeDefault();
        }
    }

    public NativeChallenge beginChallenge() {
        String id = randomToken(16);
        String code = buildCode();
        Image image = renderChallengeImageSkija(code);
        SessionState state = new SessionState(id, "Type the code shown in the image above.", normalize(code), "image-distorted", Instant.now());
        sessions.put(id, state);
        return new NativeChallenge(id, state.prompt, state.mode, state.difficultyLabel, image);
    }

    public void noteFocus(String challengeId, String fieldName) {
        SessionState s = sessions.get(challengeId);
        if (s != null) {
            s.focusChanges++;
            s.focusSequence.add(fieldName == null ? "unknown" : fieldName);
            s.lastFocusAt = Instant.now();
        }
    }

    public void noteMouse(String challengeId, double x, double y) {
        SessionState s = sessions.get(challengeId);
        if (s != null) {
            s.mouseMoves++;
            s.lastMouseAt = Instant.now();
        }
    }

    public void noteKey(String challengeId, String fieldName, String currentText, boolean pasteLike) {
        SessionState s = sessions.get(challengeId);
        if (s != null) {
            s.keyEvents++;
            if (pasteLike) s.pasteEvents++;
            if (currentText != null) s.lastText = currentText;
            s.lastTypingAt = Instant.now();
        }
    }

    public NativeCaptchaVerdict verify(String challengeId, String answer) {
        SessionState s = sessions.get(challengeId);
        if (s == null) return NativeCaptchaVerdict.fail(1.0f, "session expired");
        s.answeredAt = Instant.now();
        String normalized = normalize(answer);
        boolean exact = Objects.equals(normalized, s.expectedAnswer);
        float score = calculateHeuristicScore(s, normalized, exact);
        boolean passed = exact && score <= PASS_THRESHOLD;
        if (passed) sessions.remove(challengeId);
        else {
            s.attempts++;
            if (s.attempts >= 3) sessions.remove(challengeId);
        }
        return new NativeCaptchaVerdict(passed, score, passed ? "passed" : "failed", false, s.mode, s.difficultyLabel);
    }

    public boolean checkAnswerMatch(String challengeId, String answer) {
        SessionState s = sessions.get(challengeId);
        if (s == null) return false;
        String normalized = normalize(answer);
        return Objects.equals(normalized, s.expectedAnswer);
    }

    public String getModelStatus() { return "skija-powered image captcha"; }

    public void reset(String id) { if (id != null) sessions.remove(id); }

    @Override
    public void close() {
        sessions.clear();
        if (typeface != null) typeface.close();
    }

    private String buildCode() {
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            if (i == 3) sb.append('-');
            sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return sb.toString();
    }

    private String normalize(String v) { return v == null ? "" : v.replaceAll("\\s+", "").replace("-", "").toUpperCase(Locale.ROOT); }

    private float calculateHeuristicScore(SessionState s, String normalized, boolean exact) {
        long ms = s.answeredAt == null ? 0 : Duration.between(s.startedAt, s.answeredAt).toMillis();
        float score = 0f;
        if (!exact) score += 0.65f;
        if (s.pasteEvents > 0) score += 0.22f;
        if (s.keyEvents <= 1) score += 0.18f;
        if (s.mouseMoves == 0 && ms < 1500) score += 0.18f;
        if (ms < 650) score += 0.22f; else if (ms > 90000) score += 0.12f;
        if (normalized.isBlank()) score += 0.3f;
        return Math.max(0f, Math.min(1f, score));
    }

    private String randomToken(int len) { String a = "abcdefghijklmnopqrstuvwxyz0123456789"; StringBuilder b = new StringBuilder(len); for (int i=0;i<len;i++) b.append(a.charAt(random.nextInt(a.length()))); return b.toString(); }

    private Image renderChallengeImageSkija(String code) {
        int width = 360, height = 140;
        
        try (Surface surface = Surface.makeRaster(ImageInfo.makeS32(width, height, ColorAlphaType.OPAQUE))) {
            Canvas canvas = surface.getCanvas();
            
            // Draw gradient background
            Paint bgPaint = new Paint().setColor(0xFF0C1220).setAntiAlias(true);
            canvas.drawRect(Rect.makeWH(width, height), bgPaint);
            
            // Add subtle gradient overlay
            try (Paint gradientPaint = new Paint()) {
                gradientPaint.setShader(Shader.makeLinearGradient(
                    0, 0, width, height,
                    new int[]{0xFF161C2D, 0xFF0C1220}
                ));
                gradientPaint.setAntiAlias(true);
                canvas.drawRect(Rect.makeWH(width, height), gradientPaint);
            }
            
            // Draw noise
            drawNoiseSkija(canvas, width, height);
            
            // Draw code characters
            drawCodeCharsSkija(canvas, code, width, height);
            
            // Draw border
            try (Paint borderPaint = new Paint()
                .setColor(0x15FFFFFF)
                .setStrokeWidth(1)
                .setMode(PaintMode.STROKE)) {
                canvas.drawRRect(RRect.makeXYWH(16, 16, width - 32, height - 32, 24), borderPaint);
            }
            
            // Convert to JavaFX Image
            try (Bitmap bitmap = new Bitmap()) {
                bitmap.allocPixels(ImageInfo.makeS32(width, height, ColorAlphaType.OPAQUE));
                surface.readPixels(bitmap, 0, 0);
                ByteBuffer buffer = bitmap.peekPixels();
                byte[] pixelData = new byte[buffer.remaining()];
                buffer.get(pixelData);
                return pixelsToFXImage(pixelData, width, height);
            }
        } catch (Exception e) {
            // Fallback to simple rendering if Skija fails
            System.err.println("[NativeCaptchaService] Skija rendering failed, using fallback: " + e.getMessage());
            return renderChallengeImageFallback(code);
        }
    }

    private void drawNoiseSkija(Canvas canvas, int w, int h) {
        try (Paint noisePaint = new Paint().setAntiAlias(true)) {
            // Draw noise lines
            for (int i = 0; i < 10; i++) {
                noisePaint.setColor((0x12 + random.nextInt(0x1C)) << 24 | 0xFFFFFF);
                noisePaint.setStrokeWidth(2.0f);
                canvas.drawLine(random.nextInt(w), random.nextInt(h), random.nextInt(w), random.nextInt(h), noisePaint);
            }
            
            // Draw noise dots
            for (int i = 0; i < 42; i++) {
                noisePaint.setColor((0x0C + random.nextInt(0x2C)) << 24 | 0xFFFFFF);
                int s = 2 + random.nextInt(5);
                canvas.drawCircle(random.nextInt(w), random.nextInt(h), s, noisePaint);
            }
        }
    }

    private void drawCodeCharsSkija(Canvas canvas, String code, int w, int h) {
        int baseline = 90, x = 36;
        
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            try (Paint charPaint = new Paint().setAntiAlias(true)) {
                if (c == '-') {
                    charPaint.setColor(0xDCFFFFFF);
                    try (Font font = new Font(typeface, 32)) {
                        canvas.drawString("-", x + 10, baseline - 4 + random.nextInt(8), font, charPaint);
                    }
                    x += 28;
                    continue;
                }
                
                int fs = 34 + random.nextInt(10);
                charPaint.setColor(0xE6F0F0FF - (random.nextInt(0x28) << 16));
                
                try (Font font = new Font(typeface, fs)) {
                    canvas.save();
                    float cx = x + 18.0f;
                    float cy = baseline - 18.0f;
                    canvas.translate(cx, cy);
                    canvas.rotate((float) (-18 + random.nextInt(37)));
                    canvas.translate(-cx, -cy);
                    canvas.drawString(String.valueOf(c), x, baseline + random.nextInt(18) - 6, font, charPaint);
                    canvas.restore();
                }
                
                x += 38 + random.nextInt(8);
            }
        }
    }

    private Image pixelsToFXImage(byte[] pixelData, int width, int height) {
        WritableImage img = new WritableImage(width, height);
        PixelWriter pw = img.getPixelWriter();
        // HumbleUI Skija S32 is BGRA on Windows
        pw.setPixels(0, 0, width, height, PixelFormat.getByteBgraInstance(), pixelData, 0, width * 4);
        return img;
    }

    private Image renderChallengeImageFallback(String code) {
        // Very basic fallback if Skija is totally broken
        int width = 360, height = 140;
        WritableImage img = new WritableImage(width, height);
        PixelWriter pw = img.getPixelWriter();
        
        // Fill dark background
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pw.setArgb(x, y, 0xFF0C1220);
            }
        }
        return img;
    }

    public record NativeChallenge(String challengeId, String prompt, String mode, String difficultyLabel, Image captchaImage) {}

    public record NativeCaptchaVerdict(boolean passed, float score, String message, boolean onnxUsed, String challengeMode, String difficultyLabel) {
        public static NativeCaptchaVerdict fail(float score, String message) { return new NativeCaptchaVerdict(false, score, message, false, "image", "unknown"); }
    }

    private static final class SessionState {
        final String challengeId; final String prompt; final String expectedAnswer; final String difficultyLabel; final String mode = "image"; final Instant startedAt;
        final List<String> focusSequence = new ArrayList<>(); Instant answeredAt; Instant lastFocusAt; Instant lastTypingAt; Instant lastMouseAt; int focusChanges; int mouseMoves; int keyEvents; int pasteEvents; int attempts; int maxObservedLength; double mouseDistance; String lastText = "";
        SessionState(String challengeId, String prompt, String expectedAnswer, String difficultyLabel, Instant startedAt) { this.challengeId = challengeId; this.prompt = prompt; this.expectedAnswer = expectedAnswer; this.difficultyLabel = difficultyLabel; this.startedAt = startedAt; }
    }

}
