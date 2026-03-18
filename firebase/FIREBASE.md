# Firebase Configuration Guide

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [One-time Project Setup](#one-time-project-setup)
3. [Environment Variables](#environment-variables)
4. [Getting FIREBASE_SERVICE_ACCOUNT_JSON](#getting-firebase_service_account_json)
5. [Getting INTERNAL_SECRET](#getting-internal_secret)
6. [Build Functions](#build-functions)
7. [Local Development with Emulator](#local-development-with-emulator)
8. [Deploy to Production](#deploy-to-production)
9. [Dev / Staging Environment](#dev--staging-environment)
10. [Troubleshooting](#troubleshooting)

---

## Prerequisites

```bash
npm install -g firebase-tools
firebase login
```

Set your project alias (run once from the `firebase/` directory):

```bash
cd firebase
firebase use --add   # pick your project, alias it "prod"
```

---

## One-time Project Setup

### Enable Identity Platform

`beforeUserCreated` is a **blocking function** and requires Identity Platform (upgraded Firebase Auth). Without it, deploy fails with `OPERATION_NOT_ALLOWED`.

1. Go to: `https://console.cloud.google.com/customer-identity?project=YOUR_PROJECT_ID`
2. Click **Enable Identity Platform**

Free tier: 50K monthly active users — no cost for dev projects.

### firebase.json — add emulators block

```json
{
  "functions": [{ "source": "functions", "codebase": "default" }],
  "emulators": {
    "auth":      { "port": 9099 },
    "functions": { "port": 5001 },
    "ui":        { "enabled": true, "port": 4000 }
  }
}
```

---

## Environment Variables

| Variable | Used by | Description |
|---|---|---|
| `FIREBASE_SERVICE_ACCOUNT_JSON` | Spring Boot (`FirebaseConfig.kt`) | Full JSON content of the service account key. Allows the backend to verify Firebase ID tokens. |
| `INTERNAL_SECRET` | Spring Boot + Cloud Function | Shared secret sent as `X-Internal-Secret` header. Protects `/firebase/users` from public access. |
| `BACKEND_URL` | Cloud Function (`index.ts`) | Base URL of the Spring Boot backend. Defaults to `http://localhost:8080`. |
| `MONGODB_URI` | Spring Boot | MongoDB connection string. Defaults to `mongodb://localhost:27017/polychain-bets`. |

---

## Getting FIREBASE_SERVICE_ACCOUNT_JSON

1. Firebase Console → your project → gear icon → **Project settings**
2. **Service accounts** tab → **Generate new private key** → **Generate key**
3. A `.json` file downloads — this is your service account key

The Spring Boot backend expects the **entire JSON content** as an env var (not a file path):

```bash
# Read from the downloaded file
export FIREBASE_SERVICE_ACCOUNT_JSON=$(cat /path/to/auth-poluchain-firebase-adminsdk-XXXX.json)

# Then run the backend
./mvnw spring-boot:run
```

Or put it in the root `.env` file (already gitignored):

```
FIREBASE_SERVICE_ACCOUNT_JSON={"type":"service_account","project_id":"..."}
```

**Never commit this file or its contents to git** — it grants full admin access to your Firebase project.

---

## Getting INTERNAL_SECRET

Generate any random string — this is a secret you create yourself:

```bash
openssl rand -hex 32
# → 65009fef4f93800e659fb5295645586a...
```

Set it in two places so they match:
- Root `.env` → used by Spring Boot
- `firebase/functions/.env` → used by the Cloud Function **emulator only**

### Production: store in Google Secret Manager

The deployed function reads `INTERNAL_SECRET` from Secret Manager via the `secrets: ["INTERNAL_SECRET"]` declaration in `index.ts` — plain environment variables are not used in production.

**One-time setup:**

```bash
# Create the secret
gcloud secrets create INTERNAL_SECRET --project=auth-poluchain

# Store the value (replace with your actual secret)
echo -n "YOUR_SECRET_VALUE" | gcloud secrets versions add INTERNAL_SECRET --data-file=- --project=auth-poluchain
```

Firebase automatically grants the function's service account `Secret Manager Secret Accessor` permission at deploy time — no manual IAM setup needed.

To update the secret value later:

```bash
echo -n "NEW_SECRET_VALUE" | gcloud secrets versions add INTERNAL_SECRET --data-file=- --project=auth-poluchain
```

Then redeploy the function to pick up the new version.

---

## Build Functions

Always build before deploying or starting the emulator:

```bash
cd firebase/functions
npm install
npm run build   # compiles TypeScript → lib/
```

---

## Local Development with Emulator

### Why the emulator?

The Firebase Auth emulator runs locally and is **completely isolated from production**. It has its own user database and triggers your `beforeUserCreated` function against your local Spring Boot backend.

### Environment variables for the emulator

The emulator reads env vars from `firebase/functions/.env` — **shell variables (`source .env`) do not reach the function runtime**.

`firebase/functions/.env`:
```
BACKEND_URL=http://localhost:8080
INTERNAL_SECRET=your-secret-here
```

This file is gitignored.

### Start everything

```bash
# Terminal 1 — Spring Boot backend
source .env && ./mvnw spring-boot:run

# Terminal 2 — Firebase emulator (from firebase/ directory)
cd firebase
firebase emulators:start --only auth,functions
```

Emulator UI: http://localhost:4000

### Triggering `beforeUserCreated`

**The emulator Admin UI (`localhost:4000/auth`) does NOT trigger `beforeUserCreated`** — it creates users directly, bypassing blocking functions.

To trigger the function you must register via the **client SDK** pointed at the emulator. In `firebase_auth_test.html`, add after `getAuth(app)`:

```js
import { connectAuthEmulator } from "https://www.gstatic.com/firebasejs/10.12.0/firebase-auth.js";

if (auth) connectAuthEmulator(auth, "http://localhost:9099", { disableWarnings: true });
```

Then use the **Registration tab** (email/password) — this fires `beforeUserCreated` → calls `POST /firebase/users` on your backend.

### Google Sign-In does not work with the emulator

`signInWithPopup` opens a real Google OAuth popup and cannot be intercepted by the emulator. To test the full Google Sign-In flow, use a deployed function against real Firebase (see [Dev / Staging Environment](#dev--staging-environment)).

### Exposing localhost to Firebase (for deployed functions)

When the function is deployed but your backend runs locally, Firebase's servers can't reach `localhost:8080`. Use a tunnel:

```bash
# ngrok (free tier, temporary URL)
ngrok http 8080
# → https://abc123.ngrok.io

# Cloudflare Tunnel (free, more stable)
cloudflare tunnel --url localhost:8080
```

Set the tunnel URL as `BACKEND_URL` in the Firebase console or redeploy with the updated `.env`.

---

## Deploy to Production

### Cloud Build permissions

Firebase Functions v2 uses Google Cloud Build. If deploy fails with a permissions error, go to:

`https://console.cloud.google.com/iam-admin/iam?project=YOUR_PROJECT_ID`

Find `YOUR_PROJECT_NUMBER@cloudbuild.gserviceaccount.com` and ensure it has:
- `Cloud Build Service Account`
- `Cloud Functions Developer`
- `Artifact Registry Writer`
- `Logging Writer`

### Set environment variables

In Firebase Console → Functions → `processNewUser` → Edit → Environment variables:

| Variable | Value |
|---|---|
| `BACKEND_URL` | `https://your-backend.com` |
| `INTERNAL_SECRET` | same value as in backend env |

### Deploy

```bash
cd firebase
firebase deploy --only functions:processNewUser
```

### Artifact Registry cleanup (optional)

Prevent accumulation of old container images that incur a small monthly cost:

```bash
gcloud artifacts repositories set-cleanup-policies gcf-artifacts \
  --project=YOUR_PROJECT_ID \
  --location=us-central1 \
  --policy='[{"name":"delete-old","action":{"type":"Delete"},"condition":{"olderThan":"604800s"}}]'
```

---

## Dev / Staging Environment

Firebase has no built-in staging — the standard approach is a **separate Firebase project**.

```
auth-poluchain        ← production
auth-poluchain-dev    ← development
```

### Create a dev project

1. Firebase Console → Add project → `auth-poluchain-dev`
2. Enable Authentication → add Google sign-in provider
3. Enable Identity Platform (required for blocking functions)
4. Add a web app → copy the new `firebaseConfig`

### Register both projects with the CLI

```bash
cd firebase
firebase use --add   # select auth-poluchain-dev, alias "dev"
firebase use --add   # select auth-poluchain, alias "prod"
```

### Deploy function to dev

```bash
firebase use dev
firebase deploy --only functions:processNewUser
```

Set `BACKEND_URL` and `INTERNAL_SECRET` in the dev project's Firebase Console.

### Switch projects

```bash
firebase use dev    # target dev
firebase use prod   # target production
```

---

## Troubleshooting

### `OPERATION_NOT_ALLOWED: Blocking Functions may only be configured for GCIP projects`
→ Enable Identity Platform: `https://console.cloud.google.com/customer-identity?project=YOUR_PROJECT_ID`

### `X-Internal-Secret` header is empty in the function
→ Shell `export` / `source .env` does not reach the function runtime. Put the vars in `firebase/functions/.env` instead.

### `beforeUserCreated` never fires locally
→ The SDK must be pointed at the emulator via `connectAuthEmulator`. Creating users via the emulator Admin UI does not trigger blocking functions.

### `result.user.sendEmailVerification is not a function`
→ Firebase SDK v9+ uses standalone functions. Import `sendEmailVerification` and call it as `sendEmailVerification(user)`, not `user.sendEmailVerification()`.

### Cloud Build permissions error on deploy
→ Grant the Cloud Build service account (`PROJECT_NUMBER@cloudbuild.gserviceaccount.com`) the `Cloud Build Service Account` and `Artifact Registry Writer` roles in Google Cloud IAM.

### API key visible in frontend HTML — is it safe?
→ Yes. The Firebase API key is a **public identifier**, not a secret. Real security comes from Firebase Security Rules, ID token verification on the backend, and the `INTERNAL_SECRET` for internal endpoints. Optionally enable **App Check** in Firebase Console to restrict usage to your app only.