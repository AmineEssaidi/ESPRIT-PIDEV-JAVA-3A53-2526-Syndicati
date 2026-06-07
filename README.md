# Syndicati JavaFX

## Description

Syndicati JavaFX est l'application desktop native de la plateforme Syndicati. Elle permet aux residents, syndics et administrateurs d'utiliser les principaux services de gestion residentielle depuis une interface bureau connectee a la meme base de donnees que le site web Symfony.

L'application couvre les besoins essentiels d'une residence moderne : authentification securisee, profil resident, forum communautaire, evenements, residences, appartements, reclamations, messagerie, notifications, tableau de bord admin, medias, biometrie et assistance IA.

Le client JavaFX n'est pas un produit separe : il partage les donnees, les images ImageKit, les utilisateurs, les publications forum, les evenements, les reclamations et la logique fonctionnelle avec l'application web Syndicati.

## Technologies utilisees

Frontend desktop :
- JavaFX 25
- CSS JavaFX
- FXML / vues Java code
- JavaFX WebView

Backend / logique applicative :
- Java 25
- Maven
- JDBC
- MySQL / MariaDB
- Services Java par domaine
- Threading asynchrone et executors pour eviter de bloquer l'interface

Services et integrations :
- Infisical pour charger la configuration de demonstration
- ImageKit pour les images et medias
- Google OAuth
- Gmail / SMTP pour les emails OTP et notifications
- Twilio / SMS
- WebAuthn / Passkeys
- TOTP 2FA
- Face ID / biometrie
- Discord IPC / webhook forum
- Gemini, Groq, Mistral et Hugging Face pour les fonctions IA
- OpenObserve / Langfuse / LogAI pour l'observabilite optionnelle
- PDFBox / OpenPDF pour les exports
- ZXing pour les QR codes

## Prerequis

- Windows
- Java 25 ou le JDK fourni dans `tools/jdk-25/`
- Maven ou le Maven fourni dans `tools/maven/`
- MySQL / MariaDB accessible par la configuration Infisical
- Python 3 optionnel pour certains services biometrie / vision
- Git

## Installation

Cloner le depot :

```bash
git clone https://github.com/USERNAME/Esprit-PI-3A53-2526-Syndicati.git
cd Esprit-PI-3A53-2526-Syndicati
```

Verifier et installer la plupart des prerequis avant d'ouvrir le projet dans IntelliJ IDEA :

```bat
verify_and_install_prereqs.bat
```

Ce script verifie notamment Java 25, Maven, Python/pip, les packages Python utilises par la biometrie et les services IA, les dossiers runtime, la configuration Infisical, MySQL, ImageKit/medias et la resolution des dependances Maven. Il peut installer automatiquement une partie des outils si `winget` est disponible.

Installer/telecharger les dependances Maven :

```bat
tools\maven\bin\mvn.cmd clean install -DskipTests
```

Dans IntelliJ IDEA :
- ouvrir le dossier du projet ;
- laisser IntelliJ importer le projet Maven depuis `pom.xml` ;
- attendre la fin du telechargement/indexation des dependances Maven ;
- lancer l'application depuis IntelliJ avec la classe `com.syndicati.Launcher`.

Si le JDK local n'est pas disponible, utiliser le script prevu :

```bat
powershell -ExecutionPolicy Bypass -File download_jdk25.ps1
```

## Lancement

Lancement recommande avec IntelliJ IDEA :

1. Ouvrir le dossier du projet dans IntelliJ IDEA.
2. Attendre que Maven importe le `pom.xml` et telecharge les dependances.
3. Verifier que le SDK du projet est Java 25.
4. Lancer la classe `com.syndicati.Launcher`.
5. Si IntelliJ demande une configuration, choisir `Application`, main class `com.syndicati.Launcher`, puis utiliser le module `syndicati`.

Lancement Maven direct optionnel :

```bat
tools\maven\bin\mvn.cmd javafx:run
```

Le fichier `dev-run.bat` reste uniquement un outil de depannage/developpement local, pas la methode de lancement principale pour la livraison.

Compilation seule :

```bat
tools\maven\bin\mvn.cmd -DskipTests compile
```

## Variables d'environnement

La configuration de demonstration est chargee depuis Infisical via `.env` et `src/main/java/com/syndicati/utils/config/EnvConfig.java`.

Le fichier `.env` contient uniquement le bootstrap Infisical du projet universitaire. Les secrets reels comme la base de donnees, les cles API, SMTP, OAuth, ImageKit et IA sont recuperes au demarrage depuis Infisical.

Variables principales attendues :
- `APP_ENV`
- `DATABASE_URL`
- `MAILER_DSN`
- `MAILER_FROM_EMAIL`
- `MAILER_FROM_NAME`
- `GOOGLE_OAUTH_CLIENT_ID`
- `GOOGLE_OAUTH_CLIENT_SECRET`
- `GOOGLE_DRIVE_CREDENTIALS_JSON` ou `GOOGLE_DRIVE_CREDENTIALS_BASE64`
- `IMAGEKIT_PUBLIC_KEY`
- `IMAGEKIT_PRIVATE_KEY`
- `IMAGEKIT_URL_ENDPOINT`
- `IMAGEKIT_ENABLED`
- `GEMINI_API_KEY`
- `GEMINI_MODEL`
- `GROQ_API_KEY`
- `GROQ_MODEL`
- `MISTRAL_API_KEY`
- `HUGGING_FACE_API_KEY`
- `TWILIO_DSN`
- `TEXTBEE_API_KEY`
- `TEXTBEE_DEVICE_ID`
- `HCAPTCHA_SITE_KEY`
- `HCAPTCHA_SECRET_KEY`
- `DISCORD_FORUM_WEBHOOK_URL`

