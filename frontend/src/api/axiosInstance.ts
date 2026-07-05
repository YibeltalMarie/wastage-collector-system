import axios, { AxiosInstance, InternalAxiosRequestConfig, AxiosError } from 'axios';
import { getToken, getRefreshToken, setToken, clearTokens } from '@/utils/tokenStorage';

/**
 * The central HTTP client for the entire frontend.
 *
 * Every API call goes through this instance. Configure once, benefit everywhere.
 *
 * What it does automatically on every request:
 *   1. Attaches the JWT access token to the Authorization header
 *   2. On 401 (token expired): silently fetches a new token and retries
 *   3. On refresh failure: clears tokens and redirects to login
 *
 * This means every page/component can make API calls without thinking
 * about authentication at all — the interceptor handles it.
 */
const axiosInstance: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 10_000,
});

// ── Request interceptor ────────────────────────────────────────────────
// Runs before every outgoing request
axiosInstance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error: AxiosError) => Promise.reject(error)
);

// ── Response interceptor ───────────────────────────────────────────────
// Runs after every response (success or failure)
axiosInstance.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & {
      _retry?: boolean;
    };

    // If 401 and we have not already tried to refresh
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const refreshToken = getRefreshToken();
        if (!refreshToken) throw new Error('No refresh token');

        // Call refresh endpoint directly (not through axiosInstance — avoids loop)
        const { data } = await axios.post<{ accessToken: string }>(
          `${import.meta.env.VITE_API_BASE_URL}/api/auth/refresh`,
          { refreshToken }
        );

        // Store the new access token and retry the original request
        setToken(data.accessToken);
        originalRequest.headers.Authorization = `Bearer ${data.accessToken}`;
        return axiosInstance(originalRequest);

      } catch {
        // Refresh failed — session is dead, force logout
        clearTokens();
        window.location.href = '/login';
        return Promise.reject(error);
      }
    }

    return Promise.reject(error);
  }
);

export default axiosInstance;
