# Frontend — Firebase Auth Dev Console

Vite + TypeScript app for testing Firebase Authentication against the Spring Boot backend.

## Prerequisites

- Node.js 18+
- npm 9+

## Setup

```bash
cd frontend
npm install
```

Copy the env template and fill in your Firebase project values:

```bash
cp .env.example .env
```

`.env` is already populated with the `auth-poluchain` project config — no changes needed for the default project.

## Run in development

```bash
npm run dev
```

Opens at http://localhost:3000 with hot reload.

## Build for production

```bash
npm run build
```

Output goes to `frontend/dist/`. Serve it with any static host or preview locally:

```bash
npm run preview   # → http://localhost:4173
```

## Environment variables

All variables must be prefixed with `VITE_` to be accessible in the browser.

| Variable | Description |
|---|---|
| `VITE_FIREBASE_API_KEY` | Firebase API key |
| `VITE_FIREBASE_AUTH_DOMAIN` | `<project-id>.firebaseapp.com` |
| `VITE_FIREBASE_PROJECT_ID` | Firebase project ID |
| `VITE_FIREBASE_STORAGE_BUCKET` | `<project-id>.appspot.com` |
| `VITE_FIREBASE_MESSAGING_SENDER_ID` | Numeric sender ID |
| `VITE_FIREBASE_APP_ID` | Firebase app ID |
| `VITE_USE_EMULATOR` | `true` to route auth through local emulator (`localhost:9099`). Default: `false` |

Values are read from `.env` at build time. After changing `.env`, restart `npm run dev`.

## Local emulator mode

To test against the Firebase Auth emulator instead of production:

**1.** Set in `.env`:
```
VITE_USE_EMULATOR=true
```

**2.** Start the backend and emulator:
```bash
# Terminal 1 — Spring Boot
source ../.env && ./mvnw spring-boot:run

# Terminal 2 — Firebase emulator (from firebase/)
cd ../firebase
firebase emulators:start --only auth,functions
```

**3.** Start the frontend:
```bash
npm run dev
```

Use the **Register** tab to create a new email/password user — this fires `beforeUserCreated` → calls `POST /firebase/users` on the backend.

> Google Sign-In does not work with the emulator. Use email/password for local testing.

## Project structure

```
frontend/
  index.html        HTML shell — no inline scripts or styles
  vite.config.ts    Vite config (port 3000)
  tsconfig.json     TypeScript config
  .env              Firebase config (gitignored)
  .env.example      Template to commit
  src/
    main.ts         Entry point — DOM wiring and event listeners
    firebase.ts     Firebase app init + emulator toggle
    auth.ts         Auth actions: signInGoogle, signInEmail, registerEmail, resetPassword, signOut
    ui.ts           UI helpers: setStatus, handleUser, switchTab, copyToken, copyCurl
    style.css       Design system styles
```

## What the app does

| Tab | Action |
|---|---|
| **Sign in** | Google OAuth popup or email/password sign-in |
| **Register** | Create email/password account + send verification email |
| **Reset** | Send password reset email |

After authentication the right panel shows:
- User info (UID, email, display name, provider, email verified status)
- Full JWT ID token (copyable)
- Ready-to-run `curl` command for `POST /auth/signin` on the Spring Boot backend