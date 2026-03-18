const BACKEND_URL = import.meta.env.VITE_BACKEND_URL ?? 'http://localhost:8080';

export interface UserProfile {
  uid: string;
  email: string | null;
  displayName: string | null;
}

export async function signInBackend(idToken: string): Promise<UserProfile> {
  const response = await fetch(`${BACKEND_URL}/auth/signin`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${idToken}`,
      'Content-Type': 'application/json',
    },
  });

  if (!response.ok) {
    throw new Error(`Backend /auth/signin failed: ${response.status}`);
  }

  return response.json() as Promise<UserProfile>;
}