package com.syndicati.utils.image.imagekit;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Basic server-side ImageKit integration.
 *
 * Notes:
 * - Auth uses HTTP Basic with your PRIVATE key as username (no password).
 * - No ImageKit client-side signature/token is needed for this server-side API.
 * - This service does not modify DB; it only uploads and returns the resulting URL + fileId.
 */
public class ImageKitStorageService {

    private static final String UPLOAD_URL = "https://upload.imagekit.io/api/v1/files/upload";

    private final ImageKitConfig config;
    private final HttpClient httpClient;

    public ImageKitStorageService(ImageKitConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();
    }

    public static boolean isConfigured() {
        return ImageKitConfig.fromEnv().isEnabled();
    }

    public ImageKitUploadResult uploadFile(File file, String folder) throws IOException, InterruptedException {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new IOException("ImageKit upload: file is missing: " + (file == null ? "null" : file.getAbsolutePath()));
        }
        if (config == null || !config.isEnabled() || config.getPrivateKey() == null) {
            throw new IllegalStateException("ImageKit upload is not configured. Set IMAGEKIT_PRIVATE_KEY and (optionally) IMAGEKIT_ENABLED=true.");
        }

        String fileName = file.getName();
        String mimeType = detectMimeType(file);

        String boundary = "ImageKitBoundary-" + UUID.randomUUID();
        byte[] body = createMultipartBody(boundary, fileName, folder, mimeType, file);

        String authHeader = basicAuthHeader(config.getPrivateKey());

        HttpRequest request = HttpRequest.newBuilder()
            .uri(java.net.URI.create(UPLOAD_URL))
            .header("Authorization", authHeader)
            .header("Content-Type", "multipart/form-data; boundary=" + boundary)
            .timeout(Duration.ofMinutes(2))
            .POST(HttpRequest.BodyPublishers.ofByteArray(body))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        int code = response.statusCode();
        String respBody = response.body();
        if (code < 200 || code >= 300) {
            String msg = respBody;
            if (msg != null && msg.length() > 600) msg = msg.substring(0, 600) + "...";
            throw new IOException("ImageKit upload failed (HTTP " + code + "): " + msg);
        }

        JSONObject json = new JSONObject(respBody);
        String fileId = json.optString("fileId", null);
        String name = json.optString("name", null);
        String url = json.optString("url", null);
        String filePath = null;
        JSONObject versionInfo = json.optJSONObject("versionInfo");
        if (versionInfo != null) filePath = versionInfo.optString("filePath", null);

        return new ImageKitUploadResult(fileId, url, name, filePath);
    }

    private static String basicAuthHeader(String privateKey) {
        // Basic auth username = private key, password = empty string.
        String raw = privateKey + ":";
        String encoded = java.util.Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }

    private static String detectMimeType(File file) {
        try {
            String t = Files.probeContentType(file.toPath());
            return (t == null || t.isBlank()) ? "application/octet-stream" : t;
        } catch (Exception e) {
            return "application/octet-stream";
        }
    }

    private static byte[] createMultipartBody(
        String boundary,
        String fileName,
        String folder,
        String mimeType,
        File file
    ) throws IOException {
        String separator = "--" + boundary + "\r\n";
        String end = "--" + boundary + "--\r\n";

        // Keep only the required/commonly used fields.
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("fileName", fileName);
        if (folder != null && !folder.isBlank()) {
            fields.put("folder", folder.trim());
        }

        byte[] head = buildTextParts(separator, fields);

        byte[] fileBytes = Files.readAllBytes(file.toPath());

        String fileHeader =
            separator +
                "Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n" +
                "Content-Type: " + mimeType + "\r\n\r\n";

        byte[] fileHeadBytes = fileHeader.getBytes(StandardCharsets.UTF_8);
        byte[] tailBytes = ("\r\n" + end).getBytes(StandardCharsets.UTF_8);

        byte[] total = new byte[head.length + fileHeadBytes.length + fileBytes.length + tailBytes.length];
        System.arraycopy(head, 0, total, 0, head.length);
        System.arraycopy(fileHeadBytes, 0, total, head.length, fileHeadBytes.length);
        System.arraycopy(fileBytes, 0, total, head.length + fileHeadBytes.length, fileBytes.length);
        System.arraycopy(tailBytes, 0, total, head.length + fileHeadBytes.length + fileBytes.length, tailBytes.length);
        return total;
    }

    private static byte[] buildTextParts(String separator, Map<String, String> fields) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : fields.entrySet()) {
            sb.append(separator)
                .append("Content-Disposition: form-data; name=\"")
                .append(e.getKey())
                .append("\"\r\n")
                .append("Content-Type: text/plain; charset=UTF-8\r\n\r\n")
                .append(e.getValue())
                .append("\r\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }
}

