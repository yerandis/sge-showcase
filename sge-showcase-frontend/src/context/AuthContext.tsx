import {
  createContext,
  useContext,
  useState,
  useEffect,
  useCallback,
  type ReactNode,
} from "react";
import type { AuthUser, LoginCredentials } from "../types/auth.types";
import {
  login as loginService,
  logout as logoutService,
  getSavedUser,
  isSessionActive,
  isTokenExpired,
  getAccessToken,
  clearSession,
  updateSavedUser,
} from "../services/authService";

interface AuthContextType {
  user: AuthUser | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  updateUser: (updates: Partial<AuthUser>) => void;
  login: (credentials: LoginCredentials) => Promise<void>;
  logout: () => Promise<void>;
  /** Verifica si el usuario tiene un permiso granular. Usar usePermissions() hook en componentes. */
  can: (permission: string) => boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const token = getAccessToken();

    // Si hay token pero está expirado, limpiar sesión antes de mostrar la app
    if (token && isTokenExpired(token)) {
      clearSession();
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setIsLoading(false);
      return;
    }

    if (isSessionActive()) {
      setUser(getSavedUser());
    }
    setIsLoading(false);
  }, []);

  const login = useCallback(async (credentials: LoginCredentials) => {
    const response = await loginService(credentials);
    setUser(response.user);
  }, []);

  const logout = useCallback(async () => {
    await logoutService();
    setUser(null);
  }, []);

  // Sustituye hasRole — ahora el sistema es por permisos granulares
  const can = useCallback(
    (permission: string): boolean => {
      if (!user) return false;
      return user.permissions.includes(permission);
    },
    [user],
  );

  const updateUser = useCallback((updates: Partial<AuthUser>) => {
    const saved = getSavedUser();

    if (!saved) return;

    const updated = { ...saved, ...updates };

    updateSavedUser(updated);
    setUser(updated);
  }, []);

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: Boolean(user),
        isLoading,
        updateUser,
        login,
        logout,
        can,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth(): AuthContextType {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth debe usarse dentro de <AuthProvider>");
  return ctx;
}
