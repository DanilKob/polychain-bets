const BACKEND_URL = import.meta.env.VITE_BACKEND_URL ?? 'http://localhost:8080';

export interface UserForInitResponse {
  uid: string;
  email: string | null;
  displayName: string | null;
}

export interface FeedDto {
  id: string;
  title: string;
  content: string;
}

export interface HomepageInitResponse {
  user: UserForInitResponse;
  feed: FeedDto[];
}

export async function fetchHomepageInit(idToken: string): Promise<HomepageInitResponse> {
  const response = await fetch(`${BACKEND_URL}/homepage/init`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${idToken}`,
      'Content-Type': 'application/json',
    },
  });

  if (!response.ok) {
    throw new Error(`Backend /homepage/init failed: ${response.status}`);
  }

  return response.json() as Promise<HomepageInitResponse>;
}