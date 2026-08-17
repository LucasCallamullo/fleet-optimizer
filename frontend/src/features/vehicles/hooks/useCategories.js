// src/features/vehicles/hooks/useCategories.js
import { useState, useEffect, useCallback } from 'react';
import categoriesApi from '@vehicles/api/categoriesApi';

/**
 * useCategories - Custom hook for managing categories
 * 
 * @returns {Object} Categories state and functions
 * @property {Array} categories - List of categories
 * @property {boolean} loading - Loading state
 * @property {string|null} error - Error message
 * @property {Function} fetchCategories - Refetch categories
 * 
 * @example
 * const { categories, loading, error } = useCategories();
 */
export const useCategories = () => {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchCategories = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await categoriesApi.getAll();
      setCategories(data);
    } catch (err) {
      setError(err.message || 'Error loading categories');
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