/**
 * ================================================================
 * AUTHENTICATION SERVICE
 * ================================================================
 * 
 * Handles HTTP requests and localStorage operations for authentication:
 * - Login / Logout / Registration
 * - Refreshing expired Access Tokens
 * - Persistence management (localStorage)
 */

import api from '@/shared/api/client';

const authService = {
    /**
     * USER LOGIN
     * 
     * @param {string} email - User's email address
     * @param {string} password - User's password
     * @returns {Promise<Object>} - { success: true, user } or { success: false, message }
     */
    login: async (email, password) => {
        try {
            // STEP 1: Dispatch HTTP POST to API Gateway auth route (/api/v1/auth/login)
            const response = await api.post('/v1/auth/login', { email, password });
            
            // STEP 2: Extract auth payload direct properties matching backend response structure
            const { accessToken, refreshToken, user } = response.data;

            // STEP 3: Validate presence of essential tokens and user data
            if (accessToken && user) {
                // STEP 4: Store credentials and profile state into localStorage
                localStorage.setItem('accessToken', accessToken);
                if (refreshToken) localStorage.setItem('refreshToken', refreshToken);
                localStorage.setItem('user', JSON.stringify(user));
                
                // STEP 5: Attach Authorization bearer header to all future requests on instance
                api.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;
                
                // STEP 6: Return clean success status with user object
                return { success: true, user };
            }
            
            return { success: false, message: 'Invalid server response structure' };
        } catch (error) {
            // STEP 7: Extract detailed error message if available from backend response
            const errorMessage = error.response?.data?.message || 'Login failed. Please check credentials.';
            return { success: false, message: errorMessage };
        }
    },

    /**
     * USER REGISTRATION
     * 
     * @param {Object} userData - Registration payload (email, password, name, etc.)
     * @returns {Promise<Object>} - API response data
     */
    register: async (userData) => {
        // STEP 1: Dispatch HTTP POST request to user registration endpoint
        const response = await api.post('/v1/auth/register', userData);
        return response.data;
    },

    /**
     * REFRESH TOKEN
     * Triggered directly or via Axios response interceptors when Access Token expires.
     * 
     * @returns {Promise<string|null>} - New accessToken or null if renewal fails
     */
    refreshToken: async () => {
        // STEP 1: Retrieve stored refresh token
        const refreshToken = localStorage.getItem('refreshToken');
        if (!refreshToken) return null;

        try {
            // STEP 2: Request token renewal from backend
            const response = await api.post('/v1/auth/refresh', { refreshToken });
            
            // STEP 3: Extract newly issued accessToken directly from root response payload
            const { accessToken, refreshToken: newRefreshToken } = response.data;

            if (accessToken) {
                // STEP 4: Update localStorage with fresh token(s)
                localStorage.setItem('accessToken', accessToken);
                if (newRefreshToken) {
                    localStorage.setItem('refreshToken', newRefreshToken);
                }
                
                // STEP 5: Update global Axios default authorization header
                api.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;
                return accessToken;
            }
        } catch (error) {
            console.error('Failed to refresh token:', error);
        }

        return null;
    },

    /**
     * USER LOGOUT
     * Invalidates server session and clears client-side state.
     * 
     * @returns {Promise<Object>} - { success: true }
     */
    logout: async () => {
        const refreshToken = localStorage.getItem('refreshToken');
        
        // STEP 1: Notify backend to revoke refresh token if available
        if (refreshToken) {
            try {
                await api.post('/v1/auth/logout', { refreshToken });
            } catch (error) {
                // Non-blocking catch: proceed with local purge even if network/server fails
                console.error('Server logout warning:', error);
            }
        }

        // STEP 2: Wipe all auth artifacts from localStorage
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');

        // STEP 3: Remove authorization header default from Axios client
        delete api.defaults.headers.common['Authorization'];

        return { success: true };
    },

    /**
     * GET CURRENT USER (Local Cache)
     * Reads user profile object stored in localStorage.
     * 
     * @returns {Object|null}
     */
    getCurrentUser: () => {
        const userStr = localStorage.getItem('user');
        if (userStr) {
            try {
                return JSON.parse(userStr);
            } catch {
                return null;
            }
        }
        return null;
    },

    /**
     * GET CURRENT ACCESS TOKEN
     * 
     * @returns {string|null}
     */
    getToken: () => {
        return localStorage.getItem('accessToken');
    },

    /**
     * CHECK IF USER IS AUTHENTICATED
     * 
     * @returns {boolean}
     */
    isAuthenticated: () => {
        return !!localStorage.getItem('accessToken');
    }
};

export default authService;