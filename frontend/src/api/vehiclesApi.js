// src/api/vehiclesApi.js
import api from "@/api/client";

/**
 * VEHICLES API SERVICE
 * 
 * WHY RETURN response.data?
 * - api client (Axios) wraps the response in { data, status, headers, ... }
 * - We only need the actual data, not the metadata
 * - Simplifies the consumer's code
 * 
 * @see {@link https://axios-http.com/docs/res_schema | Axios Response Schema}
 */
const vehiclesApi = {
  /**
   * getAll() - GET ALL VEHICLES
   * 
   * @description Fetches the complete list of vehicles from the backend
   * @returns {Promise<Array>} Array of vehicle objects
   * @throws {Error} If the request fails
   * 
   * @example
   * const vehicles = await vehiclesApi.getAll();
   * // vehicles = [{ id: 1, licensePlate: 'ABC123', year: 2023 }, ...]
   */
  getAll: async () => {
    const response = await api.get("/vehicles");
    return response.data; 
  },

  /**
   * create() - CREATE A NEW VEHICLE
   * 
   * @description Sends a POST request to create a new vehicle
   * @param {Object} vehicleData - Vehicle data to create
   * @param {string} vehicleData.licensePlate - Vehicle plate number
   * @param {number} vehicleData.year - Manufacturing year
   * @param {Object} vehicleData.category - Category object { id: number }
   * @returns {Promise<Object>} Created vehicle with assigned ID
   * @throws {Error} If the request fails
   * 
   * @example
   * const newVehicle = {
   *   licensePlate: 'XYZ789',
   *   year: 2024,
   *   category: { id: 1 }
   * };
   * const created = await vehiclesApi.create(newVehicle);
   * // created = { id: 5, licensePlate: 'XYZ789', year: 2024, ... }
   */
  create: async (vehicleData) => {
    const response = await api.post("/vehicles", vehicleData);
    return response.data;
  },

  /**
   * update() - UPDATE AN EXISTING VEHICLE
   * 
   * @description Sends a PUT request to update an existing vehicle
   * @param {number} id - ID of the vehicle to update
   * @param {Object} vehicleData - Updated vehicle data
   * @param {string} vehicleData.licensePlate - Updated plate number
   * @param {number} vehicleData.year - Updated year
   * @returns {Promise<Object>} Updated vehicle data
   * @throws {Error} If the request fails
   * 
   * @example
   * await vehiclesApi.update(5, {
   *   licensePlate: 'ABC123',
   *   year: 2023
   * });
   */
  update: async (id, vehicleData) => {
    const response = await api.put(`/vehicles/${id}`, vehicleData);
    return response.data;
  },

  /**
   * delete() - DELETE A VEHICLE
   * 
   * @description Sends a DELETE request to remove a vehicle
   * @param {number} id - ID of the vehicle to delete
   * @returns {Promise<void>} No response data (204 No Content)
   * @throws {Error} If the request fails
   * 
   * @example
   * await vehiclesApi.delete(5); // Deletes vehicle with ID 5
   */
  delete: async (id) => {
    await api.delete(`/vehicles/${id}`);
  },

  /**
   * getById() - GET A SINGLE VEHICLE
   * 
   * @description Fetches a single vehicle by its ID
   * @param {number} id - ID of the vehicle to fetch
   * @returns {Promise<Object>} Vehicle data
   * @throws {Error} If the request fails or vehicle not found
   * 
   * @example
   * const vehicle = await vehiclesApi.getById(5);
   * // vehicle = { id: 5, licensePlate: 'ABC123', year: 2023, ... }
   */
  getById: async (id) => {
    const response = await api.get(`/vehicles/${id}`);
    return response.data;
  }
};

export default vehiclesApi;