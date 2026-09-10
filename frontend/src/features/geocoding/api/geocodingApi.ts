import api from '@/shared/api/client';
import type { ApiResponse } from '@shared/types/commonTypes';

// ================================================================
// TYPES & INTERFACES
// ================================================================

export interface DistanceRequest {
  originLat: number;
  originLon: number;
  destLat: number;
  destLon: number;
}

export interface DistanceResponse {
  distanceKm: number;
  durationMinutes?: number;
  originFormatted?: string;
  destinationFormatted?: string;
}

export interface BatchDistanceRequest {
  locations: DistanceRequest[];
}

// ================================================================
// API SERVICE
// ================================================================

const BASE_URL = '/v1/distance';

const geocodingApi = {
  /**
   * Calculate distance between two coordinate points
   */
  calculateDistance: async (
    payload: DistanceRequest
  ): Promise<ApiResponse<DistanceResponse>> => {
    // Step-by-step explanation: Dispatch HTTP POST carrying coordinates payload to distance endpoint
    const response = await api.post<ApiResponse<DistanceResponse>>(BASE_URL, payload);
    return response.data;
  },

  /**
   * Calculate multiple distance routes in batch
   */
  calculateBatch: async (
    locations: DistanceRequest[]
  ): Promise<ApiResponse<DistanceResponse[]>> => {
    // Step-by-step explanation: Post batch coordinate array payload to batch distance endpoint
    const response = await api.post<ApiResponse<DistanceResponse[]>>(`${BASE_URL}/batch`, {
      locations,
    });
    return response.data;
  },
};

export default geocodingApi;