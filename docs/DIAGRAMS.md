# Syndicati Java - Diagrams

## 1. Desktop Application Architecture

```mermaid
flowchart LR
    User[Desktop User] --> Stage[JavaFX Stage]
    Stage --> Controllers[FXML Controllers]
    Controllers --> Services[Application Services]
    Services --> Repositories[Data Access / Repositories]
    Repositories --> DB[(MySQL Database)]

    Services --> ImageKit[ImageKit]
    Services --> AI[Groq / Gemini]
    Services --> Mail[Mail / Notification Services]
    Services --> Camera[Camera / Face Recognition]
    Services --> Infisical[Infisical Configuration]
```

## 2. Startup Flow

```mermaid
sequenceDiagram
    participant L as Launcher
    participant A as MainApplication
    participant C as Config Loader
    participant D as Database Service
    participant S as Session Service
    participant UI as JavaFX UI

    L->>A: Start JavaFX
    A->>C: Load configuration
    C-->>A: Runtime settings
    A->>D: Initialize database access
    A->>S: Check saved session
    S-->>A: Session state
    A->>UI: Load selected screen
    UI-->>A: User interaction
```

## 3. Shared Data With Web

```mermaid
flowchart TB
    Web[Symfony Web App] --> DB[(Shared MySQL)]
    Java[JavaFX App] --> DB

    Web --> ImageKit[ImageKit Media Storage]
    Java --> ImageKit

    DB --> Events[Events]
    DB --> Residences[Residences]
    DB --> Forum[Forum]
    DB --> Users[Users]
    DB --> Complaints[Complaints]

    ImageKit --> WebDisplay[Web Image Display]
    ImageKit --> JavaDisplay[Java Image Display]
```

## 4. Event Image Loading

```mermaid
sequenceDiagram
    participant UI as Event Card
    participant C as Event Controller
    participant R as Event Repository
    participant D as Database
    participant I as ImageKit URL

    UI->>C: Request events
    C->>R: Load event list
    R->>D: Query events
    D-->>R: Event rows with image URLs
    R-->>C: Event models
    C-->>UI: Render cards
    UI->>I: Load image asynchronously
    I-->>UI: Image content
```

## 5. Face Authentication Flow

```mermaid
flowchart TD
    Start[User selects Face ID] --> Camera[Open camera]
    Camera --> Detect[Detect face]
    Detect --> Quality{Good frame?}
    Quality -- No --> Retry[Ask for better position]
    Retry --> Detect
    Quality -- Yes --> Compare[Compare embedding]
    Compare --> Match{Match found?}
    Match -- No --> Deny[Reject authentication]
    Match -- Yes --> Session[Create authenticated session]
    Session --> Home[Open main app]
```

## 6. Settings Flow

```mermaid
flowchart LR
    Settings[Settings Screen] --> Theme[Theme]
    Settings --> Accent[Accent Color]
    Settings --> Language[Language]
    Settings --> Accessibility[Accessibility]

    Accessibility --> Motion[Reduce Motion]
    Accessibility --> Contrast[High Contrast]
    Accessibility --> Dyslexia[Dyslexia Font]
    Accessibility --> Controls[Comfortable Controls]
    Accessibility --> Scale[UI Scale]

    Theme --> Preferences[(User Preferences)]
    Accent --> Preferences
    Language --> Preferences
    Accessibility --> Preferences
```

## 7. Admin Dashboard Flow

```mermaid
flowchart TD
    Admin[Admin User] --> Dashboard[Dashboard]
    Dashboard --> Users[User Management]
    Dashboard --> Residences[Residence Management]
    Dashboard --> Events[Event Management]
    Dashboard --> Forum[Forum Monitoring]
    Dashboard --> Metrics[Metrics and Logs]

    Users --> DB[(Database)]
    Residences --> DB
    Events --> DB
    Forum --> DB
    Metrics --> DB
```
