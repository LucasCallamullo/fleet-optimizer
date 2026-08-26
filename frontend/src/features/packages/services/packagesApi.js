// src/features/packages/services/packagesApi.js
import api from '@/shared/api/client';

const BASE_URL = '/v1/packages';

const packagesApi = {
  /**
   * Get all packages (filtered by user)
   * Regular users see only their own packages.
   * Admin users see all packages.
   */
  getAll: async () => {
    const response = await api.get(BASE_URL);
    return response.data;
  },

  /**
   * Get all packages with store details
   */
  getAllDetailed: async () => {
    const response = await api.get(`${BASE_URL}/detailed`);
    return response.data;
  },

  /**
   * Get package by ID
   */
  getById: async (id) => {
    const response = await api.get(`${BASE_URL}/${id}/detailed`);
    return response.data;
  },

  /**
   * Get package count for current user
   */
  getCount: async () => {
    const response = await api.get(`${BASE_URL}/count`);
    return response.data;
  },

  /**
   * Create a new package
   */
  create: async (data) => {
    const response = await api.post(BASE_URL, data);
    return response.data;
  },

  /**
   * Update an existing package
   */
  update: async (id, data) => {
    const response = await api.put(`${BASE_URL}/${id}`, data);
    return response.data;
  },

  /**
   * Delete a package
   */
  delete: async (id) => {
    await api.delete(`${BASE_URL}/${id}`);
  },
};

export default packagesApi;