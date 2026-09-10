import type { ApiResponse } from '@shared/types/commonTypes';
import type { CategoryResponseDTO } from './categoriesTypes';

// ================================================================
// ENUMS & COMMON TYPES
// ================================================================

export type VehicleStatus = 'AVAILABLE' | 'IN_TRANSIT' | 'MAINTENANCE' | 'OUT_OF_SERVICE' | 'RESERVED';

// ================================================================
// DTO INTERFACES (MATCHING SPRING BOOT BACKEND)
// ================================================================

/**
 * Basic DTO for list endpoints (`VehicleResponseDTO.java`)
 */
export interface VehicleResponse {
  id: number;
  licensePlate: string;
  year: number;
  updatedAt: string;
  categoryId: number;

  // Physical Capacities
  maxWeightKg: number;
  maxVolumeCbm: number;

  // Efficiency and Costs
  fuelConsumptionPerKm: number;
  costPerKm: number;
  pricePerKm: number;

  // Status
  status: VehicleStatus;
}

/**
 * Detailed DTO for single vehicle lookup (`VehicleDetailDTO.java`)
 */
export interface VehicleDetail {
  id: number;
  licensePlate: string;
  year: number;
  createdAt: string;
  updatedAt: string;
  category: CategoryResponseDTO;

  // Physical Capacities
  maxWeightKg: number;
  maxVolumeCbm: number;

  // Efficiency and Costs
  fuelConsumptionPerKm: number;
  costPerKm: number;
  pricePerKm: number;

  // Status
  status: VehicleStatus;
}

/**
 * Payload interface for creation/update requests (`VehicleRequestDTO`)
 */
export interface VehicleDTO {
  licensePlate: string;
  year: number;
  categoryId: number;
  maxWeightKg: number;
  maxVolumeCbm: number;
  fuelConsumptionPerKm: number;
  costPerKm: number;
  pricePerKm: number;
  status?: VehicleStatus;
}

// ================================================================
// API RESPONSE WRAPPERS
// ================================================================

export type VehicleListResponse = ApiResponse<VehicleResponse[]>;
export type VehicleListResponseDetail = ApiResponse<VehicleDetail[]>;
export type VehicleDetailResponse = ApiResponse<VehicleDetail>;
export type VehicleSingleResponse = ApiResponse<VehicleResponse>;
export type VehicleVoidResponse = ApiResponse<void>;