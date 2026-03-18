"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.processNewUser = void 0;
const identity_1 = require("firebase-functions/v2/identity");
const axios_1 = require("axios");
const BACKEND_URL = process.env.BACKEND_URL ?? "http://localhost:8080";
exports.processNewUser = (0, identity_1.beforeUserCreated)({ secrets: ["INTERNAL_SECRET"] }, async (event) => {
    const user = event.data;
    if (!user)
        return;
    // Strip sensitive fields before sending to backend
    const { credential, ...safeEventContext } = event;
    const { passwordHash, passwordSalt, ...safeUser } = user;
    try {
        await axios_1.default.post(`${BACKEND_URL}/firebase/users`, { ...safeEventContext, data: safeUser }, {
            headers: { "X-Internal-Secret": process.env.INTERNAL_SECRET },
            timeout: 5000,
        });
    }
    catch (err) {
        // Log but don't block registration — /auth/signin will upsert as fallback
        console.error("Failed to sync new user to backend:", err);
    }
});
//# sourceMappingURL=index.js.map