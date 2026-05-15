# Syndicati JavaFX

Native JavaFX desktop client for the Syndicati residential community platform. It connects to the same MySQL database as the Symfony website and mirrors the core resident/admin workflows with a desktop-first UI.

## Stack

- Java 25, JavaFX 25, Maven
- MySQL/MariaDB via JDBC services
- Symfony-compatible database schema
- ImageKit media URLs with legacy local-file fallback
- SMTP/Gmail mail, Twilio SMS, Discord forum webhook, WebAuthn, Face ID services
- Optional Python workers for biometric/vision tasks

## Modules

- Frontend: landing page, profile, forum, residence, syndicat/reclamation, events, messaging, notifications
- Backend: admin dashboard, users, forum moderation, residence/appartement/maintenance, events, syndicat tools
- Security: password auth, Google OAuth, 2FA, WebAuthn/passkeys, Face ID enrollment/authentication, activity logs
- AI: assistant, profile/avatar generation hooks, forum feeling analysis, residence/maintenance recommendations

## Run

```bat
dev-run.bat
```

Or with Maven:

```bat
tools\maven\bin\mvn.cmd -DskipTests compile
tools\maven\bin\mvn.cmd javafx:run
```

Local configuration lives in `.env.local` and `src/main/resources/application.local.properties` where present. Keep DB credentials, mail tokens, Twilio keys, ImageKit keys, and webhook URLs out of commits.

## Build

```bat
tools\maven\bin\mvn.cmd clean package -DskipTests
build-exe.bat
BuildInstaller.ps1
```

The packaged app uses bundled runtime/tooling under `tools/` when available.

## Verify

```bat
tools\maven\bin\mvn.cmd -q -DskipTests compile
cmd /c test_compile.bat
```

## Project Shape

```text
src/main/java/com/syndicati/
  components/      Shared UI pieces: header, footer, floating agent/messaging
  controllers/     View/controller orchestration
  models/          Domain models matching database tables
  services/        JDBC, auth, media, forum, events, residence, messaging, AI
  utils/           Session, validation, preferences, navigation helpers
src/main/resources/
  styles/          JavaFX CSS
  assets/          Local fallback assets
workers/           Optional helper services
tools/             Maven/JDK/runtime helpers
```

## Cross-App Contract

- Web and JavaFX intentionally share tables for users, forum, events, residence, notifications, standing, and messaging.
- Messaging uses `conversation`, `conversation_participant`, `message`, and `message_attachment`; the desktop client refreshes from DB so web-sent messages appear without relaunching.
- Forum `Jeux Video` publications announce to Discord from both clients.
- Media can be an absolute ImageKit URL or a legacy filename; loaders must handle both.

Syndicati JavaFX is the desktop face of the same platform, not a separate product.
