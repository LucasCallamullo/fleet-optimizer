import type { ApiResponse } from '@/shared/types/commonTypes';
import type { Location } from '@/features/packages/types/packageTypes';

// ================================================================
// ENUMS & COMMON TYPES
// ================================================================

/**
 * Represents the status of a route (`RouteStatus.java`)
 */
export type RouteStatus = 'PLANNED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED' | 'IN_TRANSIT';

/**
 * Represents the status of an individual route leg (`LegStatus.java`)
 */
export type LegStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'FAILED' | 'SKIPPED' | 'IN_TRANSIT';

// ================================================================
// REQUEST DTOS
// ================================================================

/**
 * Location request payload (`LocationRequestDTO.java`)
 */
export interface LocationRequestDTO {
  street: string;
  streetNumber?: string;
  city: string;
  state?: string;
  country?: string;
  postalCode?: string;
  latitude: number;
  longitude: number;
}

/**
 * Request DTO for creating a shipment (`ShipmentRequestDTO.java`)
 */
export interface ShipmentRequestDTO {
  packageIds: number[];
  vehicleId: number;
  destination: LocationRequestDTO;
}

// ================================================================
// RESPONSE DTOS
// ================================================================

/**
 * Detailed DTO for individual route legs (`LegDetailDTO.java`)
 */
export interface LegDetailDTO {
  id: number;
  sequenceOrder: number;
  status: LegStatus;
  distanceKm: number;
  durationMinutes: number;
  packageId?: number;
  origin: Location;
  destination: Location;
  estimatedArrival?: string;
}

/**
 * Detailed DTO for full route view (`RouteDetailDTO.java`)
 */
export interface RouteDetailDTO {
  id: number;
  name: string;
  description?: string;
  status: RouteStatus;
  estimatedDistanceKm: number;
  estimatedDurationMinutes: number;
  createdAt: string;
  updatedAt: string;
  legs: LegDetailDTO[];
}

/**
 * DTO for leg information within a shipment response (`ShipmentLegDTO.java`)
 */
export interface ShipmentLegDTO {
  legId: number;
  sequence: number;
  status: LegStatus;
  distanceKm: number;
  durationMinutes: number;
  vehicleId: number;
  packageId: number;
  weightKg: number;
  volumeCbm: number;
  origin: Location;
  destination: Location;
  estimatedArrival: string;
}

/**
 * Response DTO for a created shipment (`ShipmentResponseDTO.java`)
 */
export interface ShipmentResponseDTO {
  routeId: number;
  routeName: string;
  status: RouteStatus;
  vehicleId: number;
  totalDistanceKm: number;
  totalDurationMinutes: number;
  legs: ShipmentLegDTO[];
  estimatedArrival: string;
  createdAt: string;
}

// ================================================================
// API RESPONSE WRAPPERS
// ================================================================

export type ShipmentSingleResponse = ApiResponse<ShipmentResponseDTO>;
export type ShipmentListResponse = ApiResponse<ShipmentResponseDTO[]>;
export type RouteDetailResponse = ApiResponse<RouteDetailDTO>;
export type RouteDetailListResponse = ApiResponse<RouteDetailDTO[]>;
export type RouteVoidResponse = ApiResponse<void>;