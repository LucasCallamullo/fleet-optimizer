import axios, { type InternalAxiosRequestConfig, type AxiosError } from 'axios';
import type { ApiResponse } from '@shared/types/commonTypes';

const BASE_URL = 'http://localhost:8080/api';

/**
 * Custom Axios instance configured with base URL and default headers
 */
const api = axios.create({
  baseURL: BASE_URL, // Backend server API base URL
  withCredentials: false, // ← IMPORTANT: Set it to false if you use JWT in the header
  timeout: 10000, // 10-second timeout limit - Maximum request duration before aborting (10 seconds)
  headers: {
    'Content-Type': 'application/json', // Ensures all payloads stream as JSON
  },
});

/**
 * Internal interface extending Axios configuration to support the retry flag
 */
interface CustomAxiosRequestConfig extends InternalAxiosRequestConfig {
  _retry?: boolean;
}

/**
 * Payload interface expected from the token refresh endpoint
 */
interface RefreshResponse {
  accessToken: string;
  refreshToken?: string;
}

// ================================================================
// 2. REQUEST INTERCEPTOR (TRIGGERS BEFORE THE REQUEST IS SENT)
// ================================================================
api.interceptors.request.use(
  // Function executed BEFORE dispatching the request (Success handler)
  (config: InternalAxiosRequestConfig) => {
    // Step 1: Retrieve the access token from localStorage
    const token = localStorage.getItem('accessToken');

    // Step 2: If a token exists, append it to the Authorization Bearer header
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    // Step 3: Return the modified configuration object (or unmodified if no token)
    return config;
  },

  // Function executed if an error occurs prior to dispatching the request (Rare case)
  (error: AxiosError) => {
    return Promise.reject(error);
  }
);

// ================================================================
// 3. RESPONSE INTERCEPTOR (TRIGGERS AFTER THE RESPONSE IS RECEIVED)
// ================================================================
api.interceptors.response.use(
  // Success handler for successful HTTP responses (status codes 200-299)
  // Simply forwards the response payload downstream
  (response) => response,

  // Error handler for error HTTP status responses (status codes 400, 401, 403, 404, 500)
  async (error: AxiosError) => {
    // Step 1: Cache the configuration of the original request that failed
    // We will need this to replay the operation once the token is renewed
    const originalRequest = error.config as CustomAxiosRequestConfig | undefined;

    // Step 2: Check for HTTP 401 (Unauthorized / Token Expired)
    // Also ensure this request is not already a retry fallback to prevent infinite loops
    if (error.response?.status === 401 && originalRequest && !originalRequest._retry) {

      // Flag this request to mark that a renewal attempt is already in progress
      originalRequest._retry = true;

      try {
        // Step 3: Fetch the persistent refresh token from localStorage
        const refreshToken = localStorage.getItem('refreshToken');

        if (!refreshToken) {
          throw new Error('No refresh token available');
        }

        // Step 4: Call the token mutation refresh endpoint to obtain a new accessToken
        // NOTE: We use vanilla axios directly here instead of the custom 'api' instance
        // to bypass the request/response interceptors and avoid a potential recursion deadlock.
        // Supports both wrapped ApiResponse<RefreshResponse> and raw RefreshResponse formats.
        const { data: responseData } = await axios.post<ApiResponse<RefreshResponse> | RefreshResponse>(
          `${BASE_URL}/v1/auth/refresh`,
          { refreshToken }
        );

        // Step 5: Safely extract token data regardless of response wrapper structure
        const tokenData =
          'data' in responseData && responseData.data
            ? responseData.data
            : (responseData as RefreshResponse);

        const newAccessToken = tokenData.accessToken;

        // Step 6: If the renewal response succeeds and returns valid payload data
        if (newAccessToken) {
          // Commit the fresh accessToken back into localStorage
          localStorage.setItem('accessToken', newAccessToken);

          // Update refresh token if the backend issued a new one
          if (tokenData.refreshToken) {
            localStorage.setItem('refreshToken', tokenData.refreshToken);
          }

          // Step 7: Rewrite the Authorization header of the original failed request with the new token
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;

          // Step 8: Replay/Re-execute the original request with the updated token header.
          // The application component receives this response directly, completely unaware a refresh happened.
          return api(originalRequest);
        }
      } catch (refreshError) {
        // Step 9: If the refresh sequence fails (refreshToken itself is expired or invalid)
        // Purge all authentication and user state contexts from the browser
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');

        // Step 10: Force an immediate hard routing redirection to the login screen
        // window.location.href triggers a full page refresh, wiping any stale memory states
        window.location.href = '/login';

        // Step 11: Terminate the sequence and reject the promise chain with the execution error
        return Promise.reject(refreshError);
      }
    }

    // Step 12: For all non-401 errors (such as 400, 403, 404, 500, etc.)
    // Simply reject the promise chain and pass the unhandled backend error along
    return Promise.reject(error);
  }
);

export default api;