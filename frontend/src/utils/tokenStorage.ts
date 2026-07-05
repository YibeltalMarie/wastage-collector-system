/**
 * Token storage strategy:
 *
 * Access token  → memory only (most secure, cleared on tab close/refresh)
 * Refresh token → sessionStorage (survives page refresh, cleared on tab close)
 *
 * Why not localStorage?
 * localStorage persists forever and is vulnerable to XSS attacks.
 * Any injected script can read it. Memory is safer for short-lived tokens.
 *
 * Production recommendation: use httpOnly cookies for refresh tokens.
 * That requires backend changes (Set-Cookie header) and is a v2 improvement.
 */

let inMemoryAccessToken: string | null = null;

export const setToken = (token: string): void => {
  inMemoryAccessToken = token;
};

export const getToken = (): string | null => inMemoryAccessToken;

export const setRefreshToken = (token: string): void => {
  sessionStorage.setItem('wc_refresh', token);
};

export const getRefreshToken = (): string | null => {
  return sessionStorage.getItem('wc_refresh');
};

export const clearTokens = (): void => {
  inMemoryAccessToken = null;
  sessionStorage.removeItem('wc_refresh');
};
