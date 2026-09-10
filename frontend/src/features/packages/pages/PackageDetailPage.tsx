import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Store, MapPin, Package, Weight, Box, User, Truck, Loader2 } from 'lucide-react';
import api from '@/shared/api/client';
import vehiclesApi from '@/features/vehicles/api/vehiclesApi';
import { usePackage } from '../hooks/usePackages';
import PackageStatusBadge from '../components/PackageStatusBadge';
import type { VehicleDetail } from '@/features/vehicles/types/vehiclesTypes';
import type { ShipmentRequestDTO, ShipmentResponseDTO } from '@/features/routes/types/routeTypes';
import type { ApiResponse } from '@/shared/types/commonTypes';
import { extractErrorMessage } from '@/shared/lib/errorHandler';

// ================================================================
// PROVINCIAS - DESTINATION PRESETS
// ================================================================

interface ProvinciaPreset {
  id: string;
  name: string;
  capital: string;
  lat: number;
  lon: number;
  street: string;
  streetNumber: string;
  city: string;
  state: string;
  country: string;
  postalCode: string;
}

const PROVINCIAS: ProvinciaPreset[] = [
  {
    id: 'caba',
    name: 'Buenos Aires (CABA)',
    capital: 'Buenos Aires',
    lat: -34.6037,
    lon: -58.3816,
    street: 'Av. Libertador',
    streetNumber: '1000',
    city: 'Buenos Aires',
    state: 'CABA',
    country: 'Argentina',
    postalCode: '1000',
  },
  {
    id: 'cordoba',
    name: 'Córdoba',
    capital: 'Córdoba',
    lat: -31.4201,
    lon: -64.1888,
    street: 'Av. Colón',
    streetNumber: '500',
    city: 'Córdoba',
    state: 'Córdoba',
    country: 'Argentina',
    postalCode: '5000',
  },
  {
    id: 'mendoza',
    name: 'Mendoza',
    capital: 'Mendoza',
    lat: -32.8908,
    lon: -68.8272,
    street: 'Av. San Martín',
    streetNumber: '1000',
    city: 'Mendoza',
    state: 'Mendoza',
    country: 'Argentina',
    postalCode: '5500',
  },
];

// ================================================================
// COMPONENT
// ================================================================

