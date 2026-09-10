import type { ApiResponse } from '@shared/types/commonTypes';

// ================================================================
// DTO INTERFACES (MATCHING SPRING BOOT BACKEND)
// ================================================================

/**
 * Basic category DTO (`CategoryResponseDTO.java`)
 * Used inside nested response objects (e.g., VehicleDetailDTO)
 */
export interface CategoryResponseDTO {
  id: number;
  name: string;
  description?: string | null;
}

/**
 * Full category DTO (`CategoryDetailDTO.java`)
 * Used for category management endpoints
 */
export interface CategoryDetailDTO {
  id: number;
  name: string;
  description?: string | null;
  isActive: boolean;
}

/**
 * Payload interface for creation/update requests
 */
export interface CategoryDTO {
  name: string;
  description?: string;
  isActive?: boolean;
}

// ================================================================
// API RESPONSE WRAPPERS
// ================================================================

export type CategoryResponse = ApiResponse<CategoryDetailDTO>;
export type CategoryListResponse = ApiResponse<CategoryResponseDTO[]>;
export type CategoryVoidResponse = ApiResponse<void>;