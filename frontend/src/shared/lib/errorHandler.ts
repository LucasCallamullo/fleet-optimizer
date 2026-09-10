import type { AxiosError } from 'axios';
import type { ApiErrorPayload } from '@shared/types/commonTypes';

// ================================================================
// TYPES & INTERFACES
// ================================================================

export interface ErrorOptions {
  showStatus?: boolean;
  showPath?: boolean;
}

// ================================================================
// UTILITY FUNCTIONS
// ================================================================

/**
 * Extract error message from API response with status code
 * 
 * @param err - Axios error object or generic unknown error
 * @param options - Configuration options for status and path inclusion
 * @returns Human-readable error message with status
 */
export const extractErrorMessage = (
  err: unknown,
  options: ErrorOptions = { showStatus: true, showPath: false }
): string => {
  if (!err) {
    return 'An unknown error occurred.';
  }

  const axiosError = err as AxiosError<ApiErrorPayload>;

  // 1. Server responded with error status (HTTP 4xx, 5xx)
  if (axiosError.response) {
    const { data, status, statusText } = axiosError.response;
    let errorMessage = '';
    let errorDetail = '';

    if (data) {
      if (data.error) {
        errorDetail = data.error;
      } else if (data.detail) {
        errorDetail = data.detail;
      } else if (data.message) {
        errorDetail = data.message;
      } else if (data.errors && Array.isArray(data.errors)) {
        errorDetail = data.errors.join('\n');
      } else if (typeof data === 'string') {
        errorDetail = data;
      }
    }

    if (!errorDetail) {
      errorDetail = statusText || 'Server error';
    }

    if (options.showStatus) {
      errorMessage = `Status: ${status} - ${errorDetail}`;
    } else {
      errorMessage = errorDetail;
    }

    if (options.showPath && data?.path) {
      errorMessage += ` (Path: ${data.path})`;
    }

    return errorMessage;
  }

  // 2. Request was made but no response received (network error / backend down)
  if (axiosError.request) {
    return 'No response from server. Please check your network connection.';
  }

  // 3. Native JavaScript or Axios configuration error
  if (axiosError.message) {
    return axiosError.message;
  }

  return 'An unexpected error occurred.';
};

/**
 * Extract only the error detail string without status code prefix
 * 
 * @param err - Axios error object or generic unknown error
 * @returns Error detail message only
 */
export const extractErrorDetail = (err: unknown): string => {
  if (!err) return 'An unknown error occurred.';

  const axiosError = err as AxiosError<ApiErrorPayload>;

  if (axiosError.response && axiosError.response.data) {
    const data = axiosError.response.data;
    if (data.error) return data.error;
    if (data.detail) return data.detail;
    if (data.message) return data.message;
    if (data.errors && Array.isArray(data.errors)) {
      return data.errors.join('\n');
    }
    if (typeof data === 'string') return data;
  }

  if (axiosError.request) return 'No response from server. Check your connection.';
  if (axiosError.message) return axiosError.message;

  return 'An unexpected error occurred.';
};

/**
 * Extract HTTP status code from error
 * 
 * @param err - Axios error object or generic unknown error
 * @returns HTTP status code or null
 */
export const extractErrorStatus = (err: unknown): number | null => {
  const axiosError = err as AxiosError;
  if (axiosError?.response?.status) {
    return axiosError.response.status;
  }
  return null;
};

/**
 * Check if error matches a specific HTTP status code
 * 
 * @param err - Axios error object or generic unknown error
 * @param statusCode - HTTP status code to check
 * @returns True if error matches the status code
 */
export const isErrorStatus = (err: unknown, statusCode: number): boolean => {
  return extractErrorStatus(err) === statusCode;
};