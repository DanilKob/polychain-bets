# `beforeUserCreated` Event Structure

Reference for all fields available in the `processNewUser` blocking function.
Use this to decide which fields to forward to the backend via `POST /firebase/users`.

```typescript
export const processNewUser = beforeUserCreated({ secrets: ["INTERNAL_SECRET"] }, async (event) => {
    const user = event.data;   // AuthUserRecord | undefined
    // event.ipAddress, event.userAgent, event.additionalUserInfo, event.credential ...
});
```

---

## `event` — AuthBlockingEvent

| Field | Type | Description |
|---|---|---|
| `event.data` | `AuthUserRecord \| undefined` | The user being created. Always check for `undefined`. |
| `event.locale` | `string \| undefined` | BCP-47 locale from the client (e.g. `"en-US"`). |
| `event.ipAddress` | `string` | IP address of the registering client. |
| `event.userAgent` | `string` | User-Agent string of the registering client. |
| `event.additionalUserInfo` | `AdditionalUserInfo \| undefined` | Extra info from the identity provider (see below). |
| `event.credential` | `Credential \| undefined` | OAuth credential used to sign in (see below). |
| `event.emailType` | `"EMAIL_SIGN_IN" \| "PASSWORD_RESET" \| undefined` | Set for email link flows. |
| `event.smsType` | `"SIGN_IN_OR_SIGN_UP" \| "MULTI_FACTOR_SIGN_IN" \| "MULTI_FACTOR_ENROLLMENT" \| undefined` | Set for SMS flows. |

---

## `event.data` — AuthUserRecord

### Core identity

| Field | Type | Notes |
|---|---|---|
| `uid` | `string` | Firebase user ID. Always present. |
| `email` | `string \| undefined` | Primary email. Not set for phone-only or anonymous users. |
| `emailVerified` | `boolean` | `true` for Google/Apple (verified by provider). `false` for fresh email/password accounts. |
| `displayName` | `string \| undefined` | Display name. Set by Google/Apple on first registration. Not set for email/password unless the client calls `updateProfile`. |
| `photoURL` | `string \| undefined` | Profile photo URL from the identity provider. |
| `phoneNumber` | `string \| undefined` | E.164 format (e.g. `"+12125551234"`). Set for phone auth. |
| `disabled` | `boolean` | Whether the account is disabled. Always `false` on creation. |

### Provider info

