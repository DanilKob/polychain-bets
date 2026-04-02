import './style.css';
import Hls from 'hls.js';
import { onAuthStateChanged } from 'firebase/auth';
import { auth } from './firebase';
import { doSignOut } from './auth';
import { fetchHomepage, fetchFeed, fetchVideoToken, fetchWagerStats, BACKEND_URL, type FeedItem, type FeedResponse, type WagerStatsDto } from './api';

// ── State ─────────────────────────────────────────────────────────────────────

let idToken = '';
let nextCursor: string | null = null;
let isLoading = false;
let hasMore = true;

// ── Auth guard ────────────────────────────────────────────────────────────────

onAuthStateChanged(auth, async user => {
  if (!user) {
    window.location.href = '/index.html';
    return;
  }

  renderUserPill(user.displayName, user.photoURL);

  try {
    idToken = await user.getIdToken();
    const homepage = await fetchHomepage(idToken);
    renderUserPill(homepage.user.displayName, user.photoURL);
    await handleFeedResponse(homepage.feed);
  } catch (e: unknown) {
    console.error('Homepage load failed', e);
    hasMore = false;
    showEndMessage();
  }
});

document.getElementById('btnSignOut')!.addEventListener('click', async () => {
  await doSignOut();
  window.location.href = '/index.html';
});

// ── Bottom nav ────────────────────────────────────────────────────────────────

const navBtns = document.querySelectorAll<HTMLButtonElement>('.nav-btn');
navBtns.forEach(btn => {
  btn.addEventListener('click', () => {
    navBtns.forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
  });
});

// ── Pagination sentinel ───────────────────────────────────────────────────────

const paginationObserver = new IntersectionObserver(
  entries => {
    if (entries[0].isIntersecting && !isLoading && hasMore && idToken) {
      loadMore();
    }
  },
  { rootMargin: '200px' }
);

paginationObserver.observe(document.getElementById('feedSentinel')!);

async function loadMore(): Promise<void> {
  if (isLoading || !hasMore) return;
  isLoading = true;
  showSpinner(true);
  try {
    const response = await fetchFeed(idToken, nextCursor);
    await handleFeedResponse(response);
  } catch (e: unknown) {
    console.error('Feed load failed', e);
    hasMore = false;
    showEndMessage();
  } finally {
    isLoading = false;
    showSpinner(false);
  }
}

async function handleFeedResponse(response: FeedResponse): Promise<void> {
  await appendFeedItems(response.items);
  nextCursor = response.nextCursor;
  hasMore = response.hasMore;
  if (!hasMore) showEndMessage();
  if (document.getElementById('feedList')!.children.length === 0) {
    document.getElementById('feedEmpty')!.classList.add('visible');
  }
}

// ── Video autoplay observer ───────────────────────────────────────────────────
// Plays video when ≥50% visible, pauses when scrolled away

const videoObserver = new IntersectionObserver(
  entries => {
    entries.forEach(entry => {
      const video = entry.target as HTMLVideoElement;
      if (entry.isIntersecting) {
        video.play().catch(() => { /* autoplay blocked — fine, user can click */ });
      } else {
        video.pause();
      }
    });
  },
  { threshold: 0.5 }
);

// ── Stats observer — fetch + poll while card is visible ───────────────────────

const activePolls = new Map<string, number>();

const statsObserver = new IntersectionObserver(
  entries => {
    entries.forEach(entry => {
      const card = entry.target as HTMLElement;
      const wagerId = card.dataset.wagerId!;
      if (entry.isIntersecting) {
        fetchAndRenderStats(card, wagerId);
        const interval = window.setInterval(() => fetchAndRenderStats(card, wagerId), 5000);
        activePolls.set(wagerId, interval);
      } else {
        const interval = activePolls.get(wagerId);
        if (interval !== undefined) {
          clearInterval(interval);
          activePolls.delete(wagerId);
        }
      }
    });
  },
  { threshold: 0.8 }
);

async function fetchAndRenderStats(card: HTMLElement, wagerId: string): Promise<void> {
  try {
    const stats = await fetchWagerStats(idToken, wagerId);
    renderStats(card, stats);
  } catch { /* silently ignore — stale UI is acceptable */ }
}

function renderStats(card: HTMLElement, stats: WagerStatsDto): void {
  stats.outcomes.forEach(outcome => {
    const chip = card.querySelector<HTMLElement>(`[data-outcome-id="${outcome.outcomeId}"]`);
    if (!chip) return;
    const coef = chip.querySelector('.outcome-coefficient');
    const fill = chip.querySelector<HTMLElement>('.outcome-pool-fill');
    if (coef) coef.textContent = `${Number(outcome.coefficient).toFixed(2)}x`;
    if (fill) fill.style.width = `${outcome.poolSharePct}%`;
  });
}

// ── Render ────────────────────────────────────────────────────────────────────

function renderUserPill(name: string | null, photoURL: string | null): void {
  document.getElementById('userName')!.textContent = name ?? '—';
  const avatar = document.getElementById('userAvatar') as HTMLImageElement;
  if (photoURL) { avatar.src = photoURL; avatar.style.display = 'block'; }
}

