import { useState, useEffect, useCallback } from 'react';
import categoriesApi from '@features/vehicles/api/categoriesApi';
import type { CategoryResponseDTO } from '@features/vehicles/types/categoriesTypes';
import { extractErrorDetail } from '@/shared/lib/errorHandler';

// ================================================================
// TYPES & INTERFACES
// ================================================================

export interface UseCategoriesReturn {
  categories: CategoryResponseDTO[];
  loading: boolean;
  error: string | null;
  fetchCategories: () => Promise<void>;
}

// ================================================================
// CUSTOM HOOK
// ================================================================

/**
 * useCategories - Custom hook for fetching and managing vehicle categories
 */
export const useCategories = (): UseCategoriesReturn => {
  const [categories, setCategories] = useState<CategoryResponseDTO[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const fetchCategories = useCallback(async (): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      // Step-by-step explanation: Unwraps data array from the generic ApiResponse wrapper
      const response = await categoriesApi.getAll();
      setCategories(response.data || []);
    } catch (err: unknown) {
      // Step-by-step explanation: Extracts detail string safely using the shared error handler
      const message = extractErrorDetail(err);
      setError(message || 'Error loading categories');
      console.error('Fetch categories error:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchCategories();
  }, [fetchCategories]);

  return {
    categories,
    loading,
    error,
    fetchCategories,
  };
};