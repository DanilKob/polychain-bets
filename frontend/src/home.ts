import './style.css';
import { onAuthStateChanged } from 'firebase/auth';
import { auth } from './firebase';
import { doSignOut } from './auth';
import { signInBackend, type UserProfile } from './api';

onAuthStateChanged(auth, async user => {
  if (!user) {
    window.location.href = '/index.html';
    return;
  }

  renderUser(user.displayName, user.email, user.photoURL);

  try {
    const token   = await user.getIdToken();
    const profile = await signInBackend(token);
    renderProfile(profile);
  } catch (e: unknown) {
    showError((e as Error).message);
  }
});

document.getElementById('btnSignOut')!.addEventListener('click', async () => {
  await doSignOut();
  window.location.href = '/index.html';
});

function renderUser(name: string | null, email: string | null, photo: string | null): void {
  const avatar = document.getElementById('userAvatar') as HTMLImageElement;
  const userName  = document.getElementById('userName')!;
  const userEmail = document.getElementById('userEmail')!;

  if (photo) { avatar.src = photo; avatar.style.display = 'block'; }
  userName.textContent  = name  ?? '—';
  userEmail.textContent = email ?? '—';
}

function renderProfile(profile: UserProfile): void {
  const block = document.getElementById('profileBlock')!;
  block.innerHTML = `
    <div class="info-row"><span class="info-key">UID</span><span class="info-val">${profile.uid}</span></div>
    <div class="info-row"><span class="info-key">Email</span><span class="info-val">${profile.email ?? '—'}</span></div>
    <div class="info-row"><span class="info-key">Name</span><span class="info-val">${profile.displayName ?? '—'}</span></div>
  `;
  document.getElementById('backendStatus')!.className = 'status-card status-ok';
  document.getElementById('backendStatus')!.innerHTML =
    '<div class="status-label">BACKEND OK</div><div class="status-message">Profile loaded from Spring Boot /auth/signin</div>';
}

function showError(message: string): void {
  document.getElementById('backendStatus')!.className = 'status-card status-error';
  document.getElementById('backendStatus')!.innerHTML =
    `<div class="status-label">BACKEND ERROR</div><div class="status-message">${message}</div>`;
}