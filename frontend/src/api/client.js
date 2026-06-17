import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api/v1'

const api = axios.create({
  baseURL: BASE_URL,  // Backend server API base URL
  timeout: 10000,                           // 10-second timeout limit -  Maximum request duration before aborting (10 seconds)
  headers: {
    'Content-Type': 'application/json',     // Ensures all payloads stream as JSON
  },
});

/*

// 2. REQUEST INTERCEPTOR (TRIGGERS BEFORE THE REQUEST IS SENT)
// 
// This interceptor executes BEFORE each HTTP request leaves the browser.
// Core Purpose: Automatically injects the active JWT token into the Authorization header.
// 
// Why is this useful?
// - Removes the need to manually append 'Authorization: Bearer ...' to every single endpoint call.
// - If the user is authenticated, all requests are implicitly secured.
// - If no token exists, the request passes as-is (and will be handled by the backend with a 401).

api.interceptors.request.use(
  // Function executed BEFORE dispatching the request (Success handler)
  (config) => {
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
  (error) => {
    return Promise.reject(error);
  }
);

// ================================================================
// 3. RESPONSE INTERCEPTOR (TRIGGERS AFTER THE RESPONSE IS RECEIVED)
// ================================================================
// 
// This interceptor executes AFTER receiving a response back from the backend.
// Core Purpose: Globally intercept authentication errors (HTTP 401) and
// seamlessly refresh the expired access token in the background.
// 
// Why is this useful?
// - If the current token expires (HTTP 401), it attempts a silent token renewal.
// - Prevents session loss, avoiding unnecessary logout redirections for the user.
// - If token renewal fails entirely, it safely triggers a forced logout sequence.

api.interceptors.response.use(
  // Success handler for successful HTTP responses (status codes 200-299)
  // Simply forwards the response payload downstream
  (response) => response,
  
  // Error handler for error HTTP status responses (status codes 400, 401, 403, 404, 500)
  async (error) => {
    // Step 1: Cache the configuration of the original request that failed
    // We will need this to replay the operation once the token is renewed
    const originalRequest = error.config;
    
    // Step 2: Check for HTTP 401 (Unauthorized / Token Expired)
    // Also ensure this request is not already a retry fallback to prevent infinite loops
    if (error.response?.status === 401 && !originalRequest._retry) {
      
      // Flag this request to mark that a renewal attempt is already in progress
      originalRequest._retry = true;
      
      try {
        // Step 3: Fetch the persistent refresh token from localStorage
        const refreshToken = localStorage.getItem('refreshToken');
        
        // Step 4: Call the token mutation refresh endpoint to obtain a new accessToken
        // NOTE: We use vanilla axios directly here instead of the custom 'api' instance
        // to bypass the request/response interceptors and avoid a potential recursion deadlock.
        const response = await axios.post(`${BASE_URL}/auth/refresh`, { refreshToken });
        
        // Step 5: If the renewal response succeeds and returns valid payload data
        if (response.data.success && response.data.data) {
          const { accessToken } = response.data.data;
          
          // Step 6: Commit the fresh accessToken back into localStorage
          localStorage.setItem('accessToken', accessToken);
          
          // Step 7: Rewrite the Authorization header of the original failed request with the new token
          originalRequest.headers.Authorization = `Bearer ${accessToken}`;
          
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

*/

export default api;