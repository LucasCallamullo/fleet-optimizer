// src/features/auth/hooks/useAuth.js
import { useContext } from 'react';
import { AuthContext } from '@auth/context/AuthContext';

/**
 * Custom hook to access authentication context.
 * 
 * @returns {Object} Auth context value containing:
 *   - user: Current user data or null
 *   - loading: Boolean indicating if auth is being initialized
 *   - isAuthenticated: Boolean indicating if user is logged in
 *   - login: Function to authenticate user
 *   - logout: Function to logout user
 *   - hasRole: Function to check if user has a specific role
 *   - isAdmin: Function to check if user is an administrator
 * 
 * @throws {Error} If used outside of AuthProvider
 * 
 * @example
 * const { user, login, logout, isAuthenticated } = useAuth();
 */
export const useAuth = () => {
    const context = useContext(AuthContext);
    
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    
    return context;
};