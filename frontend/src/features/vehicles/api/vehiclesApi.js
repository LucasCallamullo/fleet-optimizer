// src/features/vehicles/services/vehiclesApi.js
import api from "@/shared/api/client";


const BASE_URL = '/v1/vehicles';

/**
 * VEHICLES API SERVICE
 * 
 * Centralized vehicle API endpoints.
 * All URLs use the BASE_URL constant for consistency.
 */
const vehiclesApi = {

  /**
   * getAll() - GET ALL VEHICLES
   * @returns {Promise<Array>} Array of vehicle objects
   */
  getAll: async () => {
    const response = await api.get(`${BASE_URL}/detailed`);
    return response.data;
  },

  /**
   * getById() - GET A SINGLE VEHICLE
   * @param {string|number} id - Vehicle ID
   * @returns {Promise<Object>} Vehicle data
   */
  getById: async (id) => {
    const response = await api.get(`${BASE_URL}/${id}`);
    return response.data;
  },

  /**
   * create() - CREATE A NEW VEHICLE
   * @param {Object} vehicleData - Vehicle data to create
   * @returns {Promise<Object>} Created vehicle
   */
  create: async (vehicleData) => {
    const response = await api.post(BASE_URL, vehicleData);
    return response.data;
  },

  /**
   * update() - UPDATE AN EXISTING VEHICLE
   * @param {string|number} id - Vehicle ID
   * @param {Object} vehicleData - Updated vehicle data
   * @returns {Promise<Object>} Updated vehicle
   */
  update: async (id, vehicleData) => {
    const response = await api.put(`${BASE_URL}/${id}`, vehicleData);
    return response.data;
  },

  /**
   * delete() - DELETE A VEHICLE
   * @param {string|number} id - Vehicle ID
   * @returns {Promise<void>}
   */
  delete: async (id) => {
    await api.delete(`${BASE_URL}/${id}`);
  },

  /**
   * getAvailable() - GET AVAILABLE VEHICLES
   * @param {Object} filters - Optional filters
   * @param {number} filters.minCapacity - Minimum capacity required
   * @returns {Promise<Array>} Array of available vehicles
   */
  getAvailable: async (filters = {}) => {
    const params = new URLSearchParams();
    if (filters.minCapacity) {
      params.append('minCapacity', filters.minCapacity);
    }
    const url = `${BASE_URL}/available?${params}`;
    const response = await api.get(url);
    return response.data;
  },
};

export default vehiclesApi;