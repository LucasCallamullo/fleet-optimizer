// src/features/auth/hooks/useAuth.ts
import { useContext } from 'react';
import { AuthContext, type AuthContextType } from '@features/auth/context/AuthContext';

/**
 * Custom hook to access authentication context.
 * 
 * @returns {AuthContextType} Authentication state and methods
 * @throws {Error} If used outside of AuthProvider
 */
export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }

  return context;
};