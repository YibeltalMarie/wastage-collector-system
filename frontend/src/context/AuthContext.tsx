import {
  createContext,
  useContext,
  useState,
  useCallback,
  ReactNode,
} from 'react';
import { AuthUser, AuthContextType } from '@/types/auth.types';
import { setToken, setRefreshToken, clearTokens } from '@/utils/tokenStorage';

const AuthContext = createContext<AuthContextType | null>(null);

interface AuthProviderProps {
  children: ReactNode;
}

/**
 * AuthProvider wraps the entire application.
 * It holds the logged-in user state and provides login/logout functions.
 *
 * Any component can call useAuth() to:
 *   - Read the current user: const { user } = useAuth()
 *   - Check the role:       if (user?.role === 'ADMIN')
 *   - Log out:              const { logout } = useAuth()
 */
export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(false);

  const login = useCallback(
    (userData: AuthUser, accessToken: string, refreshToken: string): void => {
      setToken(accessToken);
      setRefreshToken(refreshToken);
      setUser(userData);
    },
    []
  );

  const logout = useCallback(async (): Promise<void> => {
    setIsLoading(true);
    try {
      // Tell the server to revoke the refresh token
      const { logoutApi } = await import('@/api/authApi');
      await logoutApi();
    } catch {
      // Even if the API call fails, clear local state
      // The refresh token will expire naturally on the server
    } finally {
      clearTokens();
      setUser(null);
      setIsLoading(false);
    }
  }, []);

  return (
    <AuthContext.Provider value={{ user, isLoading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

/**
 * useAuth — the hook every component uses to access auth state.
 *
 * Usage:
 *   const { user, login, logout } = useAuth();
 *
 * Throws if used outside AuthProvider — catches missing wrapper early.
 */
export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used inside <AuthProvider>');
  }
  return context;
}
