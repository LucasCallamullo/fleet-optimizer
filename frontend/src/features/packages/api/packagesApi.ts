import api from '@/shared/api/client';
import type { ApiResponse } from '@shared/types/commonTypes';
import type {
  PackageDTO,
  PackageListResponse,
  PackageDetailListResponse,
  PackageDetailResponse,
  PackageSingleResponse,
  PackageVoidResponse,
} from '../types/packageTypes';

// ================================================================
// CONSTANTS
// ================================================================

const BASE_URL = '/v1/packages';

// ================================================================
// PACKAGES API SERVICE
// ================================================================

const packagesApi = {
  /**
   * Get all basic packages (filtered by user/role on backend)
   */
  getAll: async (): Promise<PackageListResponse> => {
    // Step-by-step explanation: Issue HTTP GET request to retrieve user's basic packages list
    const response = await api.get<PackageListResponse>(BASE_URL);
    return response.data;
  },

  /**
   * Get all packages with full nested Store & Location details
   */
  getAllDetailed: async (): Promise<PackageDetailListResponse> => {
    // Step-by-step explanation: Issue HTTP GET request to fetch detailed packages list
    const response = await api.get<PackageDetailListResponse>(`${BASE_URL}/detailed`);
    return response.data;
  },

  /**
   * Get single package by ID with expanded details
   */
  getById: async (id: number | string): Promise<PackageDetailResponse> => {
    // Step-by-step explanation: Issue HTTP GET request targeting package detailed endpoint
    const response = await api.get<PackageDetailResponse>(`${BASE_URL}/${id}/detailed`);
    return response.data;
  },

  /**
   * Get total package count for current authenticated user
   */
  getCount: async (): Promise<ApiResponse<number>> => {
    // Step-by-step explanation: Issue HTTP GET request to fetch user's total package count
    const response = await api.get<ApiResponse<number>>(`${BASE_URL}/count`);
    return response.data;
  },

  /**
   * Create a new package
   */
  create: async (data: PackageDTO): Promise<PackageSingleResponse> => {
    // Step-by-step explanation: Issue HTTP POST request containing new package payload
    const response = await api.post<PackageSingleResponse>(BASE_URL, data);
    return response.data;
  },

  /**
   * Update an existing package
   */
  update: async (
    id: number | string,
    data: Partial<PackageDTO>
  ): Promise<PackageSingleResponse> => {
    // Step-by-step explanation: Issue HTTP PUT request to update specified package record
    const response = await api.put<PackageSingleResponse>(`${BASE_URL}/${id}`, data);
    return response.data;
  },

  /**
   * Delete a package by ID
   */
  delete: async (id: number | string): Promise<PackageVoidResponse> => {
    // Step-by-step explanation: Issue HTTP DELETE request targeting specified package ID
    const response = await api.delete<PackageVoidResponse>(`${BASE_URL}/${id}`);
    return response.data;
  },
};

export default packagesApi;