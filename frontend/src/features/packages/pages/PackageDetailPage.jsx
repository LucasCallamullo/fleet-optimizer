// src/features/packages/pages/PackageDetailPage.jsx
import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Store, MapPin, Package, Weight, Box, User, Truck, Loader2 } from 'lucide-react';
import api from '@/shared/api/client';
import packagesApi from '../services/packagesApi';
import PackageStatusBadge from '../components/PackageStatusBadge';

// ================================================================
// PROVINCIAS - Para seleccionar destino
// ================================================================
const PROVINCIAS = [
  { 
    id: 'caba', 
    name: 'Buenos Aires (CABA)', 
    capital: 'Buenos Aires', 
    lat: -34.6037, 
    lon: -58.3816,
    street: 'Av. Colon',
    streetNumber: '500',
    city: 'Buenos Aires',
    state: 'CABA',
    country: 'Argentina',
    postalCode: '1000'
  },
  { 
    id: 'cordoba', 
    name: 'Córdoba', 
    capital: 'Córdoba', 
    lat: -31.4201, 
    lon: -64.1888,
    street: 'Av. Colon',
    streetNumber: '500',
    city: 'Cordoba',
    state: 'Cordoba',
    country: 'Argentina',
    postalCode: '5000'
  },
  { 
    id: 'mendoza', 
    name: 'Mendoza', 
    capital: 'Mendoza', 
    lat: -32.8908, 
    lon: -68.8272,
    street: 'Av. San Martin',
    streetNumber: '1000',
    city: 'Mendoza',
    state: 'Mendoza',
    country: 'Argentina',
    postalCode: '5500'
  },
];

