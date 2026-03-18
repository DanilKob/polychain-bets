import { beforeUserCreated } from "firebase-functions/v2/identity";
import axios from "axios";

const BACKEND_URL = process.env.BACKEND_URL ?? "http://localhost:8080";

export const processNewUser = beforeUserCreated({ secrets: ["INTERNAL_SECRET"] }, async (event) => {
    const user = event.data;
    if (!user) return;

    // Strip sensitive fields before sending to backend
    const { credential, ...safeEventContext } = event as any;
    const { passwordHash, passwordSalt, ...safeUser } = user as any;

    try {
        await axios.post(
            `${BACKEND_URL}/firebase/users`,
            { ...safeEventContext, data: safeUser },
            {
                headers: { "X-Internal-Secret": process.env.INTERNAL_SECRET },
                timeout: 5000,
            }
        );
    } catch (err) {
        // Log but don't block registration — /auth/signin will upsert as fallback
        console.error("Failed to sync new user to backend:", err);
    }
});
