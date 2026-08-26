// src/features/packages/hooks/usePackages.js
import { useState, useEffect, useCallback } from 'react';
import packagesApi from '../services/packagesApi';

export const usePackages = () => {
  const [packages, setPackages] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchPackages = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await packagesApi.getAllDetailed();
      setPackages(data);
    } catch (err) {
      setError(err.message || 'Error fetching packages');
      console.error('Fetch packages error:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchPackages();
  }, [fetchPackages]);

  return {
    packages,
    loading,
    error,
    fetchPackages,
  };
};