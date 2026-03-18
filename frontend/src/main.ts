import './style.css';
import { onAuthStateChanged } from 'firebase/auth';
import { auth } from './firebase';
import { signInGoogle, signInEmail, registerEmail, resetPassword, doSignOut } from './auth';
import { handleUser, setStatus, switchTab, copyToken, copyCurl } from './ui';

// ── Auth state ───────────────────────────────────────────────
onAuthStateChanged(auth, user => {
  if (user) {
    setStatus('ok', 'AUTHENTICATED', `Welcome, ${user.displayName ?? user.email}`);
    handleUser(user);
  } else {
    handleUser(null);
  }
});

// ── Tabs ────────────────────────────────────────────────────
document.querySelectorAll<HTMLButtonElement>('.tab').forEach(tab => {
  tab.addEventListener('click', () => switchTab(tab.dataset.tab!));
});

document.querySelectorAll<HTMLButtonElement>('[data-switch]').forEach(btn => {
  btn.addEventListener('click', () => switchTab(btn.dataset.switch!));
});

// ── Auth actions ─────────────────────────────────────────────
document.getElementById('btnGoogle')!.addEventListener('click', e => {
  signInGoogle(e.currentTarget as HTMLButtonElement);
});

document.getElementById('btnSignIn')!.addEventListener('click', e => {
  signInEmail(
    e.currentTarget as HTMLButtonElement,
    (document.getElementById('loginEmail') as HTMLInputElement).value.trim(),
    (document.getElementById('loginPassword') as HTMLInputElement).value,
  );
});

document.getElementById('loginPassword')!.addEventListener('keydown', e => {
  if (e.key === 'Enter') (document.getElementById('btnSignIn') as HTMLButtonElement).click();
});

document.getElementById('btnRegister')!.addEventListener('click', e => {
  registerEmail(
    e.currentTarget as HTMLButtonElement,
    (document.getElementById('regEmail') as HTMLInputElement).value.trim(),
    (document.getElementById('regPassword') as HTMLInputElement).value,
    (document.getElementById('regName') as HTMLInputElement).value.trim(),
  );
});

document.getElementById('btnReset')!.addEventListener('click', e => {
  resetPassword(
    e.currentTarget as HTMLButtonElement,
    (document.getElementById('resetEmail') as HTMLInputElement).value.trim(),
  );
});

document.getElementById('btnSignOut')!.addEventListener('click', doSignOut);

// ── Clipboard ────────────────────────────────────────────────
document.getElementById('copyBtn')!.addEventListener('click', copyToken);
document.getElementById('curlCopyBtn')!.addEventListener('click', copyCurl);