
// ============================================
// GENERIC API TYPES
// ============================================

/**
 * Generic API response wrapper from Spring Boot backend (Success Case)
 */
export interface ApiResponse<T> {
  timestamp: string;
  status: number;
  message?: string | null;
  detail?: string | null;
  path?: string | null;
  data: T;
  success: true;
}

/**
 * Standard backend error payload signature (Failure Case)
 * Handles Spring Boot Default Error Controller & Custom GlobalExceptionHandler
 */
export interface ApiErrorPayload {
  timestamp?: string;
  status?: number;
  error?: string;
  message?: string;
  detail?: string;
  errors?: string[];
  path?: string;
}

/**
 * Discriminated Union type for API results
 */
export type ApiResult<T> = ApiResponse<T> | ApiErrorPayload;

/**
 * Paginated API Response
 */
export interface PaginatedApiResponse<T = any> {
  timestamp: string;
  status: number;
  detail?: string | null;
  message?: string | null;
  path?: string | null;
  data: {
    content: T[];
    totalPages: number;
    totalElements: number;
    size: number;
    number: number;
  } | null;  
  success: boolean;
}

/**
 * Type guard to check if API response is successful
 */
export function isApiSuccess<T>(response: ApiResponse<T>): response is ApiResponse<T> & { data: T } {
  return response.success && response.data !== null;
}

/**
 * Type guard to check if API response is paginated
 */
export function isPaginatedApiSuccess<T>(
  response: PaginatedApiResponse<T>
): response is PaginatedApiResponse<T> & { data: NonNullable<PaginatedApiResponse<T>['data']> } {
  return response.success && response.data !== null;
}