import api from '@/shared/api/client';
import type {
  CategoryDTO,
  CategoryResponse,
  CategoryListResponse,
  CategoryVoidResponse,
} from '../types/categoriesTypes';

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
   */
  getAll: async (): Promise<CategoryListResponse> => {
    // Step-by-step explanation: Issue HTTP GET request to retrieve all category records
    const response = await api.get<CategoryListResponse>(BASE_URL);
    return response.data;
  },

  /**
   * getById() - GET A SINGLE CATEGORY
   */
  getById: async (id: number): Promise<CategoryResponse> => {
    // Step-by-step explanation: Issue HTTP GET request targeting a specific category ID
    const response = await api.get<CategoryResponse>(`${BASE_URL}/${id}`);
    return response.data;
  },

  /**
   * create() - CREATE A NEW CATEGORY
   */
  create: async (categoryData: CategoryDTO): Promise<CategoryResponse> => {
    // Step-by-step explanation: Issue HTTP POST carrying the new category payload
    const response = await api.post<CategoryResponse>(BASE_URL, categoryData);
    return response.data;
  },

  /**
   * update() - UPDATE AN EXISTING CATEGORY
   */
  update: async (
    id: number,
    categoryData: Partial<CategoryDTO>
  ): Promise<CategoryResponse> => {
    // Step-by-step explanation: Issue HTTP PUT to update an existing category entry
    const response = await api.put<CategoryResponse>(`${BASE_URL}/${id}`, categoryData);
    return response.data;
  },

  /**
   * delete() - DELETE A CATEGORY
   */
  delete: async (id: number): Promise<CategoryVoidResponse> => {
    // Step-by-step explanation: Issue HTTP DELETE request for the specified category ID
    const response = await api.delete<CategoryVoidResponse>(`${BASE_URL}/${id}`);
    return response.data;
  },
};

export default categoriesApi;