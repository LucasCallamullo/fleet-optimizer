// src/features/packages/hooks/usePackages.ts
import { useState, useEffect, useCallback } from 'react';
import packagesApi from '@features/packages/api/packagesApi';
import type { PackageDetailDTO } from '@features/packages/types/packageTypes';
import { extractErrorDetail } from '@/shared/lib/errorHandler';

// ================================================================
// TYPES & INTERFACES
// ================================================================

export interface UsePackageReturn {
  pkg: PackageDetailDTO | null;
  loading: boolean;
  error: string | null;
  fetchPackage: () => Promise<void>;
}

export interface UsePackagesReturn {
  packages: PackageDetailDTO[];
  loading: boolean;
  error: string | null;
  fetchPackages: () => Promise<void>;
}

// ================================================================
// SINGLE PACKAGE HOOK
// ================================================================

export const usePackage = (id?: number | string): UsePackageReturn => {
  const [pkg, setPkg] = useState<PackageDetailDTO | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const fetchPackage = useCallback(async (): Promise<void> => {
    if (!id) return;

    setLoading(true);
    setError(null);
    try {
      // Step-by-step explanation: Unwraps single package detail object from response wrapper
      const response = await packagesApi.getById(id);
      setPkg(response.data || null);
    } catch (err: unknown) {
      // Step-by-step explanation: Extracts error message safely via shared handler
      const message = extractErrorDetail(err);
      setError(message || 'Error fetching package detail');
      console.error('Fetch package error:', err);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchPackage();
  }, [fetchPackage]);

  return { pkg, loading, error, fetchPackage };
};

// ================================================================
// COLLECTION PACKAGES HOOK
// ================================================================

export const usePackages = (): UsePackagesReturn => {
  const [packages, setPackages] = useState<PackageDetailDTO[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const fetchPackages = useCallback(async (): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      // Step-by-step explanation: Unwraps package array from response wrapper
      const response = await packagesApi.getAllDetailed();
      setPackages(response.data || []);
    } catch (err: unknown) {
      // Step-by-step explanation: Extracts error detail safely via shared handler
      const message = extractErrorDetail(err);
      setError(message || 'Error fetching packages');
      console.error('Fetch packages error:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchPackages();
  }, [fetchPackages]);

  return { packages, loading, error, fetchPackages };
};