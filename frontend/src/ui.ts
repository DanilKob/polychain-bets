import type { User } from 'firebase/auth';

export type StatusType = 'ok' | 'error' | 'info' | 'idle';

export function setStatus(type: StatusType, label: string, message: string): void {
  const card   = document.getElementById('statusCard')!;
  const header = document.getElementById('statusHeader')!;
  card.className = `status-card status-${type}`;
  card.innerHTML = `<div class="status-label">${label}</div><div class="status-message">${message}</div>`;
  header.className = `panel-header ${type === 'ok' ? 'green' : type === 'error' ? 'red' : ''}`;
  header.textContent = 'Status';
}

export function setLoading(btn: HTMLButtonElement, loading: boolean): void {
  btn.classList.toggle('loading', loading);
  btn.disabled = loading;
}

export async function handleUser(user: User | null): Promise<void> {
  const userInfoBlock = document.getElementById('userInfoBlock')!;
  const curlBlock     = document.getElementById('curlBlock')!;
  const tokenHeader   = document.getElementById('tokenHeader')!;

  if (!user) {
    userInfoBlock.style.display = 'none';
    curlBlock.style.display     = 'none';
    tokenHeader.className       = 'panel-header';
    setTokenEmpty();
    return;
  }

  const token     = await user.getIdToken(true);
  const providers = user.providerData.map(p => p.providerId).join(', ');

  userInfoBlock.style.display = 'block';
  document.getElementById('userInfoTable')!.innerHTML = `
    <div class="info-row"><span class="info-key">UID</span><span class="info-val">${user.uid}</span></div>
    <div class="info-row"><span class="info-key">Email</span><span class="info-val">${user.email ?? '—'}</span></div>
    <div class="info-row"><span class="info-key">Name</span><span class="info-val">${user.displayName ?? '—'}</span></div>
    <div class="info-row"><span class="info-key">Provider</span><span class="info-val">${providers}</span></div>
    <div class="info-row"><span class="info-key">Verified</span><span class="info-val ${user.emailVerified ? 'verified' : 'unverified'}">${user.emailVerified ? '✓ YES' : '✗ NO'}</span></div>
  `;

  const tv = document.getElementById('tokenValue')!;
  tv.textContent = token;
  tv.classList.remove('empty');
  tokenHeader.className = 'panel-header green';

  curlBlock.style.display = 'block';
  renderCurl(token);
}

function setTokenEmpty(): void {
  const tv = document.getElementById('tokenValue')!;
  tv.textContent = '— token will appear after authentication —';
  tv.classList.add('empty');
}

let curlFullToken = '';

function renderCurl(token: string): void {
  curlFullToken = token;
  document.getElementById('curlCode')!.innerHTML =
    `<span class="kw">curl</span> -X POST \\\n` +
    `  <span class="str">http://localhost:8080/homepage/init</span> \\\n` +
    `  -H <span class="str">"Authorization: Bearer <span class="var">${token.substring(0, 40)}...</span>"</span> \\\n` +
    `  -H <span class="str">"Content-Type: application/json"</span>`;
}

export function copyToken(): void {
  const val = document.getElementById('tokenValue')!.textContent ?? '';
  if (val.startsWith('—')) return;
  navigator.clipboard.writeText(val);
  flashCopied(document.getElementById('copyBtn')!);
}

export function copyCurl(): void {
  if (!curlFullToken) return;
  const full = `curl -X POST \\\n  http://localhost:8080/homepage/init \\\n  -H "Authorization: Bearer ${curlFullToken}" \\\n  -H "Content-Type: application/json"`;
  navigator.clipboard.writeText(full);
  flashCopied(document.getElementById('curlCopyBtn')!);
}

function flashCopied(btn: HTMLElement): void {
  btn.textContent = '✓ Copied';
  btn.classList.add('copied');
  setTimeout(() => { btn.textContent = 'Copy'; btn.classList.remove('copied'); }, 2000);
}

export function switchTab(name: string): void {
  document.querySelectorAll<HTMLButtonElement>('.tab').forEach(t => {
    t.classList.toggle('active', t.dataset.tab === name);
  });
  document.querySelectorAll<HTMLElement>('.view').forEach(v => v.classList.remove('active'));
  document.getElementById(`view-${name}`)!.classList.add('active');
}