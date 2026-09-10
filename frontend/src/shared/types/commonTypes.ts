
// ============================================
// GENERIC API TYPES
// ============================================

/**
 * API Response wrapper from Spring Boot backend
 */
export interface ApiResponse<T = any> {
  timestamp: string;
  status: number;
  detail?: string | null;
  message?: string | null;
  path?: string | null;
  data: T;
  success: boolean;
}

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