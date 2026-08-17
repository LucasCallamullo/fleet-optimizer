// src/features/geocoding/pages/GeocodingPage.jsx
import { useState } from 'react';
import { MapPin, ArrowRight, Loader2, Ruler, Clock, CheckCircle, XCircle, Globe } from 'lucide-react';
import geocodingApi from '../api/geocodingApi';

// ================================================================
// DATA - Provincias con sus capitales
// ================================================================
const PROVINCIAS = [
  { id: 'buenos-aires', name: 'Buenos Aires', capital: 'La Plata', lat: -34.9215, lon: -57.9545 },
  { id: 'caba', name: 'CABA', capital: 'Buenos Aires', lat: -34.6037, lon: -58.3816 },
  { id: 'cordoba', name: 'Córdoba', capital: 'Córdoba', lat: -31.4201, lon: -64.1888 },
  { id: 'santa-fe', name: 'Santa Fe', capital: 'Santa Fe', lat: -31.6333, lon: -60.7000 },
  { id: 'mendoza', name: 'Mendoza', capital: 'Mendoza', lat: -32.8908, lon: -68.8272 },
  { id: 'tucuman', name: 'Tucumán', capital: 'San Miguel de Tucumán', lat: -26.8083, lon: -65.2176 },
  { id: 'salta', name: 'Salta', capital: 'Salta', lat: -24.7859, lon: -65.4117 },
  { id: 'jujuy', name: 'Jujuy', capital: 'San Salvador de Jujuy', lat: -24.1858, lon: -65.2995 },
  { id: 'neuquen', name: 'Neuquén', capital: 'Neuquén', lat: -38.9516, lon: -68.0591 },
  { id: 'rionegro', name: 'Río Negro', capital: 'Viedma', lat: -40.8139, lon: -62.9967 },
  { id: 'chubut', name: 'Chubut', capital: 'Rawson', lat: -43.3000, lon: -65.1000 },
  { id: 'santacruz', name: 'Santa Cruz', capital: 'Río Gallegos', lat: -51.6227, lon: -69.2181 },
  { id: 'tierradelfuego', name: 'Tierra del Fuego', capital: 'Ushuaia', lat: -54.8019, lon: -68.3030 },
  { id: 'misiones', name: 'Misiones', capital: 'Posadas', lat: -27.3671, lon: -55.8960 },
  { id: 'corrientes', name: 'Corrientes', capital: 'Corrientes', lat: -27.4692, lon: -58.8306 },
  { id: 'entre-rios', name: 'Entre Ríos', capital: 'Paraná', lat: -31.7333, lon: -60.5333 },
  { id: 'chaco', name: 'Chaco', capital: 'Resistencia', lat: -27.4514, lon: -58.9866 },
  { id: 'formosa', name: 'Formosa', capital: 'Formosa', lat: -26.1776, lon: -58.1781 },
  { id: 'santiagodelestero', name: 'Santiago del Estero', capital: 'Santiago del Estero', lat: -27.7833, lon: -64.2667 },
  { id: 'catamarca', name: 'Catamarca', capital: 'San Fernando del Valle de Catamarca', lat: -28.4696, lon: -65.7852 },
  { id: 'larioja', name: 'La Rioja', capital: 'La Rioja', lat: -29.4131, lon: -66.8557 },
  { id: 'sanluis', name: 'San Luis', capital: 'San Luis', lat: -33.2950, lon: -66.3356 },
  { id: 'sanjuan', name: 'San Juan', capital: 'San Juan', lat: -31.5375, lon: -68.5364 },
];

