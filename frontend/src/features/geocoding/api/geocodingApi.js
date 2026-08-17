// src/features/geocoding/services/geocodingApi.js
import api from '@/shared/api/client';

const BASE_URL = '/v1/distance';

const geocodingApi = {
  /**
   * Calculate distance between two points
   */
  calculateDistance: async ({ originLat, originLon, destLat, destLon }) => {
    const response = await api.post(BASE_URL, {
      originLat,
      originLon,
      destLat,
      destLon,
    });
    return response.data;
  },

  /**
   * Calculate multiple distances in batch
   */
  calculateBatch: async (locations) => {
    const response = await api.post(`${BASE_URL}/batch`, { locations });
    return response.data;
  },
};

export default geocodingApi;