// src/features/vehicles/services/categoriesApi.js
import api from "@/shared/api/client";

// ================================================================
// CONSTANTS
// ================================================================
const BASE_URL = '/v1/categories';

// ================================================================
// CATEGORIES API SERVICE
// ================================================================
const categoriesApi = {
  /**
   * getAll() - GET ALL CATEGORIES
   * 
   * @description Fetches all vehicle categories from the backend
   * @returns {Promise<Array>} Array of category objects
   * @throws {Error} If the request fails
   * 
   * @example
   * const categories = await categoriesApi.getAll();
   * // categories = [{ id: 1, name: 'Truck' }, ...]
   */
  getAll: async () => {
    const response = await api.get(BASE_URL);
    return response.data;
  },

  /**
   * getById() - GET A SINGLE CATEGORY
   * 
   * @param {number} id - Category ID
   * @returns {Promise<Object>} Category data
   */
  getById: async (id) => {
    const response = await api.get(`${BASE_URL}/${id}`);
    return response.data;
  },

  /**
   * create() - CREATE A NEW CATEGORY
   * 
   * @param {Object} categoryData - Category data
   * @param {string} categoryData.name - Category name
   * @param {string} [categoryData.description] - Category description
   * @returns {Promise<Object>} Created category
   */
  create: async (categoryData) => {
    const response = await api.post(BASE_URL, categoryData);
    return response.data;
  },

  /**
   * update() - UPDATE AN EXISTING CATEGORY
   * 
   * @param {number} id - Category ID
   * @param {Object} categoryData - Updated category data
   * @returns {Promise<Object>} Updated category
   */
  update: async (id, categoryData) => {
    const response = await api.put(`${BASE_URL}/${id}`, categoryData);
    return response.data;
  },

  /**
   * delete() - DELETE A CATEGORY
   * 
   * @param {number} id - Category ID
   * @returns {Promise<void>}
   */
  delete: async (id) => {
    await api.delete(`${BASE_URL}/${id}`);
  },
};

export default categoriesApi;