export default function GeocodingPage() {
  const [originId, setOriginId] = useState('caba');
  const [destId, setDestId] = useState('cordoba');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);

  const origin = PROVINCIAS.find(p => p.id === originId);
  const destination = PROVINCIAS.find(p => p.id === destId);

  const handleCalculate = async () => {
    if (!origin || !destination) return;

    setLoading(true);
    setError(null);
    setResult(null);

    try {
      const response = await geocodingApi.calculateDistance({
        originLat: origin.lat,
        originLon: origin.lon,
        destLat: destination.lat,
        destLon: destination.lon,
      });
      setResult(response);
    } catch (err) {
      setError(err.message || 'Error calculating distance');
    } finally {
      setLoading(false);
    }
  };

  const handleSwap = () => {
    setOriginId(destId);
    setDestId(originId);
  };

  // Formatear distancia
  const formatDistance = (km) => {
    if (!km) return '—';
    if (km > 1000) return `${(km / 1000).toFixed(1)} km`;
    return `${km.toFixed(1)} km`;
  };

  const formatDuration = (minutes) => {
    if (!minutes) return '—';
    const hours = Math.floor(minutes / 60);
    const mins = Math.round(minutes % 60);
    if (hours > 0) {
      return `${hours}h ${mins}min`;
    }
    return `${mins}min`;
  };

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-800">
          <span className="flex items-center justify-center gap-1.5">
            <Globe className="h-4 w-4" />
            Geocoding
          </span>
        </h1>
        <p className="text-gray-500">Calculate distances and travel times between provinces</p>
      </div>

      {/* ============================================================ */}
      {/* SELECTORS */}
      {/* ============================================================ */}
      <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6 mb-6">
        <div className="grid grid-cols-1 md:grid-cols-[1fr,auto,1fr] gap-4 items-center">
          {/* Origin */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">
              <span className="flex items-center gap-1.5">
                <MapPin className="h-4 w-4 text-blue-500" />
                Origin
              </span>
            </label>
            <select
              value={originId}
              onChange={(e) => setOriginId(e.target.value)}
              disabled={loading}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:opacity-60"
            >
              {PROVINCIAS.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name} - {p.capital}
                </option>
              ))}
            </select>
            {origin && (
              <div className="text-xs text-gray-400 mt-1">
                {origin.capital}: {origin.lat}, {origin.lon}
              </div>
            )}
          </div>

          {/* Swap Button */}
          <div className="flex justify-center pt-5">
            <button
              onClick={handleSwap}
              disabled={loading}
              className="p-2 rounded-full border border-gray-200 hover:bg-gray-50 transition-colors disabled:opacity-50"
            >
              <ArrowRight className="h-5 w-5 text-gray-400 rotate-90 md:rotate-0" />
            </button>
          </div>

          {/* Destination */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">
              <span className="flex items-center gap-1.5">
                <MapPin className="h-4 w-4 text-red-500" />
                Destination
              </span>
            </label>
            <select
              value={destId}
              onChange={(e) => setDestId(e.target.value)}
              disabled={loading}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:opacity-60"
            >
              {PROVINCIAS.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name} - {p.capital}
                </option>
              ))}
            </select>
            {destination && (
              <div className="text-xs text-gray-400 mt-1">
                {destination.capital}: {destination.lat}, {destination.lon}
              </div>
            )}
          </div>
        </div>

        {/* Calculate Button */}
        <button
          onClick={handleCalculate}
          disabled={loading || !origin || !destination}
          className="mt-4 w-full py-2.5 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
        >
          {loading ? (
            <>
              <Loader2 className="h-4 w-4 animate-spin" />
              Calculating...
            </>
          ) : (
            <>
              <Ruler className="h-4 w-4" />
              Calculate Distance
            </>
          )}
        </button>
      </div>

      {/* ============================================================ */}
      {/* RESULT */}
      {/* ============================================================ */}
      {result && (
        <div className="bg-white rounded-xl border border-green-200 shadow-sm p-6">
          <div className="flex items-center gap-2 text-green-600 mb-4">
            <CheckCircle className="h-5 w-5" />
            <h2 className="text-lg font-semibold text-gray-800">Result</h2>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
            <div className="bg-gray-50 rounded-lg p-4 text-center">
              <div className="flex items-center justify-center gap-2 text-gray-500 text-sm mb-1">
                <Ruler className="h-4 w-4" />
                Distance
              </div>
              <div className="text-2xl font-bold text-gray-800">
                {formatDistance(result.distanceKm)}
              </div>
            </div>

            <div className="bg-gray-50 rounded-lg p-4 text-center">
              <div className="flex items-center justify-center gap-2 text-gray-500 text-sm mb-1">
                <Clock className="h-4 w-4" />
                Estimated Time
              </div>
              <div className="text-2xl font-bold text-gray-800">
                {formatDuration(result.durationMinutes)}
              </div>
            </div>
          </div>

          {/* Route details */}
          <div className="mt-4 text-sm text-gray-500 border-t border-gray-100 pt-4">
            <div className="flex justify-between">
              <span>From: <span className="font-medium text-gray-700">{origin?.capital}</span></span>
              <span>To: <span className="font-medium text-gray-700">{destination?.capital}</span></span>
            </div>
          </div>
        </div>
      )}

      {/* ============================================================ */}
      {/* ERROR */}
      {/* ============================================================ */}
      {error && (
        <div className="bg-white rounded-xl border border-red-200 shadow-sm p-6">
          <div className="flex items-center gap-2 text-red-600">
            <XCircle className="h-5 w-5" />
            <h2 className="text-lg font-semibold text-gray-800">Error</h2>
          </div>
          <p className="text-red-600 mt-2">{error}</p>
        </div>
      )}
    </div>
  );
}