# Syndicati Java - Technical Documentation

## 1. Objective

Syndicati Java is a JavaFX desktop application for residential community management. It mirrors the main web platform while providing a native desktop experience for residents, syndics, and administrators.

The application focuses on fast navigation, secure login, forum participation, event browsing, residence and apartment access, profile management, notifications, media display, and admin workflows.

## 2. Technology Stack

| Layer | Technology |
| --- | --- |
| Runtime | Java 25 |
| UI | JavaFX 25, FXML, CSS |
| Database | MySQL Connector/J |
| Build | Maven |
| Media | ImageKit, Google Drive utilities, local resource fallback |
| Networking | OkHttp, Java HTTP APIs |
| Data formats | JSON, Gson |
| Security | BCrypt, biometric/face modules, session utilities |
| Camera / Vision | JavaCV, OpenCV, ONNX Runtime, webcam capture |
| Mail | Jakarta Mail |
| PDF / Documents | PDFBox, OpenPDF |
| Configuration | Infisical bootstrap, local fallback properties |

## 3. Application Structure

The source code is organized under `src/main/java/com/syndicati`.

| Package Area | Role |
| --- | --- |
| `MainApplication`, `Launcher` | JavaFX startup and application entry points |
| `views/frontend` | Resident-facing screens and controllers |
| `views/backend` | Admin dashboard and management screens |
| `components/shared` | Reusable UI components, navigation, overlays |
| `models` | Domain objects for users, residences, events, forum, syndicat, security |
| `services` | Database, media, AI, mail, voice, camera, notifications, analytics |
| `utils` | Configuration, session, image helpers, logging, validation |
| `resources` | FXML, CSS, images, videos, icons, language files |

## 4. Runtime Flow

1. The launcher starts the JavaFX application.
2. Configuration is loaded from Infisical or local fallback files.
3. The database connection is initialized.
4. Session recovery may check existing login state.
5. The main window loads the correct screen.
6. Controllers request data through services and repositories.
7. UI components render the response and keep the user inside the same stage.

## 5. Main Modules

| Module | Purpose |
| --- | --- |
| Home | Desktop home page and platform overview |
| Authentication | Login, passkey-style flows, face authentication, biometric support |
| Profile | User data, avatar, preferences, session state |
| Residences | Residence and apartment browsing and details |
| Events | Event list, details, participation, media display |
| Forum | Publications, comments, reactions, bookmarks, reports |
| Settings | Theme, language, accessibility, accent, UI scale |
| Messaging | Resident conversations and communication views |
| Admin | Dashboard, users, management and monitoring screens |
| AI / Agent | Assistance, voice, summaries, automation features |

## 6. Shared Data With Web

The Java app and Symfony web app are designed around the same Syndicati domain:

- Users and authentication state.
- Residences and apartments.
- Events and participations.
- Forum publications, comments, reactions, and media.
- Complaints and syndicat-related data.
- Image URLs stored in the database and resolved through ImageKit.

When media is uploaded from either application, the stored database value should be a web-accessible URL or normalized path so both clients can render it consistently.

## 7. Configuration and Secrets

The Java app uses Infisical for shared test/runtime configuration. Real API keys, database credentials, AI keys, and media credentials should not be hardcoded into controllers or views.

Expected configuration categories:

- Database connection.
- ImageKit credentials and endpoint.
- AI provider keys.
- Mail/SMS credentials.
- Google or external service credentials where enabled.
- Application mode and feature toggles.

## 8. Security Design

Security-related features include:

- Password hashing with BCrypt.
- Session management utilities.
- Biometric and face authentication flows.
- Camera-based enrollment/authentication support.
- Role-aware UI behavior for residents, syndics, and admins.
- Restricted admin screens.

Sensitive actions should always validate the current session and role before making database changes.

## 9. Media and Image Handling

The Java app should display images from:

- ImageKit URLs saved by the web app.
- ImageKit URLs saved by the Java app.
- Local fallback resources when a remote image is missing.

Image loading should avoid blocking the JavaFX UI thread. Remote images should be loaded asynchronously or through JavaFX image loading options when possible.

## 10. Performance Notes

The JavaFX application should keep heavy operations away from the UI thread:

- Database reads and writes.
- Remote image loading.
- AI requests.
- Mail/SMS operations.
- Camera and face recognition processing.
- File and PDF generation.

Use background tasks for long-running operations, then update the UI on the JavaFX application thread.

## 11. Build and Run

Install dependencies and build:

```bash
mvn clean install
```

Run the application from Maven:

```bash
mvn javafx:run
```

If using IntelliJ IDEA, run the configured JavaFX application entry point. The project should load configuration automatically through the existing Infisical/bootstrap setup.

## 12. Delivery Notes

- Do not commit generated `target/` output.
- Do not commit real secrets, local logs, or temporary files.
- Keep installer/build scripts that are part of delivery.
- Keep documentation synchronized when routes, services, or major screens change.