function appendFeedItems(items: FeedItem[]): void {
  const list = document.getElementById('feedList')!;
  items.forEach(item => list.appendChild(buildCard(item, item.media.videoToken)));
}

function buildCard(item: FeedItem, videoToken: string | null): HTMLElement {
  const card = document.createElement('div');
  card.className = 'wager-card';
  card.dataset.id = item.id;
  card.dataset.wagerId = item.id;

  // ── Video box ──
  const videoBox = document.createElement('div');
  videoBox.className = 'wager-video-box';

  if (item.media.hlsUrl && videoToken) {
    const video = document.createElement('video');
    video.muted = true;
    video.loop = true;
    video.playsInline = true;
    video.poster = item.media.thumbnailUrl;

    attachHls(video, `${BACKEND_URL}${item.media.hlsUrl}`, item.media.videoId, videoToken);
    videoObserver.observe(video);

    // Click toggles mute, badge shows state
    const badge = document.createElement('div');
    badge.className = 'video-mute-badge';
    badge.textContent = '🔇 tap to unmute';

    video.addEventListener('click', () => {
      video.muted = !video.muted;
      badge.textContent = video.muted ? '🔇 tap to unmute' : '🔊 tap to mute';
    });

    videoBox.appendChild(video);
    videoBox.appendChild(badge);
  } else {
    videoBox.innerHTML = `<div class="wager-thumbnail-placeholder">No preview</div>`;
  }

  // ── Outcomes with live stats ──
  const outcomesEl = document.createElement('div');
  outcomesEl.className = 'wager-outcomes';
  item.outcomes.forEach(o => {
    const chip = document.createElement('div');
    chip.className = 'outcome-chip';
    chip.dataset.outcomeId = o.id;
    chip.innerHTML = `
      <div class="outcome-chip-row">
        <span class="outcome-description">${escHtml(o.description)}</span>
        <span class="outcome-coefficient">—</span>
      </div>
      <div class="outcome-pool-bar"><div class="outcome-pool-fill" style="width:0%"></div></div>
    `;
    outcomesEl.appendChild(chip);
  });

  const date = new Date(item.createdAt).toLocaleDateString('en-US', {
    month: 'short', day: 'numeric', year: 'numeric',
  });

  // ── Body ──
  const body = document.createElement('div');
  body.className = 'wager-body';
  body.innerHTML = `
    <div class="wager-name">${escHtml(item.name)}</div>
    <div class="wager-text">${escHtml(item.text)}</div>
    <div class="wager-meta">
      <span>${date}</span>
      <span class="wager-meta-dot">·</span>
      <span>${item.outcomes.length} outcome${item.outcomes.length !== 1 ? 's' : ''}</span>
      <span class="wager-meta-dot">·</span>
      <span>${formatDuration(item.media.durationSeconds)}</span>
    </div>
  `;
  body.insertAdjacentElement('afterbegin', outcomesEl);

  card.appendChild(videoBox);
  card.appendChild(body);
  statsObserver.observe(card);
  return card;
}

function attachHls(video: HTMLVideoElement, manifestUrl: string, videoId: string, initialToken: string): void {
  if (Hls.isSupported()) {
    const tokenHolder = { current: initialToken };

    const hls = new Hls({
      autoStartLoad: true,
      startLevel: -1,
      xhrSetup: (xhr, url) => {
        if (url.startsWith(BACKEND_URL)) {
          const separator = url.includes('?') ? '&' : '?';
          xhr.open('GET', `${url}${separator}vt=${encodeURIComponent(tokenHolder.current)}`, true);
        }
      },
    });

    hls.on(Hls.Events.ERROR, async (_event, data) => {
      if (data.response?.code === 403) {
        try {
          tokenHolder.current = (await fetchVideoToken(idToken, videoId)).token;
          data.fatal ? hls.loadSource(manifestUrl) : hls.startLoad();
        } catch {
          // token refresh failed — leave HLS stalled rather than error looping
        }
      }
    });

    hls.loadSource(manifestUrl);
    hls.attachMedia(video);
  } else if (video.canPlayType('application/vnd.apple.mpegurl')) {
    // Safari native HLS — append token directly to URL
    video.src = `${manifestUrl}?vt=${encodeURIComponent(initialToken)}`;
    // On network error, attempt a token refresh and reload
    video.onerror = async () => {
      try {
        const fresh = (await fetchVideoToken(idToken, videoId)).token;
        video.src = `${manifestUrl}?vt=${encodeURIComponent(fresh)}`;
        video.load();
      } catch { /* ignore */ }
    };
  }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

function formatDuration(seconds: number): string {
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return s > 0 ? `${m}m ${s}s` : `${m}m`;
}

function showSpinner(visible: boolean): void {
  document.getElementById('feedSpinner')!.classList.toggle('active', visible);
}

function showEndMessage(): void {
  document.getElementById('feedEndMsg')!.classList.add('visible');
}

function escHtml(str: string): string {
  return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}