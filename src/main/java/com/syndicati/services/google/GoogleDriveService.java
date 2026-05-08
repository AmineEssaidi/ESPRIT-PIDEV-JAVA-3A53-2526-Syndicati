package com.syndicati.services.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.syndicati.models.syndicat.Reclamation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

public class GoogleDriveService {
    private static final String APPLICATION_NAME = "Syndicati";
    private static final String APP_ROOT_FOLDER_NAME = "Syndicati Reclamations";
    private static final String RECLAMATION_PDF_NAME = "reclamation-details.pdf";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"), ".credentials/syndicati-drive");
    private static FileDataStoreFactory DATA_STORE_FACTORY;
    private static HttpTransport HTTP_TRANSPORT;
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static com.google.api.client.http.HttpRequestInitializer authorize() throws IOException {
        try (InputStream in = GoogleDriveService.class.getResourceAsStream("/google_drive_credentials.json")) {
            if (in == null) {
                throw new IOException("Resource not found: google_drive_credentials.json");
            }

            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(DATA_STORE_FACTORY)
                    .setAccessType("offline")
                    .build();
            
            Credential credential = flow.loadCredential("user");
            if (credential != null && (credential.getRefreshToken() != null || credential.getExpiresInSeconds() == null || credential.getExpiresInSeconds() > 60)) {
                System.out.println("[GoogleDrive] Using cached credential (no browser popup needed)");
                return credential;
            }
            
            System.out.println("[GoogleDrive] No valid cached credential, initializing new auth...");
            credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
            return credential;
        }
    }

    public static Drive getDriveService() throws IOException {
        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, authorize())
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private static String getOrCreateFolder(Drive service, String folderName, String parentId) throws IOException {
        String query = "name = '" + escapeDriveQueryValue(folderName) + "' and mimeType = 'application/vnd.google-apps.folder' and trashed = false";
        if (parentId != null) {
            query += " and '" + parentId + "' in parents";
        }
        
        FileList result = service.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("nextPageToken, files(id, name)")
                .execute();
        
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            File fileMetadata = new File();
            fileMetadata.setName(folderName);
            fileMetadata.setMimeType("application/vnd.google-apps.folder");
            if (parentId != null) {
                fileMetadata.setParents(Collections.singletonList(parentId));
            }
            File file = service.files().create(fileMetadata)
                    .setFields("id")
                    .execute();
            return file.getId();
        } else {
            return files.get(0).getId();
        }
    }

    public static void uploadOrUpdateFileAsync(String userFolderName, String filename, java.io.File localFile) {
        new Thread(() -> {
            try {
                uploadOrUpdateFile(userFolderName, filename, localFile);
                System.out.println("[GoogleDrive] Successfully uploaded/updated: " + filename);
            } catch (IOException e) {
                System.err.println("[GoogleDrive] Failed to upload/update: " + filename);
                e.printStackTrace();
            }
        }).start();
    }

    public static void uploadOrUpdateFile(String userFolderName, String filename, java.io.File localFile) throws IOException {
        if (localFile == null || !localFile.exists()) return;
        
        Drive service = getDriveService();
        
        String rootFolderId = getOrCreateFolder(service, APP_ROOT_FOLDER_NAME, null);
        String userFolderId = getOrCreateFolder(service, sanitizeFolderName(userFolderName), rootFolderId);

        uploadOrUpdateFileInFolder(service, userFolderId, filename, localFile);
    }

    public static void uploadOrUpdateReclamationPdf(Reclamation reclamation, java.io.File localFile) throws IOException {
        if (reclamation == null || reclamation.getUser() == null) return;

        Drive service = getDriveService();

        String rootFolderId = getOrCreateFolder(service, APP_ROOT_FOLDER_NAME, null);
        String reclamationFolderId = getOrCreateFolder(service, buildReclamationFolderName(reclamation), rootFolderId);

        uploadOrUpdateFileInFolder(service, reclamationFolderId, buildReclamationPdfName(reclamation), localFile);
    }

    private static void uploadOrUpdateFileInFolder(Drive service, String folderId, String filename, java.io.File localFile) throws IOException {
        String query = "name = '" + escapeDriveQueryValue(filename) + "' and '" + folderId + "' in parents and trashed = false";
        FileList result = service.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute();

        List<File> files = result.getFiles();

        String mimeType = Files.probeContentType(localFile.toPath());
        if (mimeType == null) mimeType = "application/octet-stream";
        FileContent mediaContent = new FileContent(mimeType, localFile);

        if (files != null && !files.isEmpty()) {
            String fileId = files.get(0).getId();
            service.files().update(fileId, new File(), mediaContent).execute();
        } else {
            File fileMetadata = new File();
            fileMetadata.setName(filename);
            fileMetadata.setParents(Collections.singletonList(folderId));

            service.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();
        }
    }

    private static String buildReclamationFolderName(Reclamation reclamation) {
        StringBuilder name = new StringBuilder();
        if (reclamation.getIdReclamations() != null && reclamation.getIdReclamations() > 0) {
            name.append("Reclamation #").append(reclamation.getIdReclamations());
        } else {
            name.append("Reclamation");
        }

        String title = sanitizeFolderName(reclamation.getTitreReclamations());
        if (!title.isBlank()) {
            name.append(" - ").append(title);
        }

        return sanitizeFolderName(name.toString());
    }

    private static String buildReclamationPdfName(Reclamation reclamation) {
        if (reclamation.getIdReclamations() != null && reclamation.getIdReclamations() > 0) {
            return "reclamation-" + reclamation.getIdReclamations() + ".pdf";
        }

        return RECLAMATION_PDF_NAME;
    }

    private static String sanitizeFolderName(String value) {
        if (value == null || value.isBlank()) {
            return "Reclamation";
        }

        String sanitized = value.trim()
                .replaceAll("[\\\\/:*?\"<>|]", "_")
                .replaceAll("\\s+", " ")
                .replaceAll("[^a-zA-Z0-9 _#-]", "_")
                .trim();

        return sanitized.isBlank() ? "Reclamation" : sanitized;
    }

    private static String escapeDriveQueryValue(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("'", "\\'");
    }

    public static java.io.File resolveUploadFile(String relativePath) {
        if (relativePath == null || relativePath.isEmpty() || "-".equals(relativePath)) return null;
        String uploadsPath = System.getProperty("user.dir") + java.io.File.separator + "uploads";
        return new java.io.File(uploadsPath, relativePath);
    }
}
