# Syndicati Java - Internal API and Integration Reference

The Java application is a desktop client, so most "API" behavior is implemented through controllers, services, repositories, and external integrations rather than public HTTP endpoints.

## 1. UI Controller Contracts

| Area | Responsibility |
| --- | --- |
| Startup controllers | Load initial screens and manage session recovery |
| Authentication controllers | Handle login, biometric/face actions, and account security |
| Home controllers | Render main page data and navigation |
| Event controllers | Load events, event cards, details, and participation actions |
| Residence controllers | Load residences, apartments, and related details |
| Forum controllers | Display publications, comments, reactions, bookmarks, and reports |
| Settings controllers | Persist theme, language, accessibility, accent, and UI scale |
| Admin controllers | Manage dashboard data, users, residence/event/forum administration |

Controller rules:

- Keep UI updates on the JavaFX application thread.
- Move database, network, camera, and AI work to background tasks.
- Validate session and role before protected actions.
- Normalize images before binding them into JavaFX nodes.

## 2. Service Contracts

| Service Type | Purpose |
| --- | --- |
| Database service | Create and manage MySQL connections |
| ImageKit service/config | Upload and load media shared with the web app |
| Google Drive utilities | Optional document/media integration |
| Voice service | Speech input and voice-assisted interactions |
| Camera service | Camera access for face authentication |
| InsightFace / biometric services | Face enrollment and matching |
| Mailer service | Email delivery and async mail operations |
| AI services | Chat, sentiment, summaries, and assistant behavior |
| Notification services | User-facing alerts and status updates |
| Observability/log services | Runtime diagnostics and monitoring |
| PDF/document services | Generate exports and reports |

## 3. Repository / Data Access Reference

| Domain | Data Managed |
| --- | --- |
| Users | Accounts, roles, profile data, preferences |
| Residences | Residence records, images, addresses, details |
| Apartments | Apartment records linked to residences |
| Events | Event records, dates, locations, media |
| Participations | Resident event registration state |
| Forum | Publications, comments, reactions, bookmarks, reports |
| Syndicat | Syndic records and related residence management |
| Complaints | Reclamations, responses, images, status |
| Security | Face/biometric data, passkey-related state where enabled |

## 4. External Integrations

| Integration | Used For | Notes |
| --- | --- | --- |
| Infisical | Runtime secrets | Loads DB, media, AI, and communication configuration |
| MySQL | Shared persistence | Must match the web app schema |
| ImageKit | Public media storage | Preferred image source for web/Java compatibility |
| Groq / Gemini | AI features | Optional depending on configured keys |
| Jakarta Mail provider | Email | Used for mail notifications and account flows |
| Camera / OpenCV / ONNX | Face ID | Used for enrollment and authentication |
| PDFBox / OpenPDF | PDF generation | Used for exports and reports |

## 5. Image URL Rules

| Input Type | Expected Handling |
| --- | --- |
| Full `https://` ImageKit URL | Load directly |
| Stored ImageKit path | Resolve against ImageKit endpoint |
| Local resource path | Load from bundled resources |
| Missing or invalid image | Show fallback placeholder |

Both the web and Java apps should store image values in a format that can be resolved by both clients.

## 6. Authentication and Security APIs

| Flow | Main Steps |
| --- | --- |
| Password login | Read credentials, verify hash, create session |
| Face ID enrollment | Capture face, validate quality, store biometric reference |
| Face ID login | Capture face, compare reference, create session on match |
| Biometric support | Register available biometric state and validate before login |
| Role checks | Gate admin/syndic/resident actions before database mutation |

## 7. Background Work Guidelines

Use background execution for:

- Loading large event/residence/forum lists.
- Fetching remote images.
- Uploading media.
- Running face detection.
- Calling AI services.
- Sending email.
- Generating PDFs.

After completion, update JavaFX controls using the JavaFX application thread.

## 8. Error Handling Rules

| Situation | Expected Behavior |
| --- | --- |
| Database unavailable | Show a clear connection error and prevent unsafe actions |
| Image unavailable | Show placeholder and keep the card layout stable |
| AI key missing | Disable direct AI features gracefully |
| Camera unavailable | Disable Face ID actions and show user-facing guidance |
| Invalid session | Return to login or recovery screen |
| Unauthorized action | Show access denied and avoid partial database writes |
