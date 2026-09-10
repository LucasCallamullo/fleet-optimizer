import { useState } from 'react';
import geocodingApi, {
  type DistanceRequest,
  type DistanceResponse,
} from '../api/geocodingApi';
import { extractErrorDetail } from '@/shared/lib/errorHandler';

export interface UseGeocodingReturn {
  loading: boolean;
  result: DistanceResponse | null;
  error: string | null;
  calculateDistance: (payload: DistanceRequest) => Promise<void>;
  reset: () => void;
}

/**
 * Custom hook to encapsulate geocoding distance calculation requests
 */
export const useGeocoding = (): UseGeocodingReturn => {
  const [loading, setLoading] = useState<boolean>(false);
  const [result, setResult] = useState<DistanceResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  const calculateDistance = async (payload: DistanceRequest): Promise<void> => {
    setLoading(true);
    setError(null);
    setResult(null);

    try {
      // Step-by-step explanation: Unwraps data from the generic ApiResponse wrapper
      const response = await geocodingApi.calculateDistance(payload);
      setResult(response.data);
    } catch (err: unknown) {
      // Step-by-step explanation: Parses error detail via generic error handler
      const message = extractErrorDetail(err);
      setError(message || 'Failed to calculate distance');
    } finally {
      setLoading(false);
    }
  };

  const reset = (): void => {
    setResult(null);
    setError(null);
    setLoading(false);
  };

  return {
    loading,
    result,
    error,
    calculateDistance,
    reset,
  };
};