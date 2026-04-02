export const BACKEND_URL = import.meta.env.VITE_BACKEND_URL ?? 'http://localhost:8088';

// ── User ─────────────────────────────────────────────────────────────────────

export interface UserResponse {
  uid: string;
  email: string | null;
  displayName: string | null;
}

// ── Feed ─────────────────────────────────────────────────────────────────────

export interface OutcomeDto {
  id: string;
  wagerId: string;
  description: string;
}

export interface MediaDto {
  id: string;
  videoId: string;
  videoToken: string;
  thumbnailUrl: string;
  previewUrl: string;
  hlsUrl: string;
  durationSeconds: number;
  width: number;
  height: number;
}

export interface FeedItem {
  id: string;
  name: string;
  text: string;
  createdAt: string;
  media: MediaDto;
  outcomes: OutcomeDto[];
}

export interface FeedResponse {
  items: FeedItem[];
  nextCursor: string | null;
  hasMore: boolean;
  count: number;
}

// ── Homepage ──────────────────────────────────────────────────────────────────

export interface HomepageResponse {
  user: UserResponse;
  feed: FeedResponse;
}

// ── Requests ──────────────────────────────────────────────────────────────────

async function authorizedGet<T>(path: string, token: string): Promise<T> {
  const res = await fetch(`${BACKEND_URL}${path}`, {
    headers: { 'Authorization': `Bearer ${token}` },
  });
  if (!res.ok) throw new Error(`${path} failed: ${res.status}`);
  return res.json() as Promise<T>;
}

export async function syncUser(token: string): Promise<UserResponse> {
  const res = await fetch(`${BACKEND_URL}/auth/signin`, {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
  });
  if (!res.ok) throw new Error(`/auth/signin failed: ${res.status}`);
  return res.json() as Promise<UserResponse>;
}

export async function fetchHomepage(token: string): Promise<HomepageResponse> {
  return authorizedGet<HomepageResponse>('/api/v1/homepage', token);
}

export async function fetchFeed(token: string, cursor?: string | null, limit = 5): Promise<FeedResponse> {
  const params = new URLSearchParams({ limit: String(limit) });
  if (cursor) params.set('cursor', cursor);
  return authorizedGet<FeedResponse>(`/api/v1/feed?${params}`, token);
}

export interface VideoTokenResponse {
  token: string;
  expirySeconds: number;
}

export async function fetchVideoToken(idToken: string, videoId: string): Promise<VideoTokenResponse> {
  return authorizedGet<VideoTokenResponse>(`/api/v1/video/${videoId}/token`, idToken);
}

// ── Wager stats ───────────────────────────────────────────────────────────────

export interface OutcomeStatsDto {
  outcomeId: string;
  pool: number;
  voterCount: number;
  coefficient: number;
  poolSharePct: number;
}

export interface WagerStatsDto {
  wagerId: string;
  totalPool: number;
  voterCount: number;
  outcomes: OutcomeStatsDto[];
  updatedAt: string;
}

export async function fetchWagerStats(token: string, wagerId: string): Promise<WagerStatsDto> {
  return authorizedGet<WagerStatsDto>(`/api/v1/wager/${wagerId}/stats`, token);
}