import {
  signInWithPopup,
  GoogleAuthProvider,
  signInWithEmailAndPassword,
  createUserWithEmailAndPassword,
  sendPasswordResetEmail,
  sendEmailVerification,
  updateProfile,
  signOut,
} from 'firebase/auth';
import { auth } from './firebase';
import { setStatus, setLoading } from './ui';

const googleProvider = new GoogleAuthProvider();

const AUTH_ERRORS: Record<string, string> = {
  'auth/wrong-password':     'Incorrect password.',
  'auth/user-not-found':     'User not found.',
  'auth/invalid-email':      'Invalid email address.',
  'auth/too-many-requests':  'Too many attempts. Please wait.',
  'auth/invalid-credential': 'Invalid credentials.',
  'auth/email-already-in-use': 'Email already registered.',
  'auth/weak-password':      'Password is too weak (min. 6 characters).',
  'auth/error-code:-47':     'Registration is currently unavailable. Please try again later.',
};

function resolveAuthError(e: unknown): string {
  const err = e as { code?: string; message: string };
  return AUTH_ERRORS[err.code ?? ''] ?? err.message;
}

export async function signInGoogle(btn: HTMLButtonElement): Promise<void> {
  setLoading(btn, true);
  try {
    await signInWithPopup(auth, googleProvider);
    window.location.href = '/home.html';
  } catch (e: unknown) {
    setStatus('error', 'GOOGLE ERROR', resolveAuthError(e));
    setLoading(btn, false);
  }
}

export async function signInEmail(btn: HTMLButtonElement, email: string, password: string): Promise<void> {
  if (!email || !password) { setStatus('error', 'VALIDATION', 'Please fill in email and password.'); return; }
  setLoading(btn, true);
  try {
    await signInWithEmailAndPassword(auth, email, password);
    window.location.href = '/home.html';
  } catch (e: unknown) {
    setStatus('error', 'LOGIN ERROR', resolveAuthError(e));
  } finally {
    setLoading(btn, false);
  }
}

export async function registerEmail(
  btn: HTMLButtonElement,
  email: string,
  password: string,
  name: string
): Promise<void> {
  if (!email || !password) { setStatus('error', 'VALIDATION', 'Please fill in email and password.'); return; }
  setLoading(btn, true);
  try {
    const result = await createUserWithEmailAndPassword(auth, email, password);
    if (name) await updateProfile(result.user, { displayName: name });
    await sendEmailVerification(result.user);
    setStatus('ok', 'REGISTERED', `Account created: ${result.user.email}. Verification email sent.`);
  } catch (e: unknown) {
    setStatus('error', 'REGISTER ERROR', resolveAuthError(e));
  } finally {
    setLoading(btn, false);
  }
}

export async function resetPassword(btn: HTMLButtonElement, email: string): Promise<void> {
  if (!email) { setStatus('error', 'VALIDATION', 'Please enter your email.'); return; }
  setLoading(btn, true);
  try {
    await sendPasswordResetEmail(auth, email);
    setStatus('info', 'EMAIL SENT', `Email sent to ${email}. Check your inbox (and spam folder).`);
  } catch (e: unknown) {
    setStatus('error', 'RESET ERROR', resolveAuthError(e));
  } finally {
    setLoading(btn, false);
  }
}

export async function doSignOut(): Promise<void> {
  await signOut(auth);
  setStatus('idle', 'SIGNED OUT', 'You have signed out.');
}