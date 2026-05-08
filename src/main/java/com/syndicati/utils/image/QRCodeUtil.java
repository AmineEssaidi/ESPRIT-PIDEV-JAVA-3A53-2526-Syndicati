package com.syndicati.utils.image;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility for generating QR Codes using ZXing.
 */
public final class QRCodeUtil {

    private QRCodeUtil() {}

    /**
     * Generates a QR Code as a JavaFX Image.
     *
     * @param text   The content to encode.
     * @param width  Desired width in pixels.
     * @param height Desired height in pixels.
     * @return A JavaFX Image containing the QR code, or null if generation fails.
     */
    public static Image generateQRCode(String text, int width, int height) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix matrix = new MultiFormatWriter().encode(
                text, BarcodeFormat.QR_CODE, width, height, hints
            );

            WritableImage image = new WritableImage(width, height);
            PixelWriter pw = image.getPixelWriter();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    pw.setColor(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            return image;
        } catch (Exception e) {
            System.err.println("QR Code generation failed: " + e.getMessage());
            return null;
        }
    }
}
