# Firebase Functions — Environment Variables Best Practices

---

## 1. Use `defineSecret` and `defineString` (Recommended – Firebase v2)

Firebase Functions v2 introduced first-class param support via the `firebase-functions/params` module:

```js
const { defineSecret, defineString } = require('firebase-functions/params');

const apiKey = defineSecret('API_KEY');
const region = defineString('REGION', { default: 'us-central1' });

exports.myFunction = onRequest({ secrets: [apiKey] }, (req, res) => {
  const key = apiKey.value(); // accessed at runtime
  res.send(`Key is ${key}`);
});
```

> Secrets are stored in **Google Cloud Secret Manager**, never in source code or `firebase.json`.

---

## 2. Use `.env` Files for Non-Sensitive Config

Firebase supports `.env` files per environment:

```
.env              ← shared defaults
.env.local        ← local dev overrides (gitignored)
.env.production   ← production values
.env.staging      ← staging values
```

Set values:

```bash
# .env
REGION=us-central1
MIN_INSTANCES=1
```

Access in code:

```js
process.env.REGION
```

> ⚠️ **Never put secrets** (API keys, passwords) in `.env` files — use Secret Manager for those.

---

## 3. Google Cloud Secret Manager (for Sensitive Values)

Set a secret via CLI:

```bash
firebase functions:secrets:set API_KEY
# prompts you to enter the value securely
```

Bind to a function:

```js
exports.myFn = onRequest({ secrets: ["API_KEY"] }, (req, res) => {
  res.send(process.env.API_KEY); // available as env var at runtime
});
```

---

## 4. Avoid the Legacy `functions.config()` (v1 – Deprecated)

The old approach is **deprecated** and should be avoided in new projects:

```js
// ❌ Deprecated
firebase functions:config:set somekey.value="hello"
functions.config().somekey.value
```

---

## 5. Switching Between Local, Staging, and Production

### File Structure

```
functions/
├── .env                 ← shared config (committed)
├── .env.local           ← emulator only (gitignored, never deployed)
├── .env.dev             ← deployed dev
├── .env.staging         ← deployed staging
├── .env.production      ← deployed production
```

### How Firebase Picks the Right File

Firebase **automatically merges** env files based on your project alias:

| Command | Files Loaded |
|---|---|
| `firebase emulators:start` | `.env` + `.env.local` |
| `firebase deploy --project staging` | `.env` + `.env.staging` |
| `firebase deploy --project production` | `.env` + `.env.production` |

### Set Up Project Aliases

```bash
firebase use --add   # maps alias → Firebase project
```

Your `.firebaserc` will look like:

```json
{
  "projects": {
    "default": "my-app-dev",
    "staging": "my-app-staging",
    "production": "my-app-production"
  }
}
```

### Switch Active Environment

```bash
firebase use staging      # switch to staging alias
firebase use production   # switch to production alias
firebase use              # see current active project
```

### Secrets Are Scoped Per Project

Each Firebase project has its own Secret Manager, so secrets are **automatically scoped** per environment:

```bash
firebase use staging
firebase functions:secrets:set API_KEY   # sets secret on staging project

firebase use production
firebase functions:secrets:set API_KEY   # sets secret on production project
```

---

## 6. Separating Emulator Config from Deployed Config

### The Problem

```bash
firebase emulators:start   # loads .env + .env.local ✅
firebase deploy            # loads .env + .env.local ❌ (if default = dev, .env.local leaks in)
```

### The Solution

`.env.local` is a **Firebase-reserved filename** — it is only ever loaded by the emulator, never by `firebase deploy`, regardless of the active project alias.

So as long as you:
- Put emulator-only config in `.env.local`
- Always deploy explicitly with `--project <alias>`

...your local config will never leak into deployed environments.

### `.env.local` — Emulator Only

```bash
# .env.local  (gitignored, never deployed)
API_URL=http://localhost:5001
FIRESTORE_EMULATOR_HOST=localhost:8080
USE_EMULATOR=true
DEBUG=true
```

### Deploy Explicitly by Alias

```bash
# Never rely on implicit default — always specify the target
firebase deploy --only functions --project dev
firebase deploy --only functions --project staging
firebase deploy --only functions --project production
```

### NPM Scripts to Enforce This

```json
{
  "scripts": {
    "emulate": "firebase emulators:start",
    "deploy:dev": "firebase deploy --only functions --project dev",
    "deploy:staging": "firebase deploy --only functions --project staging",
    "deploy:production": "firebase deploy --only functions --project production"
  }
}
```

### Detect Emulator at Runtime (Extra Safety)

```js
const { defineString } = require('firebase-functions/params');

const useEmulator = defineString('USE_EMULATOR', { default: 'false' });

if (useEmulator.value() === 'true') {
  // point SDKs to local emulator
  admin.firestore().settings({ host: 'localhost:8080', ssl: false });
}
```

---

## 7. Keeping `"default"` in `.firebaserc`

If you keep `"default"` in `.firebaserc`, `.env.local` is still safe:

```bash
firebase emulators:start   # loads .env + .env.local
firebase deploy            # loads .env + .env.default  ← looks for this file, NOT .env.local
```

Firebase looks for `.env.default` on plain deploy — **not** `.env.local`.

**The only rule:** don't create a `.env.default` file, or treat it like any other deployed env file (no secrets).

---

## 8. `.gitignore` Setup

```bash
# .gitignore
.env.local
.env.*.local
```

> `.env.staging` and `.env.production` **can be committed** if they contain no secrets — only commit non-sensitive config values.

---

## Summary

### Environment File Loading

| Scenario | Command | Files Loaded |
|---|---|---|
| Local emulator | `npm run emulate` | `.env` + `.env.local` |
| Deploy dev | `npm run deploy:dev` | `.env` + `.env.dev` |
| Deploy staging | `npm run deploy:staging` | `.env` + `.env.staging` |
| Deploy production | `npm run deploy:production` | `.env` + `.env.production` |

### Config Type Guide

| Use Case | Tool |
|---|---|
| Sensitive secrets (API keys, tokens) | `defineSecret` + Secret Manager |
| Non-sensitive config (regions, flags) | `.env` files + `defineString` |
| Local dev / emulator overrides | `.env.local` |
| Per-environment config | `.env.production`, `.env.staging` |
| Legacy v1 (avoid) | `functions.config()` |

### Key Principles

- **`.env.local` is guaranteed emulator-only** — Firebase never loads it during deploy
- **Deploy explicitly** with `--project <alias>` — never rely on implicit default
- **Secrets and config are different** — Secret Manager for sensitive values, `.env` files for everything else
- **Never commit secrets** to any `.env` file