| Field | Type | Notes |
|---|---|---|
| `providerData` | `AuthUserInfo[]` | One entry per linked provider. For new users this has exactly one element. |
| `providerData[0].providerId` | `string` | `"google.com"`, `"apple.com"`, `"password"`, `"phone"`, etc. **This is what the current code uses.** |
| `providerData[0].uid` | `string` | User ID from the provider (e.g. Google's subject ID). |
| `providerData[0].displayName` | `string` | Display name from the provider. |
| `providerData[0].email` | `string` | Email from the provider. |
| `providerData[0].photoURL` | `string` | Photo URL from the provider. |
| `providerData[0].phoneNumber` | `string` | Phone number from the provider. |

### Metadata

| Field | Type | Notes |
|---|---|---|
| `metadata.creationTime` | `string` | UTC string — when the account was created. |
| `metadata.lastSignInTime` | `string` | UTC string — same as `creationTime` for new users. |
| `tokensValidAfterTime` | `string \| undefined` | UTC string — tokens issued before this time are invalid. |
| `tenantId` | `string \| null \| undefined` | Only relevant for multi-tenant (GCIP) setups. Always `null` in standard projects. |

### Advanced

| Field | Type | Notes |
|---|---|---|
| `customClaims` | `Record<string, any> \| undefined` | Custom claims set via Admin SDK. Empty on first creation. |
| `passwordHash` | `string \| undefined` | Base64 hashed password. Only available for email/password accounts and only if your project has hash export enabled. |
| `passwordSalt` | `string \| undefined` | Base64 password salt. Same availability as `passwordHash`. |
| `multiFactor` | `AuthMultiFactorSettings \| undefined` | MFA enrollment info. Rarely set at creation time. |

---

## `event.additionalUserInfo` — AdditionalUserInfo

Extra data from the OAuth provider. Available for Google and Apple sign-ins.

| Field | Type | Notes |
|---|---|---|
| `providerId` | `string \| undefined` | `"google.com"`, `"apple.com"`, etc. |
| `isNewUser` | `boolean` | Always `true` in `beforeUserCreated`. |
| `profile` | `any \| undefined` | Raw provider profile object. For Google this contains `name`, `picture`, `locale`, `given_name`, `family_name`, etc. |
| `username` | `string \| undefined` | GitHub/Twitter username if applicable. |
| `email` | `string \| undefined` | Email from the provider profile. |
| `phoneNumber` | `string \| undefined` | Phone number from the provider. |
| `recaptchaScore` | `number \| undefined` | reCAPTCHA Enterprise score (0.0–1.0). Requires App Check. |

### Google `profile` example
```json
{
  "given_name": "Ivan",
  "family_name": "Petrov",
  "picture": "https://lh3.googleusercontent.com/...",
  "locale": "en",
  "email": "ivan@gmail.com",
  "email_verified": true
}
```

---

## `event.credential` — Credential

OAuth tokens from the provider. Only present for OAuth sign-ins (Google, Apple).

| Field | Type | Notes |
|---|---|---|
| `providerId` | `string` | `"google.com"`, `"apple.com"`, etc. |
| `signInMethod` | `string` | Sign-in method used. |
| `idToken` | `string \| undefined` | Provider's ID token (e.g. Google ID token). |
| `accessToken` | `string \| undefined` | Provider's access token. |
| `refreshToken` | `string \| undefined` | Provider's refresh token. |
| `expirationTime` | `string \| undefined` | Token expiry as UTC string. |
| `secret` | `string \| undefined` | OAuth 1.0 token secret (Twitter). |
| `claims` | `Record<string, any> \| undefined` | Decoded claims from the provider token. |

---

## `BeforeCreateResponse` — what you can return

`beforeUserCreated` can modify or block the user before they are created. Return an object or `void`.

| Field | Type | Effect |
|---|---|---|
| `displayName` | `string` | Override the display name stored in Firebase. |
| `photoURL` | `string` | Override the photo URL. |
| `emailVerified` | `boolean` | Override email verification status. |
| `disabled` | `boolean` | Set to `true` to block the registration entirely. |
| `customClaims` | `object` | Set custom claims on the new user's token immediately. |

To block a registration, throw a `HttpsError` instead of returning `{ disabled: true }`:

```typescript
import { HttpsError } from "firebase-functions/v2/https";
throw new HttpsError("permission-denied", "Registration not allowed.");
```

---

## Fields currently sent to the backend

```typescript
// index.ts — current payload
{
    uid:         user.uid,
    email:       user.email ?? null,
    displayName: user.displayName ?? null,
    provider:    user.providerData?.[0]?.providerId ?? "unknown"
}
```

## Suggested additional fields

| Field | Source | Usefulness |
|---|---|---|
| `photoURL` | `user.photoURL` | Store avatar URL without a separate API call |
| `phoneNumber` | `user.phoneNumber` | Useful for phone-auth users |
| `locale` | `event.locale` | User's locale for localised notifications |
| `ipAddress` | `event.ipAddress` | Fraud detection / geo-restriction |
| `givenName` / `familyName` | `event.additionalUserInfo?.profile?.given_name` | Granular name storage (Google only) |
| `providerUid` | `user.providerData?.[0]?.uid` | Provider-specific ID for account linking |

## Example

```json
{
  "locale" : "und",
  "ipAddress" : "62.4.55.59",
  "userAgent" : "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36,gzip(gfe),gzip(gfe)",
  "eventId" : "W1etKNCEJjUHht5MCmEeNA",
  "eventType" : "providers/cloud.auth/eventTypes/user.beforeCreate:google.com",
  "authType" : "USER",
  "resource" : {
    "service" : "identitytoolkit.googleapis.com",
    "name" : "projects/auth-poluchain"
  },
  "timestamp" : "Tue, 17 Mar 2026 22:17:50 GMT",
  "additionalUserInfo" : {
    "providerId" : "google.com",
    "profile" : {
      "name" : "Danylo Kobzar",
      "granted_scopes" : "https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile openid",
      "id" : "105577728002694059667",
      "verified_email" : true,
      "given_name" : "Danylo",
      "family_name" : "Kobzar",
      "email" : "kobzardanila@gmail.com",
      "picture" : "https://lh3.googleusercontent.com/a/ACg8ocKR8pq5MwMgBfLnKSHyMibmGQ1kWKMu2wzh9Fqu-2bf0oTqSw=s96-c"
    },
    "isNewUser" : true
  },
  "params" : { },
  "data" : {
    "uid" : "HaJh1h0iDHScQngq6AoqkYpUjQv1",
    "email" : "kobzardanila@gmail.com",
    "emailVerified" : true,
    "displayName" : "Danylo Kobzar",
    "photoURL" : "https://lh3.googleusercontent.com/a/ACg8ocKR8pq5MwMgBfLnKSHyMibmGQ1kWKMu2wzh9Fqu-2bf0oTqSw=s96-c",
    "disabled" : false,
    "metadata" : {
      "creationTime" : "Tue, 17 Mar 2026 22:17:50 GMT",
      "lastSignInTime" : "Tue, 17 Mar 2026 22:17:50 GMT"
    },
    "providerData" : [ {
      "uid" : "105577728002694059667",
      "displayName" : "Danylo Kobzar",
      "email" : "kobzardanila@gmail.com",
      "photoURL" : "https://lh3.googleusercontent.com/a/ACg8ocKR8pq5MwMgBfLnKSHyMibmGQ1kWKMu2wzh9Fqu-2bf0oTqSw=s96-c",
      "providerId" : "google.com"
    } ],
    "tokensValidAfterTime" : null,
    "multiFactor" : null
  }
}
```