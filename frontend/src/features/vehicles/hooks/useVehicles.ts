import { useState, useEffect, useCallback } from 'react';
import vehiclesApi from '@/features/vehicles/api/vehiclesApi';
import type { VehicleDetail, VehicleDTO } from '@/features/vehicles/types/vehiclesTypes';
import { extractErrorMessage, extractErrorDetail } from '@/shared/lib/errorHandler';

// ================================================================
// TYPES & INTERFACES
// ================================================================

export interface UseVehiclesReturn {
  vehicles: VehicleDetail[];
  loading: boolean;
  error: string | null;
  isSaving: boolean;
  editingVehicle: VehicleDetail | null;
  setEditingVehicle: React.Dispatch<React.SetStateAction<VehicleDetail | null>>;
  createVehicle: (vehicleData: VehicleDTO) => Promise<boolean>;
  updateVehicle: (id: number | string, vehicleData: Partial<VehicleDTO>) => Promise<boolean>;
  deleteVehicle: (id: number | string) => Promise<boolean>;
  fetchVehicles: () => Promise<void>;
}

// ================================================================
// CUSTOM HOOK
// ================================================================

export const useVehicles = (): UseVehiclesReturn => {
  // Main data: Array of detailed vehicles from the API
  const [vehicles, setVehicles] = useState<VehicleDetail[]>([]);

  // Loading state: Indicates active network request
  const [loading, setLoading] = useState<boolean>(true);

  // Error state: Stores error message rendered in UI
  const [error, setError] = useState<string | null>(null);

  // Saving state: Indicates an active create/update operation
  const [isSaving, setIsSaving] = useState<boolean>(false);

  // Edit state: Selected vehicle for updates (null = creating new)
  const [editingVehicle, setEditingVehicle] = useState<VehicleDetail | null>(null);

  /**
   * fetchVehicles() - FETCH DATA FROM API
   */
  const fetchVehicles = useCallback(async (): Promise<void> => {
    try {
      setLoading(true);
      setError(null);

      // Step-by-step explanation: Issue GET request and unwrap response payload
      const response = await vehiclesApi.getAll();
      setVehicles(response.data || []);

    } catch (err: unknown) {
      console.error('Error fetching vehicles:', err);
      // Step-by-step explanation: Safely extract error message using shared helper
      const message = extractErrorDetail(err);
      setError(message || 'No se pudo cargar la flota de vehículos.');
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * createVehicle() - CREATE NEW VEHICLE
   */
  const createVehicle = useCallback(
    async (vehicleData: VehicleDTO): Promise<boolean> => {
      try {
        setIsSaving(true);

        // Step-by-step explanation: Issue POST request to create new vehicle entry
        await vehiclesApi.create(vehicleData);

        // Refresh fleet list after successful creation
        await fetchVehicles();
        return true;
      } catch (err: unknown) {
        console.error('Error creating vehicle:', err);
        const errorMessage = extractErrorMessage(err);
        alert(errorMessage);
        return false;
      } finally {
        setIsSaving(false);
      }
    },
    [fetchVehicles]
  );

  /**
   * updateVehicle() - UPDATE EXISTING VEHICLE
   */
  const updateVehicle = useCallback(
    async (id: number | string, vehicleData: Partial<VehicleDTO>): Promise<boolean> => {
      try {
        setIsSaving(true);

        // Step-by-step explanation: Issue PUT request to update specified vehicle record
        await vehiclesApi.update(id, vehicleData);

        await fetchVehicles();
        setEditingVehicle(null);
        return true;
      } catch (err: unknown) {
        console.error('Error updating vehicle:', err);
        const errorMessage = extractErrorMessage(err);
        alert(errorMessage || 'Hubo un error al intentar actualizar el vehículo.');
        return false;
      } finally {
        setIsSaving(false);
      }
    },
    [fetchVehicles]
  );

  /**
   * deleteVehicle() - DELETE VEHICLE
   */
  const deleteVehicle = useCallback(
    async (id: number | string): Promise<boolean> => {
      if (!confirm('¿Estás seguro de que querés eliminar este vehículo?')) {
        return false;
      }

      try {
        // Step-by-step explanation: Issue DELETE request targeting vehicle ID
        await vehiclesApi.delete(id);
        await fetchVehicles();
        return true;
      } catch (err: unknown) {
        console.error('Error deleting vehicle:', err);
        const errorMessage = extractErrorMessage(err);
        alert(errorMessage || 'Hubo un error al intentar eliminar el vehículo.');
        return false;
      }
    },
    [fetchVehicles]
  );

  useEffect(() => {
    fetchVehicles();
  }, [fetchVehicles]);

  return {
    vehicles,
    loading,
    error,
    isSaving,
    editingVehicle,
    setEditingVehicle,
    createVehicle,
    updateVehicle,
    deleteVehicle,
    fetchVehicles,
  };
};