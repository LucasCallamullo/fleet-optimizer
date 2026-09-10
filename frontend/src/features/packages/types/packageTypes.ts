import type { ApiResponse } from '@shared/types/commonTypes';

// ================================================================
// ENUMS & COMMON TYPES
// ================================================================

/**
 * Represents the status lifecycle of a package (`PackageStatus.java`)
 */
export type PackageStatus =
  | 'CREATED'
  | 'PROCESSING'
  | 'READY_FOR_PICKUP'
  | 'IN_TRANSIT'
  | 'DELIVERED'
  | 'ON_HOLD'
  | 'CANCELLED';

/**
 * Embedded Location object model (`Location.java`)
 */
export interface Location {
  street?: string;
  streetNumber?: string;
  city?: string;
  state?: string;
  country?: string;
  postalCode?: string;
  latitude?: number;
  longitude?: number;
}

// ================================================================
// DTO INTERFACES (MATCHING SPRING BOOT BACKEND)
// ================================================================

/**
 * Store response DTO with full location details (`StoreResponseDTO.java`)
 */
export interface StoreResponseDTO {
  id: number;
  name: string;
  description?: string;
  location: Location;
  ownerId: string;
}

/**
 * Basic package response DTO for list endpoints (`PackageResponseDTO.java`)
 */
export interface PackageResponseDTO {
  id: number;
  trackingNumber: string;
  totalWeightKg: number;
  totalVolumeCbm: number;
  status: PackageStatus;
  storeId: number;
  ownerId: string;
}

/**
 * Detailed package response DTO with full store information (`PackageDetailDTO.java`)
 */
export interface PackageDetailDTO {
  id: number;
  trackingNumber: string;
  totalWeightKg: number;
  totalVolumeCbm: number;
  status: PackageStatus;
  ownerId: string;
  createdAt: string;
  updatedAt: string;
  store: StoreResponseDTO;
}

/**
 * Payload interface for creation/update requests (`PackageRequestDTO`)
 */
export interface PackageDTO {
  trackingNumber: string;
  totalWeightKg: number;
  totalVolumeCbm: number;
  storeId: number;
  status?: PackageStatus;
}

// ================================================================
// API RESPONSE WRAPPERS
// ================================================================

export type PackageListResponse = ApiResponse<PackageResponseDTO[]>;
export type PackageDetailListResponse = ApiResponse<PackageDetailDTO[]>;
export type PackageSingleResponse = ApiResponse<PackageResponseDTO>;
export type PackageDetailResponse = ApiResponse<PackageDetailDTO>;
export type PackageVoidResponse = ApiResponse<void>;