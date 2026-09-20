import axios, { type InternalAxiosRequestConfig, type AxiosError } from 'axios';
import type { ApiResponse } from '@shared/types/commonTypes';

// Use Vite environment variable with local fallback for deployment portability
// const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

// this changes is because Nginx is use it like reverse proxy to redirect to gateway by private networks on docker
// const BASE_URL = 'http://localhost/api';
const BASE_URL = import.meta.env.VITE_API_URL || '/api';

/**
 * Custom Axios instance configured with base URL, timeout, and standard headers
 */
const api = axios.create({
  baseURL: BASE_URL,
  withCredentials: false, // Set to false when passing JWT tokens via Bearer header
  timeout: 10000, // Abort request after 10 seconds of inactivity
  headers: {
    'Content-Type': 'application/json',
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
// 1. REQUEST INTERCEPTOR
// ================================================================
api.interceptors.request.use(
  // Step-by-step explanation: Attach Bearer authorization header if token exists
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('accessToken');

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },

  // Step-by-step explanation: Rejection handler if request preparation fails
  (error: AxiosError) => {
    return Promise.reject(error);
  }
);

// ================================================================
// 2. RESPONSE INTERCEPTOR
// ================================================================
api.interceptors.response.use(
  // Step-by-step explanation: Pass through successful responses (2xx status codes)
  (response) => response,

  // Step-by-step explanation: Intercept errors to handle automatic 401 token refresh
  async (error: AxiosError) => {
    const originalRequest = error.config as CustomAxiosRequestConfig | undefined;

    // Check for HTTP 401 and verify request hasn't been retried already
    if (error.response?.status === 401 && originalRequest && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const refreshToken = localStorage.getItem('refreshToken');

        if (!refreshToken) {
          throw new Error('No refresh token available');
        }

        // Call refresh endpoint bypassing this interceptor instance via raw axios
        const { data: responseData } = await axios.post<ApiResponse<RefreshResponse> | RefreshResponse>(
          `${BASE_URL}/v1/auth/refresh`,
          { refreshToken }
        );

        // Safely extract refresh payload regardless of response wrapping
        const tokenData =
          'data' in responseData && responseData.data
            ? responseData.data
            : (responseData as RefreshResponse);

        const newAccessToken = tokenData.accessToken;

        if (newAccessToken) {
          localStorage.setItem('accessToken', newAccessToken);

          if (tokenData.refreshToken) {
            localStorage.setItem('refreshToken', tokenData.refreshToken);
          }

          // Update header and replay original request
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
          return api(originalRequest);
        }
      } catch (refreshError) {
        // Clear auth state on refresh failure and redirect to login
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');

        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default api;