/* eslint-disable react-refresh/only-export-components */
import { createContext, useState, useEffect, type ReactNode } from 'react';
import authService from '@features/auth/services/authService';
import type { User, AuthOperationResult } from '@features/auth/types/authTypes';
import api from '@shared/api/client';

// ================================================================
// 1. INTERFACES & TYPES
// ================================================================

/**
 * Interface defining the shared authentication state and actions
 */
export interface AuthContextType {
  user: User | null;
  loading: boolean;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<AuthOperationResult>;
  logout: () => Promise<void>;
  hasRole: (role: string) => boolean;
  isAdmin: () => boolean;
}

interface AuthProviderProps {
  children: ReactNode;
}

// ================================================================
// 2. CREATE CONTEXT
// ================================================================
export const AuthContext = createContext<AuthContextType | undefined>(undefined);

// ================================================================
// 3. AUTH PROVIDER COMPONENT
// ================================================================
export const AuthProvider = ({ children }: AuthProviderProps) => {
  // ------------------------------------------------------------
  // 3.1 GLOBAL STATES
  // ------------------------------------------------------------
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);

  // ------------------------------------------------------------
  // 3.2 INITIAL MOUNT EFFECT: Session Restoration
  // ------------------------------------------------------------
  useEffect(() => {
    const initializeAuth = () => {
      // Retrieve persistent user state and bearer token on app bootstrap
      const currentUser = authService.getCurrentUser();
      const token = authService.getToken();

      if (currentUser && token) {
        setUser(currentUser);
        setIsAuthenticated(true);
        api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      }

      setLoading(false);
    };

    initializeAuth();
  }, []);

  // ------------------------------------------------------------
  // 3.3 LOGIN ACTION
  // ------------------------------------------------------------
  const login = async (email: string, password: string): Promise<AuthOperationResult> => {
    // Dispatch credentials using the new object-based payload signature
    const result = await authService.login({ email, password });

    if (result.success && result.user) {
      setUser(result.user);
      setIsAuthenticated(true);
      return { success: true, user: result.user };
    }

    return { success: false, message: result.message };
  };

  // ------------------------------------------------------------
  // 3.4 LOGOUT ACTION
  // ------------------------------------------------------------
  const logout = async (): Promise<void> => {
    await authService.logout();
    setUser(null);
    setIsAuthenticated(false);
  };

  // ------------------------------------------------------------
  // 3.5 ROLE CHECK HELPER FUNCTIONS
  // ------------------------------------------------------------
  
  /**
   * Checks if current user possesses a specific role string
   */
  const hasRole = (role: string): boolean => {
    if (!user?.roles || !Array.isArray(user.roles)) return false;
    return user.roles.includes(role);
  };

  /**
   * Verifies if current user holds administrator privileges
   */
  const isAdmin = (): boolean => {
    if (!user?.roles || !Array.isArray(user.roles)) return false;
    const adminRoles = ['admin', 'role_admin', 'administrador'];
    return user.roles.some((r) => adminRoles.includes(r.toLowerCase()));
  };

  // ------------------------------------------------------------
  // 3.6 CONTEXT VALUE BINDING
  // ------------------------------------------------------------
  const value: AuthContextType = {
    user,
    loading,
    isAuthenticated,
    login,
    logout,
    hasRole,
    isAdmin,
  };

  // ------------------------------------------------------------
  // 3.7 RENDER PROVIDER
  // ------------------------------------------------------------
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};