Les fichiers locaux comme `config/application.local.properties` ne doivent pas contenir de secrets reels dans le depot. Ils servent uniquement d'override local optionnel.

## Fonctionnalites principales

- Page d'accueil desktop Syndicati
- Authentification email / mot de passe
- Inscription et recuperation de mot de passe
- OTP email
- Google OAuth
- 2FA TOTP avec QR code
- WebAuthn / Passkeys
- Face ID et services biometrie
- Profil resident avec avatar
- Gestion des preferences et accessibilite
- Forum communautaire avec publications, commentaires, reactions, favoris et moderation
- Envoi d'annonces forum par email
- Evenements avec participation et tickets
- Residences et appartements
- Reclamations syndic et reponses
- Messagerie et conversations
- Notifications
- Tableau de bord administrateur
- Logs d'activite et observabilite
- Assistant IA et analyse de sentiment
- Upload et affichage des medias ImageKit
- Fallback pour anciens chemins locaux `uploads/`
- Export PDF et generation de QR codes
- Synchronisation fonctionnelle avec le site web Symfony

## Structure du projet

```text
src/main/java/com/syndicati/
  components/       Composants UI partages : header, footer, chat, agent
  controllers/      Controleurs par domaine
  interfaces/       Interfaces communes
  models/           Modeles metier et repositories JDBC
  services/         Auth, DB, mail, forum, events, residence, IA, media, logs
  utils/            Config, navigation, session, images, validation, localisation
  views/            Vues JavaFX frontend et backend

src/main/resources/
  app_logo/         Logos Syndicati
  images/           Images locales de fallback
  lang/             Traductions JSON
  styles/           CSS JavaFX

workers/            Workers optionnels LogAI
docs/               Documentation technique
installer/          Scripts de packaging
tools/              Maven, JDK et outils locaux
uploads/            Fichiers locaux de fallback, non destines a remplacer ImageKit
```

## Dossiers importants

- `src/main/java/com/syndicati/services/mail/` : emails OTP, annonces, reclamations et evenements
- `src/main/java/com/syndicati/utils/config/EnvConfig.java` : chargement `.env`, overrides locaux et Infisical
- `src/main/java/com/syndicati/utils/image/imagekit/` : configuration et upload ImageKit
- `src/main/java/com/syndicati/services/security/` : Google OAuth, Face ID, WebAuthn, 2FA
- `src/main/java/com/syndicati/services/observability/` : OpenObserve, Langfuse, LogAI, hCaptcha

## Cross-app contract

- Le web Symfony et JavaFX partagent les memes tables principales.
- Les medias peuvent etre des URLs ImageKit absolues ou d'anciens noms de fichiers locaux.
- Les modules forum, events, residences, reclamations, utilisateurs, notifications, standing et messagerie doivent rester compatibles avec le schema Symfony.
- Les changements web importants sont exposes cote Symfony par les endpoints de synchronisation.

## Build

Build Maven :

```bat
tools\maven\bin\mvn.cmd clean package -DskipTests
```

Build executable :

```bat
build-exe.bat
```

Build installateur :

```powershell
powershell -ExecutionPolicy Bypass -File BuildInstaller.ps1
```

## Nettoyage avant publication GitHub

Ne pas versionner :
- `target/`
- `dist/`
- `installer-output/`
- `.idea/`
- `.vscode/`
- `cache/`
- `__pycache__/`
- `tools/jdk-25/`
- fichiers `.log`
- fichiers temporaires
- executables generes
- fichiers locaux contenant des secrets reels

Conserver :
- `README.md`
- `.gitignore`
- `.env` bootstrap Infisical du projet
- `pom.xml`
- `src/`
- `docs/`
- `workers/`
- `installer/`
- `tools/maven/`
- `build-exe.bat`
- `BuildInstaller.ps1`

## Commandes utiles

Installation :

Exécuter:
verify_and_install_prereqs.bat

```bat
tools\maven\bin\mvn.cmd clean install -DskipTests
```

Lancement recommande :

Ouvrir le projet dans IntelliJ IDEA, laisser Maven importer `pom.xml`, puis lancer la classe `com.syndicati.Launcher`.

Compilation :

```bat
tools\maven\bin\mvn.cmd -DskipTests compile
```

Build :

```bat
tools\maven\bin\mvn.cmd clean package -DskipTests
```

## Demo

```text
Video : https://youtu.be/XC1lMSUlNxE?si=pORqu_waDPZ8eA7W
Captures : demo/
```

## Auteurs

- Mohamed Amine Essaidi
- Mohamed Rayen Kahloun
- Mariem Ben Hamza
- Mohamed Baha Hamdi
- Syrine Negra

Tuteurs:
- Ons Fadhel
- Ameni Hajri

Classe :

- 3A53

Annee universitaire :

- 2025-2026

Projet :

- ESPRIT PI 3A - Syndicati

