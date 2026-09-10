import api from '@/shared/api/client';
import type {
  VehicleDTO,
  VehicleListResponse,
  VehicleListResponseDetail,
  VehicleDetailResponse,
  VehicleSingleResponse,
  VehicleVoidResponse,
} from '../types/vehiclesTypes';

// ================================================================
// TYPES & INTERFACES FOR FILTERS
// ================================================================

export interface VehicleAvailableFilters {
  minCapacity?: number;
}

// ================================================================
// CONSTANTS
// ================================================================

const BASE_URL = '/v1/vehicles';

// ================================================================
// VEHICLES API SERVICE
// ================================================================

const vehiclesApi = {
  /**
   * getAll() - GET ALL DETAILED VEHICLES
   * Fetches full vehicle details including nested Category object
   */
  getAll: async (): Promise<VehicleListResponseDetail> => {
    // Step-by-step explanation: Issue HTTP GET to fetch all vehicles with expanded details
    const response = await api.get<VehicleListResponseDetail>(`${BASE_URL}/detailed`);
    return response.data;
  },

  /**
   * getById() - GET A SINGLE VEHICLE DETAIL
   */
  getById: async (id: number | string): Promise<VehicleDetailResponse> => {
    // Step-by-step explanation: Issue HTTP GET targeting a specific vehicle ID
    const response = await api.get<VehicleDetailResponse>(`${BASE_URL}/${id}`);
    return response.data;
  },

  /**
   * create() - CREATE A NEW VEHICLE
   */
  create: async (vehicleData: VehicleDTO): Promise<VehicleSingleResponse> => {
    // Step-by-step explanation: Issue HTTP POST carrying the new vehicle payload
    const response = await api.post<VehicleSingleResponse>(BASE_URL, vehicleData);
    return response.data;
  },

  /**
   * update() - UPDATE AN EXISTING VEHICLE
   */
  update: async (
    id: number | string,
    vehicleData: Partial<VehicleDTO>
  ): Promise<VehicleSingleResponse> => {
    // Step-by-step explanation: Issue HTTP PUT to update vehicle record
    const response = await api.put<VehicleSingleResponse>(`${BASE_URL}/${id}`, vehicleData);
    return response.data;
  },

  /**
   * delete() - DELETE A VEHICLE
   */
  delete: async (id: number | string): Promise<VehicleVoidResponse> => {
    // Step-by-step explanation: Issue HTTP DELETE request for specified vehicle ID
    const response = await api.delete<VehicleVoidResponse>(`${BASE_URL}/${id}`);
    return response.data;
  },

  /**
   * getAvailable() - GET AVAILABLE VEHICLES WITH FILTERS
   */
  getAvailable: async (
    filters: VehicleAvailableFilters = {}
  ): Promise<VehicleListResponse> => {
    const params = new URLSearchParams();
    if (filters.minCapacity !== undefined) {
      params.append('minCapacity', filters.minCapacity.toString());
    }

    const queryString = params.toString() ? `?${params.toString()}` : '';
    
    // Step-by-step explanation: Issue HTTP GET to retrieve filtered available vehicles
    const response = await api.get<VehicleListResponse>(
      `${BASE_URL}/available${queryString}`
    );
    return response.data;
  },
};

export default vehiclesApi;