import api from '@/shared/api/client';
import type { ApiRouteDetail, RouteDetail } from '../types/routeCreateTypes';

export const routesApi = {
  getById: async (id: string | number): Promise<RouteDetail> => {
    const response = await api.get<ApiRouteDetail>(`/v1/routes/${id}`);
    
    return response.data?.data;
  },
};