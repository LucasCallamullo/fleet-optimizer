// src/shared/lib/errorHandler.js

/**
 * Extract error message from API response with status code
 * 
 * @param {Error} err - Axios error object
 * @param {Object} options - Configuration options
 * @param {boolean} options.showStatus - Show status code in message (default: true)
 * @param {boolean} options.showPath - Show API path in message (default: false)
 * @returns {string} Human-readable error message with status
 * 
 * @example
 * // Returns: "403 - Forbidden: You don't have permission to access this resource"
 * const message = extractErrorMessage(err);
 * 
 * @example
 * // Returns: "You don't have permission to access this resource"
 * const message = extractErrorMessage(err, { showStatus: false });
 */
export const extractErrorMessage = (err, options = { showStatus: true, showPath: false }) => {
  // ================================================================
  // 1. No error object
  // ================================================================
  if (!err) {
    return 'An unknown error occurred.';
  }

  // ================================================================
  // 2. Server responded with error (HTTP 4xx, 5xx)
  // ================================================================
  if (err.response) {
    const { data, status, statusText } = err.response;
    
    // Build the error message with status code
    let errorMessage = '';
    let errorDetail = '';
    
    // Extract the error detail
    if (data) {
      // Your ErrorResponse: { timestamp, status, error, path }
      if (data.error) {
        errorDetail = data.error;
      } else if (data.message) {
        errorDetail = data.message;
      } else if (data.errors && Array.isArray(data.errors)) {
        errorDetail = data.errors.join('\n');
      } else if (typeof data === 'string') {
        errorDetail = data;
      }
    }
    
    // If no detail found, use statusText
    if (!errorDetail) {
      errorDetail = statusText || 'Server error';
    }
    
    // Build the final message
    if (options.showStatus) {
      // Formato: "403 - Forbidden: You don't have permission"
      errorMessage = `Status: ${status} - ${errorDetail}`;
    } else {
      errorMessage = errorDetail;
    }
    
    // Optionally add path to the message
    if (options.showPath && data?.path) {
      errorMessage += ` (Path: ${data.path})`;
    }
    
    return errorMessage;
  }

  // ================================================================
  // 3. Request was made but no response (network error)
  // ================================================================
  if (err.request) {
    return 'No response from server. Please check your network connection.';
  }

  // ================================================================
  // 4. Something else (axios config error, etc.)
  // ================================================================
  if (err.message) {
    return err.message;
  }

  // ================================================================
  // 5. Fallback
  // ================================================================
  return 'An unexpected error occurred.';
};

/**
 * Extract only the error detail without status code
 * 
 * @param {Error} err - Axios error object
 * @returns {string} Error detail only
 */
export const extractErrorDetail = (err) => {
  if (!err) return 'An unknown error occurred.';
  
  if (err.response && err.response.data) {
    const data = err.response.data;
    if (data.error) return data.error;
    if (data.message) return data.message;
    if (data.errors && Array.isArray(data.errors)) {
      return data.errors.join('\n');
    }
    if (typeof data === 'string') return data;
  }
  
  if (err.request) return 'No response from server. Check your connection.';
  if (err.message) return err.message;
  
  return 'An unexpected error occurred.';
};

/**
 * Extract HTTP status code from error
 * 
 * @param {Error} err - Axios error object
 * @returns {number|null} HTTP status code or null
 */
export const extractErrorStatus = (err) => {
  if (err?.response?.status) {
    return err.response.status;
  }
  return null;
};

/**
 * Check if error is a specific HTTP status
 * 
 * @param {Error} err - Axios error object
 * @param {number} statusCode - HTTP status code to check
 * @returns {boolean} True if error matches the status code
 */
export const isErrorStatus = (err, statusCode) => {
  return extractErrorStatus(err) === statusCode;
};