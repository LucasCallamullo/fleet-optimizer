/* eslint-disable react-refresh/only-export-components */
import React, { createContext, useState, useEffect } from 'react';
import authService from '@/features/auth/services/authService';
import api from '@/shared/api/client';

// ================================================================
// 1. CREATE CONTEXT
// ================================================================
const AuthContext = createContext(null);

// ================================================================
// 2. EXPORT CONTEXT (for the hook)
// ================================================================
export { AuthContext };

// ================================================================
// 3. AUTH PROVIDER COMPONENT
// ================================================================
export const AuthProvider = ({ children }) => {

    // ------------------------------------------------------------
    // 3.1 GLOBAL STATES
    // ------------------------------------------------------------
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [isAuthenticated, setIsAuthenticated] = useState(false);

    // ------------------------------------------------------------
    // 3.2 INITIAL MOUNT EFFECT: Session Restoration
    // ------------------------------------------------------------
    useEffect(() => {
        const initializeAuth = () => { 
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
    const login = async (email, password) => {
        const result = await authService.login(email, password);

        if (result.success) {
            setUser(result.user);
            setIsAuthenticated(true);
            return { success: true };
        }

        return { success: false, message: result.message };
    };

    // ------------------------------------------------------------
    // 3.4 LOGOUT ACTION
    // ------------------------------------------------------------
    const logout = async () => {
        await authService.logout();
        setUser(null);
        setIsAuthenticated(false);
    };

    // ------------------------------------------------------------
    // 3.5 ROLE CHECK HELPER FUNCTIONS
    // ------------------------------------------------------------
    
    /**
     * Checks if current user possesses a specific role string.
     * Supports checking both Spanish and English role naming conventions.
     * 
     * @param {string} role - Target role to verify (e.g., 'admin', 'user', 'usuario')
     * @returns {boolean}
     */
    const hasRole = (role) => {
        if (!user?.roles || !Array.isArray(user.roles)) return false;
        return user.roles.includes(role);
    };

    /**
     * Verifies if the user holds Administrator privileges.
     * Handles common admin role names ('admin', 'ROLE_ADMIN', 'administrador').
     * 
     * @returns {boolean}
     */
    const isAdmin = () => {
        if (!user?.roles || !Array.isArray(user.roles)) return false;
        const adminRoles = ['admin', 'ROLE_ADMIN', 'administrador'];
        return user.roles.some((r) => adminRoles.includes(r.toLowerCase()));
    };

    // ------------------------------------------------------------
    // 3.6 CONTEXT VALUE BINDING
    // ------------------------------------------------------------
    const value = {
        user,
        loading,
        isAuthenticated,
        login,
        logout,
        hasRole,
        isAdmin
    };

    // ------------------------------------------------------------
    // 3.7 RENDER PROVIDER
    // ------------------------------------------------------------
    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};