export default function PackageDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  // Custom hook for single package fetching
  const { pkg, loading, error } = usePackage(id);

  // Local state for shipment generation
  const [selectedDestination, setSelectedDestination] = useState<string>('cordoba');
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  // Vehicles state
  const [vehicles, setVehicles] = useState<VehicleDetail[]>([]);
  const [vehiclesLoading, setVehiclesLoading] = useState<boolean>(false);
  const [selectedVehicle, setSelectedVehicle] = useState<VehicleDetail | null>(null);

  // ================================================================
  // 1. FETCH AVAILABLE VEHICLES
  // ================================================================
  const fetchAvailableVehicles = useCallback(async () => {
    if (!pkg) return;

    setVehiclesLoading(true);
    try {
      // Step-by-step explanation: Issue request using vehicles API with minimum weight filter
      const response = await vehiclesApi.getAvailable({
        minCapacity: pkg.totalWeightKg,
      });
      // Cast list to VehicleDetail array for category field access
      setVehicles((response.data as unknown as VehicleDetail[]) || []);
    } catch (err: unknown) {
      console.error('Error fetching available vehicles:', err);
      setVehicles([]);
    } finally {
      setVehiclesLoading(false);
    }
  }, [pkg]);

  useEffect(() => {
    if (pkg) {
      fetchAvailableVehicles();
    }
  }, [pkg, fetchAvailableVehicles]);

  // ================================================================
  // 2. HANDLE SUBMIT - CREATE SHIPMENT / ROUTE
  // ================================================================
  const handleCreateRoute = async () => {
    if (!pkg) return;

    if (!selectedVehicle) {
      setSubmitError('Please select a vehicle');
      return;
    }

    const dest = PROVINCIAS.find((p) => p.id === selectedDestination);
    if (!dest) {
      setSubmitError('Invalid destination selected');
      return;
    }

    setIsSubmitting(true);
    setSubmitError(null);

    try {
      // Construct strongly typed shipment payload
      const payload: ShipmentRequestDTO = {
        packageIds: [pkg.id],
        vehicleId: selectedVehicle.id,
        destination: {
          street: dest.street,
          streetNumber: dest.streetNumber,
          city: dest.city,
          state: dest.state,
          country: dest.country,
          postalCode: dest.postalCode,
          latitude: dest.lat,
          longitude: dest.lon,
        },
      };

      // Step-by-step explanation: Issue POST request to create shipment and unwrap ApiResponse payload
      const response = await api.post<ApiResponse<ShipmentResponseDTO>>('/v1/shipments', payload);
      const shipmentData = response.data.data;

      if (shipmentData?.routeId) {
        navigate(`/routes/${shipmentData.routeId}`);
      } else {
        navigate('/routes');
      }
    } catch (err: unknown) {
      console.error('Error creating route:', err);
      const message = extractErrorMessage(err);
      setSubmitError(message || 'Error creating route');
    } finally {
      setIsSubmitting(false);
    }
  };

  // ================================================================
  // 3. LOADING & ERROR STATES
  // ================================================================
  if (loading) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-8">
        <p className="text-center text-muted-foreground animate-pulse">Loading package details...</p>
      </div>
    );
  }

  if (error || !pkg) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-8">
        <div className="bg-card rounded-xl border border-destructive/30 p-6 text-card-foreground shadow-sm">
          <p className="text-destructive font-medium">{error || 'Package not found'}</p>
          <button
            type="button"
            onClick={() => navigate('/packages')}
            className="mt-4 text-primary hover:underline text-sm font-medium"
          >
            ← Back to packages
          </button>
        </div>
      </div>
    );
  }

  const destination = PROVINCIAS.find((p) => p.id === selectedDestination);

  // ================================================================
  // 4. MAIN RENDER
  // ================================================================
  return (
    <div className="max-w-4xl mx-auto px-4 py-8 space-y-6 text-foreground">
      {/* Back Button */}
      <button
        type="button"
        onClick={() => navigate('/packages')}
        className="flex items-center gap-2 text-muted-foreground hover:text-foreground transition-colors text-sm"
      >
        <ArrowLeft className="h-4 w-4" />
        Back to packages
      </button>

      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground flex items-center gap-2">
            <Package className="h-6 w-6 text-primary" />
            Create Route
          </h1>
          <p className="text-sm text-muted-foreground">
            Tracking: <span className="font-mono font-medium text-foreground">{pkg.trackingNumber}</span>
          </p>
        </div>
        <PackageStatusBadge status={pkg.status} />
      </div>

      {/* Package Info Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="bg-card border border-border rounded-xl shadow-sm p-6 text-card-foreground">
          <h3 className="text-sm font-semibold text-foreground mb-4 flex items-center gap-2">
            <Package className="h-4 w-4 text-muted-foreground" />
            Package Information
          </h3>
          <div className="space-y-3">
            <div className="flex justify-between border-b border-border pb-2">
              <span className="text-muted-foreground text-sm">ID</span>
              <span className="font-mono text-sm">#{pkg.id}</span>
            </div>
            <div className="flex justify-between border-b border-border pb-2">
              <span className="text-muted-foreground text-sm">Tracking Number</span>
              <span className="font-mono text-sm">{pkg.trackingNumber}</span>
            </div>
            <div className="flex justify-between border-b border-border pb-2">
              <span className="text-muted-foreground text-sm flex items-center gap-1">
                <Weight className="h-3.5 w-3.5" />
                Weight
              </span>
              <span>{pkg.totalWeightKg} kg</span>
            </div>
            <div className="flex justify-between border-b border-border pb-2">
              <span className="text-muted-foreground text-sm flex items-center gap-1">
                <Box className="h-3.5 w-3.5" />
                Volume
              </span>
              <span>{pkg.totalVolumeCbm} m³</span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground text-sm flex items-center gap-1">
                <User className="h-3.5 w-3.5" />
                Owner
              </span>
              <span className="font-mono text-xs truncate max-w-37.5">
                {pkg.ownerId}
              </span>
            </div>
          </div>
        </div>

        {/* Store Info */}
        <div className="bg-card border border-border rounded-xl shadow-sm p-6 text-card-foreground">
          <h3 className="text-sm font-semibold text-foreground mb-4 flex items-center gap-2">
            <Store className="h-4 w-4 text-muted-foreground" />
            Store (Origin)
          </h3>
          {pkg.store ? (
            <div className="space-y-3">
              <div className="flex justify-between border-b border-border pb-2">
                <span className="text-muted-foreground text-sm">Name</span>
                <span className="font-medium">{pkg.store.name}</span>
              </div>
              <div className="flex justify-between border-b border-border pb-2">
                <span className="text-muted-foreground text-sm">Address</span>
                <span className="text-sm text-right">
                  {pkg.store.location?.street} {pkg.store.location?.streetNumber}
                  <br />
                  {pkg.store.location?.city}, {pkg.store.location?.state}
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground text-sm flex items-center gap-1">
                  <MapPin className="h-3.5 w-3.5" />
                  Coordinates
                </span>
                <span className="font-mono text-xs">
                  {pkg.store.location?.latitude}, {pkg.store.location?.longitude}
                </span>
              </div>
            </div>
          ) : (
            <p className="text-muted-foreground text-sm">No store associated</p>
          )}
        </div>
      </div>

      {/* Destination Selector */}
      <div className="bg-card border border-border rounded-xl shadow-sm p-6 text-card-foreground">
        <h3 className="text-sm font-semibold text-foreground mb-4 flex items-center gap-2">
          <MapPin className="h-4 w-4 text-destructive" />
          Destination Selection
        </h3>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label htmlFor="destination-select" className="block text-sm font-medium text-muted-foreground mb-1.5">
              Select Destination
            </label>
            <select
              id="destination-select"
              value={selectedDestination}
              onChange={(e) => setSelectedDestination(e.target.value)}
              className="w-full px-3 py-2 border border-border rounded-lg bg-background text-foreground focus:ring-2 focus:ring-ring focus:border-transparent text-sm"
            >
              {PROVINCIAS.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name} ({p.capital})
                </option>
              ))}
            </select>
          </div>

          <div>
            <span className="block text-sm font-medium text-muted-foreground mb-1.5">
              Address Details
            </span>
            <div className="grid grid-cols-2 gap-2">
              <input
                type="text"
                placeholder="Street"
                value={destination?.street || ''}
                disabled
                className="px-3 py-2 border border-border rounded-lg bg-muted text-muted-foreground text-sm"
              />
              <input
                type="text"
                placeholder="Number"
                value={destination?.streetNumber || ''}
                disabled
                className="px-3 py-2 border border-border rounded-lg bg-muted text-muted-foreground text-sm"
              />
            </div>
            <p className="text-xs text-muted-foreground mt-1">
              Address auto-filled from selected province
            </p>
          </div>
        </div>

        {destination && (
          <div className="mt-4 p-3 bg-primary/10 rounded-lg border border-primary/20">
            <div className="flex items-center gap-2 text-sm text-foreground">
              <MapPin className="h-4 w-4 text-primary" />
              <span className="font-medium">Destination:</span>
              <span>{destination.name} - {destination.capital}</span>
              <span className="text-xs text-muted-foreground font-mono">
                ({destination.lat}, {destination.lon})
              </span>
            </div>
          </div>
        )}
      </div>

      {/* Vehicle Selector */}
      <div className="bg-card border border-border rounded-xl shadow-sm p-6 text-card-foreground">
        <h3 className="text-sm font-semibold text-foreground mb-4 flex items-center gap-2">
          <Truck className="h-4 w-4 text-emerald-500" />
          Select Vehicle
        </h3>

        <div className="space-y-3">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-muted-foreground">
                Required capacity:
                <span className="font-medium text-foreground ml-1">
                  {pkg.totalWeightKg} kg / {pkg.totalVolumeCbm} m³
                </span>
              </p>
              <p className="text-xs text-muted-foreground">
                Vehicles with sufficient capacity and AVAILABLE status
              </p>
            </div>
          </div>

          {vehiclesLoading ? (
            <div className="text-center py-4 text-muted-foreground text-sm">
              Loading available vehicles...
            </div>
          ) : vehicles.length === 0 ? (
            <div className="text-center py-4 text-amber-600 dark:text-amber-400 bg-amber-500/10 rounded-lg border border-amber-500/20 text-sm">
              No available vehicles with sufficient capacity found.
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
              {vehicles.map((vehicle) => {
                const isSelected = selectedVehicle?.id === vehicle.id;
                return (
                  <button
                    type="button"
                    key={vehicle.id}
                    onClick={() => setSelectedVehicle(vehicle)}
                    className={`p-3 border rounded-lg text-left transition-all w-full ${
                      isSelected
                        ? 'border-primary bg-primary/10 ring-2 ring-primary/20'
                        : 'border-border bg-background hover:bg-muted'
                    }`}
                  >
                    <div className="flex justify-between items-start">
                      <div>
                        <div className="font-mono font-medium text-foreground text-sm">
                          {vehicle.licensePlate}
                        </div>
                        <div className="text-xs text-muted-foreground">
                          {vehicle.category?.name || 'Uncategorized'}
                        </div>
                      </div>
                      <span className="text-xs px-2 py-0.5 bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border border-emerald-500/20 rounded-full">
                        Available
                      </span>
                    </div>
                    <div className="mt-2 text-xs text-muted-foreground space-y-0.5">
                      <div>Weight: {vehicle.maxWeightKg || '—'} kg</div>
                      <div>Volume: {vehicle.maxVolumeCbm || '—'} m³</div>
                      <div>Year: {vehicle.year}</div>
                    </div>
                  </button>
                );
              })}
            </div>
          )}
        </div>
      </div>

      {/* Submit Error */}
      {submitError && (
        <div className="bg-destructive/10 border border-destructive/20 rounded-lg p-4">
          <p className="text-destructive text-sm font-medium">{submitError}</p>
        </div>
      )}

      {/* Action Buttons */}
      <div className="flex gap-4">
        <button
          type="button"
          onClick={handleCreateRoute}
          disabled={isSubmitting || !selectedVehicle || !selectedDestination}
          className="px-6 py-2 bg-primary text-primary-foreground hover:bg-primary/90 font-medium rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2 text-sm"
        >
          {isSubmitting ? (
            <>
              <Loader2 className="h-4 w-4 animate-spin" />
              Creating Route...
            </>
          ) : (
            'Continue to Route Creation'
          )}
        </button>
        <button
          type="button"
          onClick={() => navigate('/packages')}
          className="px-6 py-2 border border-border bg-card hover:bg-muted text-foreground font-medium rounded-lg transition-colors text-sm"
        >
          Cancel
        </button>
      </div>
    </div>
  );
}