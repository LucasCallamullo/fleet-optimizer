import api from '@shared/api/client';
import type {
  LoginPayload,
  RegisterPayload,
  LoginApiResponse,
  RefreshTokenApiResponse,
  RegisterApiResponse,
  AuthOperationResult,
  User,
} from '../types/authTypes';

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
const authService = {
  /**
   * USER LOGIN
   * 
   * @param {LoginPayload} credentials - User credentials object (email, password)
   * @returns {Promise<AuthOperationResult>} - Result containing operation status and user profile
   */
  login: async (credentials: LoginPayload): Promise<AuthOperationResult> => {
    try {
      // STEP 1: Dispatch HTTP POST to API Gateway auth route (/api/v1/auth/login)
      const response = await api.post<LoginApiResponse>('/v1/auth/login', credentials);

      // STEP 2: Extract auth payload wrapped inside standard backend ApiResponse structure
      const { accessToken, refreshToken, user } = response.data.data;

      // STEP 3: Validate presence of essential tokens and user data
      if (accessToken && user) {
        // STEP 4: Store credentials and profile state into localStorage
        localStorage.setItem('accessToken', accessToken);
        if (refreshToken) localStorage.setItem('refreshToken', refreshToken);
        localStorage.setItem('user', JSON.stringify(user));

        // STEP 5: Attach Authorization bearer header to all future requests on Axios instance
        api.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;

        // STEP 6: Return clean success status with user object
        return { success: true, user };
      }

      return { success: false, message: 'Invalid response payload from server' };
    } catch (error: unknown) {
      // STEP 7: Extract detailed error message if available from backend response
      const err = error as { response?: { data?: { message?: string } } };
      const errorMessage =
        err.response?.data?.message || 'Login failed. Please check your credentials.';
      return { success: false, message: errorMessage };
    }
  },

  /**
   * USER REGISTRATION
   * 
   * @param {RegisterPayload} userData - Registration payload (email, password, name, etc.)
   * @returns {Promise<User>} - Registered user profile data
   */
  register: async (userData: RegisterPayload): Promise<User> => {
    // STEP 1: Dispatch HTTP POST request to user registration endpoint
    const response = await api.post<RegisterApiResponse>('/v1/auth/register', userData);
    
    // STEP 2: Return typed user object from response wrapper
    return response.data.data;
  },

  /**
   * REFRESH TOKEN
   * Triggered directly or via Axios response interceptors when Access Token expires.
   * 
   * @returns {Promise<string|null>} - New accessToken string or null if renewal fails
   */
  refreshToken: async (): Promise<string | null> => {
    // STEP 1: Retrieve stored refresh token from localStorage
    const refreshToken = localStorage.getItem('refreshToken');
    if (!refreshToken) return null;

    try {
      // STEP 2: Request token renewal from backend
      const response = await api.post<RefreshTokenApiResponse>('/v1/auth/refresh', {
        refreshToken,
      });

      // STEP 3: Extract newly issued tokens from response
      const { accessToken, refreshToken: newRefreshToken } = response.data.data;

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
   * @returns {Promise<{ success: boolean }>} - Operation status
   */
  logout: async (): Promise<{ success: boolean }> => {
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
   * Reads cached user profile object stored in localStorage.
   * 
   * @returns {User|null} - User object if session active, otherwise null
   */
  getCurrentUser: (): User | null => {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      try {
        return JSON.parse(userStr) as User;
      } catch {
        return null;
      }
    }
    return null;
  },

  /**
   * GET CURRENT ACCESS TOKEN
   * 
   * @returns {string|null} - Active Bearer token string
   */
  getToken: (): string | null => {
    return localStorage.getItem('accessToken');
  },

  /**
   * CHECK IF USER IS AUTHENTICATED
   * 
   * @returns {boolean} - True if access token is present in storage
   */
  isAuthenticated: (): boolean => {
    return !!localStorage.getItem('accessToken');
  },
};

export default authService;