export default function PackageDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [pkg, setPkg] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedDestination, setSelectedDestination] = useState('cordoba');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState(null);

  // State for vehicle selection
  const [vehicles, setVehicles] = useState([]);
  const [vehiclesLoading, setVehiclesLoading] = useState(false);
  const [selectedVehicle, setSelectedVehicle] = useState(null);

  // ================================================================
  // 1. FETCH PACKAGE DETAIL
  // ================================================================
  useEffect(() => {
    const fetchPackage = async () => {
      setLoading(true);
      setError(null);
      try {
        const data = await packagesApi.getById(id);
        setPkg(data);
      } catch (err) {
        setError(err.message || 'Error fetching package');
        console.error('Fetch package error:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchPackage();
  }, [id]);

  // ================================================================
  // 2. FETCH AVAILABLE VEHICLES
  // ================================================================
  const fetchAvailableVehicles = async () => {
    if (!pkg) return;
    
    setVehiclesLoading(true);
    try {
      const response = await api.get('/v1/vehicles/available-for-package', {
        params: {
          requiredWeightKg: pkg.totalWeightKg,
          requiredVolumeCbm: pkg.totalVolumeCbm,
        },
      });
      setVehicles(response.data);
    } catch (err) {
      console.error('Error fetching vehicles:', err);
      setVehicles([]);
    } finally {
      setVehiclesLoading(false);
    }
  };

  // Fetch vehicles when package is loaded
  useEffect(() => {
    if (pkg) {
      fetchAvailableVehicles();
    }
  }, [pkg]);

  // ================================================================
  // 3. HANDLE SUBMIT - CREATE SHIPMENT / ROUTE
  // ================================================================
  const handleCreateRoute = async () => {
    // Validation
    if (!selectedVehicle) {
      setSubmitError('Please select a vehicle');
      return;
    }

    if (!selectedDestination) {
      setSubmitError('Please select a destination');
      return;
    }

    const dest = PROVINCIAS.find(p => p.id === selectedDestination);
    if (!dest) {
      setSubmitError('Invalid destination selected');
      return;
    }

    setIsSubmitting(true);
    setSubmitError(null);

    try {
      // Build the payload
      const payload = {
        packageIds: [pkg.id],
        vehicleId: selectedVehicle.id,
        destination: {
          street: dest.street || 'Av. Default',
          streetNumber: dest.streetNumber || '100',
          city: dest.city || dest.capital,
          state: dest.state || dest.name,
          country: 'Argentina',
          postalCode: dest.postalCode || '5000',
          latitude: dest.lat,
          longitude: dest.lon
        }
      };

      console.log('Creating shipment with payload:', payload);

      // Send to backend
      const response = await api.post('/api/v1/shipments', payload);
      
      console.log('Shipment created:', response.data);

      // Redirect to route detail page
      if (response.data.routeId) {
        navigate(`/routes/${response.data.routeId}`);
      } else if (response.data.id) {
        navigate(`/routes/${response.data.id}`);
      } else {
        // Fallback: go to routes list
        navigate('/routes');
      }

    } catch (err) {
      console.error('Error creating route:', err);
      setSubmitError(err.response?.data?.message || err.message || 'Error creating route');
    } finally {
      setIsSubmitting(false);
    }
  };

  // ================================================================
  // 4. LOADING
  // ================================================================
  if (loading) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-8">
        <p className="text-center text-gray-500 animate-pulse">Loading package details...</p>
      </div>
    );
  }

  // ================================================================
  // 5. ERROR
  // ================================================================
  if (error || !pkg) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-8">
        <div className="bg-white rounded-xl border border-red-200 shadow-sm p-6">
          <p className="text-red-600">{error || 'Package not found'}</p>
          <button
            onClick={() => navigate('/packages')}
            className="mt-4 text-blue-600 hover:text-blue-800 font-medium"
          >
            ← Back to packages
          </button>
        </div>
      </div>
    );
  }

  const destination = PROVINCIAS.find(p => p.id === selectedDestination);

  return (
    <div className="max-w-4xl mx-auto px-4 py-8 space-y-6">
      {/* Back Button */}
      <button
        onClick={() => navigate('/packages')}
        className="flex items-center gap-2 text-gray-500 hover:text-gray-700 transition-colors"
      >
        <ArrowLeft className="h-4 w-4" />
        Back to packages
      </button>

      {/* ============================================================ */}
      {/* HEADER */}
      {/* ============================================================ */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-800 flex items-center gap-2">
            <Package className="h-6 w-6 text-blue-500" />
            Create Route
          </h1>
          <p className="text-sm text-gray-500">
            Tracking: <span className="font-mono font-medium">{pkg.trackingNumber}</span>
          </p>
        </div>
        <PackageStatusBadge status={pkg.status} />
      </div>

      {/* ============================================================ */}
      {/* PACKAGE INFO GRID */}
      {/* ============================================================ */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6">
          <h3 className="text-sm font-semibold text-gray-700 mb-4 flex items-center gap-2">
            <Package className="h-4 w-4 text-gray-400" />
            Package Information
          </h3>
          <div className="space-y-3">
            <div className="flex justify-between border-b border-gray-100 pb-2">
              <span className="text-gray-500 text-sm">ID</span>
              <span className="text-gray-700 font-mono text-sm">#{pkg.id}</span>
            </div>
            <div className="flex justify-between border-b border-gray-100 pb-2">
              <span className="text-gray-500 text-sm">Tracking Number</span>
              <span className="text-gray-700 font-mono text-sm">{pkg.trackingNumber}</span>
            </div>
            <div className="flex justify-between border-b border-gray-100 pb-2">
              <span className="text-gray-500 text-sm flex items-center gap-1">
                <Weight className="h-3.5 w-3.5" />
                Weight
              </span>
              <span className="text-gray-700">{pkg.totalWeightKg} kg</span>
            </div>
            <div className="flex justify-between border-b border-gray-100 pb-2">
              <span className="text-gray-500 text-sm flex items-center gap-1">
                <Box className="h-3.5 w-3.5" />
                Volume
              </span>
              <span className="text-gray-700">{pkg.totalVolumeCbm} m³</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-500 text-sm flex items-center gap-1">
                <User className="h-3.5 w-3.5" />
                Owner
              </span>
              <span className="text-gray-700 font-mono text-xs truncate max-w-[150px]">
                {pkg.ownerId}
              </span>
            </div>
          </div>
        </div>

        {/* Store Info */}
        <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6">
          <h3 className="text-sm font-semibold text-gray-700 mb-4 flex items-center gap-2">
            <Store className="h-4 w-4 text-gray-400" />
            Store (Origin)
          </h3>
          {pkg.store ? (
            <div className="space-y-3">
              <div className="flex justify-between border-b border-gray-100 pb-2">
                <span className="text-gray-500 text-sm">Name</span>
                <span className="text-gray-700 font-medium">{pkg.store.name}</span>
              </div>
              <div className="flex justify-between border-b border-gray-100 pb-2">
                <span className="text-gray-500 text-sm">Address</span>
                <span className="text-gray-700 text-sm text-right">
                  {pkg.store.location?.street} {pkg.store.location?.streetNumber}
                  <br />
                  {pkg.store.location?.city}, {pkg.store.location?.state}
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500 text-sm flex items-center gap-1">
                  <MapPin className="h-3.5 w-3.5" />
                  Coordinates
                </span>
                <span className="text-gray-700 font-mono text-xs">
                  {pkg.store.location?.latitude}, {pkg.store.location?.longitude}
                </span>
              </div>
            </div>
          ) : (
            <p className="text-gray-400 text-sm">No store associated</p>
          )}
        </div>
      </div>

      {/* ============================================================ */}
      {/* DESTINATION SELECTOR */}
      {/* ============================================================ */}
      <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6">
        <h3 className="text-sm font-semibold text-gray-700 mb-4 flex items-center gap-2">
          <MapPin className="h-4 w-4 text-red-500" />
          Destination Selection
        </h3>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-600 mb-1.5">
              Select Destination
            </label>
            <select
              value={selectedDestination}
              onChange={(e) => setSelectedDestination(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              {PROVINCIAS.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name} ({p.capital})
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-600 mb-1.5">
              Address Details
            </label>
            <div className="grid grid-cols-2 gap-2">
              <input
                type="text"
                placeholder="Street"
                value={destination?.street || ''}
                disabled
                className="px-3 py-2 border border-gray-300 rounded-lg bg-gray-50 text-gray-600"
              />
              <input
                type="text"
                placeholder="Number"
                value={destination?.streetNumber || ''}
                disabled
                className="px-3 py-2 border border-gray-300 rounded-lg bg-gray-50 text-gray-600"
              />
            </div>
            <p className="text-xs text-gray-400 mt-1">
              Address auto-filled from selected province
            </p>
          </div>
        </div>

        {destination && (
          <div className="mt-4 p-3 bg-blue-50 rounded-lg border border-blue-100">
            <div className="flex items-center gap-2 text-sm text-blue-700">
              <MapPin className="h-4 w-4" />
              <span className="font-medium">Destination:</span>
              <span>{destination.name} - {destination.capital}</span>
              <span className="text-xs text-blue-500 font-mono">
                ({destination.lat}, {destination.lon})
              </span>
            </div>
          </div>
        )}
      </div>

      {/* ============================================================ */}
      {/* VEHICLE SELECTOR */}
      {/* ============================================================ */}
      <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6">
        <h3 className="text-sm font-semibold text-gray-700 mb-4 flex items-center gap-2">
          <Truck className="h-4 w-4 text-green-500" />
          Select Vehicle
        </h3>

        <div className="space-y-3">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600">
                Required capacity:
                <span className="font-medium text-gray-800 ml-1">
                  {pkg.totalWeightKg} kg / {pkg.totalVolumeCbm} m³
                </span>
              </p>
              <p className="text-xs text-gray-400">
                Vehicles with sufficient capacity and AVAILABLE status
              </p>
            </div>
          </div>

          {vehiclesLoading ? (
            <div className="text-center py-4 text-gray-500">
              Loading available vehicles...
            </div>
          ) : vehicles.length === 0 ? (
            <div className="text-center py-4 text-yellow-600 bg-yellow-50 rounded-lg border border-yellow-200">
              No available vehicles with sufficient capacity found.
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
              {vehicles.map((vehicle) => (
                <div
                  key={vehicle.id}
                  onClick={() => setSelectedVehicle(vehicle)}
                  className={`p-3 border rounded-lg cursor-pointer transition-all ${
                    selectedVehicle?.id === vehicle.id
                      ? 'border-blue-500 bg-blue-50 ring-2 ring-blue-200'
                      : 'border-gray-200 hover:border-gray-300 hover:bg-gray-50'
                  }`}
                >
                  <div className="flex justify-between items-start">
                    <div>
                      <div className="font-mono font-medium text-gray-800">
                        {vehicle.licensePlate}
                      </div>
                      <div className="text-xs text-gray-500">
                        {vehicle.category?.name || 'Uncategorized'}
                      </div>
                    </div>
                    <span className="text-xs px-2 py-0.5 bg-green-100 text-green-700 rounded-full">
                      Available
                    </span>
                  </div>
                  <div className="mt-2 text-xs text-gray-500 space-y-0.5">
                    <div>Weight: {vehicle.maxWeightKg || '—'} kg</div>
                    <div>Volume: {vehicle.maxVolumeCbm || '—'} m³</div>
                    <div>Year: {vehicle.year}</div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* ============================================================ */}
      {/* SUBMIT ERROR */}
      {/* ============================================================ */}
      {submitError && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4">
          <p className="text-red-600 text-sm">{submitError}</p>
        </div>
      )}

      {/* ============================================================ */}
      {/* ACTION BUTTONS */}
      {/* ============================================================ */}
      <div className="flex gap-4">
        <button
          onClick={handleCreateRoute}
          disabled={isSubmitting || !selectedVehicle || !selectedDestination}
          className="px-6 py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
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
          onClick={() => navigate('/packages')}
          className="px-6 py-2 border border-gray-200 hover:bg-gray-50 text-gray-600 font-medium rounded-lg transition-colors"
        >
          Cancel
        </button>
      </div>
    </div>
  );
}