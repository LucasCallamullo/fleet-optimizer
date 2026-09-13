import api from '@/shared/api/client';
import type { ApiRouteDetail, RouteDetail, ApiRouteList } from '../types/routeCreateTypes';

export const routesApi = {

  getAll: async (): Promise<RouteDetail[]> => {
    const response = await api.get<ApiRouteList>('/v1/routes');
    return response.data.data ?? [];
  },

  getById: async (id: string | number): Promise<RouteDetail> => {
    const response = await api.get<ApiRouteDetail>(`/v1/routes/${id}`);
    
    return response.data?.data;
  },
};