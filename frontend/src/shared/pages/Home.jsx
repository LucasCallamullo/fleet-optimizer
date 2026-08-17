// src/features/home/pages/Home.jsx
import { Link } from 'react-router-dom';
import { useAuth } from '@/features/auth/hooks/useAuth';
import { 
  Package, 
  Truck, 
  Route, 
  MapPin, 
  User, 
  Mail, 
  Shield,
  Calendar,
  ChevronRight,
  Clock,
  Key,
  BadgeCheck,
  Handshake 
} from 'lucide-react';

export default function Home() {
  const { user } = useAuth();

  // Decodificar JWT (si existe)
  const decodeJWT = (token) => {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      return JSON.parse(jsonPayload);
    } catch (e) {
      return null;
    }
  };

  // Obtener token y decoded data
  const token = localStorage.getItem('accessToken');
  const decodedToken = token ? decodeJWT(token) : null;

  // Extraer información del token
  const tokenInfo = decodedToken ? {
    userId: decodedToken.sub || 'N/A',
    email: decodedToken.email || 'N/A',
    roles: decodedToken.realm_access?.roles || [],
    issuer: decodedToken.iss || 'N/A',
    issuedAt: decodedToken.iat ? new Date(decodedToken.iat * 1000).toLocaleString() : 'N/A',
    expiresAt: decodedToken.exp ? new Date(decodedToken.exp * 1000).toLocaleString() : 'N/A',
    expiresIn: decodedToken.exp ? Math.floor((decodedToken.exp * 1000 - Date.now()) / 60000) : 0,
  } : null;

  // Módulos disponibles
  const modules = [
    {
      name: 'Packages',
      icon: Package,
      path: '/packages',
      color: 'from-blue-400 to-blue-500',
      bgColor: 'bg-blue-50',
      textColor: 'text-blue-600',
      description: 'Manage your packages',
    },
    {
      name: 'Fleets',
      icon: Truck,
      path: '/vehicles',
      color: 'from-emerald-400 to-emerald-500',
      bgColor: 'bg-emerald-50',
      textColor: 'text-emerald-600',
      description: 'Manage your vehicles',
    },
    {
      name: 'Routes',
      icon: Route,
      path: '/routes',
      color: 'from-purple-400 to-purple-500',
      bgColor: 'bg-purple-50',
      textColor: 'text-purple-600',
      description: 'Plan and track routes',
    },
    {
      name: 'Geocoding',
      icon: MapPin,
      path: '/geocoding',
      color: 'from-orange-400 to-orange-500',
      bgColor: 'bg-orange-50',
      textColor: 'text-orange-600',
      description: 'Calculate distances',
    },
  ];

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      {/* ============================================================ */}
      {/* HEADER - Welcome + User Info */}
      {/* ============================================================ */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-8">
        <div>
          <h1 className="text-3xl font-bold text-gray-900 flex items-center gap-1">
            Welcome back, {user?.name || user?.email || 'User'} <Handshake />
          </h1>
          <p className="text-gray-500 mt-1">
            Fleet Optimizer Dashboard — Manage your operations
          </p>
        </div>
        <div className="flex items-center gap-3 bg-gray-100 rounded-lg px-4 py-2 border border-gray-200">
          <Shield className="h-5 w-5 text-blue-600" />
          <span className="text-sm font-medium text-gray-700">
            {user?.roles?.includes('admin') ? '🔑 Admin' : '👤 User'}
          </span>
        </div>
      </div>

      {/* ============================================================ */}
      {/* QUICK ACTIONS - Módulos */}
      {/* ============================================================ */}
      <h2 className="text-xl font-semibold text-gray-800 mb-4 flex items-center gap-2">
        <Calendar className="h-5 w-5 text-gray-500" />
        Quick Access
      </h2>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {modules.map((module) => {
          const Icon = module.icon;
          return (
            <Link
              key={module.path}
              to={module.path}
              className="group relative overflow-hidden rounded-xl bg-white border border-gray-200 
                p-6 transition-all duration-300 hover:shadow-lg hover:border-gray-300 hover:-translate-y-0.5"
            >
              {/* Icon Background */}
              <div className={`absolute inset-0 bg-gradient-to-br ${module.color} opacity-0 group-hover:opacity-5 transition-opacity duration-300`} />
              
              <div className="relative z-10">
                <div className="flex items-center justify-between mb-3">
                  <div className={`p-2.5 rounded-xl ${module.bgColor}`}>
                    <Icon className={`h-5 w-5 ${module.textColor}`} />
                  </div>
                  <ChevronRight className="h-4 w-4 text-gray-300 group-hover:text-gray-600 transition-colors" />
                </div>
                <h3 className="text-base font-semibold text-gray-800 group-hover:text-blue-600 transition-colors">
                  {module.name}
                </h3>
                <p className="text-sm text-gray-500 mt-1">{module.description}</p>
              </div>
            </Link>
          );
        })}
      </div>

      {/* ============================================================ */}
      {/* USER INFO CARD - Datos del usuario + JWT Decoded */}
      {/* ============================================================ */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8 mt-8">
        {/* User Info */}
        <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6">
          <h3 className="text-sm font-semibold text-gray-700 mb-4 flex items-center gap-2">
            <User className="h-4 w-4 text-gray-500" />
            User Information
          </h3>
          <div className="space-y-3">
            <div className="flex justify-between border-b border-gray-100 pb-2">
              <span className="text-gray-500 text-sm">Name</span>
              <span className="text-gray-900 font-medium">{user?.name || 'N/A'}</span>
            </div>
            <div className="flex justify-between border-b border-gray-100 pb-2">
              <span className="text-gray-500 text-sm">Email</span>
              <span className="text-gray-900 font-medium flex items-center gap-2">
                <Mail className="h-4 w-4 text-gray-400" />
                {user?.email || 'N/A'}
              </span>
            </div>
            <div className="flex justify-between border-b border-gray-100 pb-2">
              <span className="text-gray-500 text-sm">Roles</span>
              <span className="text-gray-900 font-medium">
                {user?.roles?.length > 0 ? (
                  <div className="flex flex-wrap gap-1 justify-end">
                    {user.roles.map((role, i) => (
                      <span key={i} className="px-2 py-0.5 bg-blue-50 text-blue-700 rounded-md text-xs font-medium">
                        {role}
                      </span>
                    ))}
                  </div>
                ) : 'N/A'}
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-500 text-sm">User ID</span>
              <span className="text-gray-700 font-mono text-xs truncate max-w-[180px]">
                {user?.id || 'N/A'}
              </span>
            </div>
          </div>
        </div>

        {/* JWT Decoded Info */}
        <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6">
          <h3 className="text-sm font-semibold text-gray-700 mb-4 flex items-center gap-2">
            <Key className="h-4 w-4 text-gray-500" />
            JWT Session Info
          </h3>
          {tokenInfo ? (
            <div className="space-y-3">
              <div className="flex justify-between border-b border-gray-100 pb-2">
                <span className="text-gray-500 text-sm">Issuer</span>
                <span className="text-gray-700 text-sm font-mono truncate max-w-[180px]">
                  {tokenInfo.issuer}
                </span>
              </div>
              <div className="flex justify-between border-b border-gray-100 pb-2">
                <span className="text-gray-500 text-sm">User ID (sub)</span>
                <span className="text-gray-700 font-mono text-xs truncate max-w-[180px]">
                  {tokenInfo.userId}
                </span>
              </div>
              <div className="flex justify-between border-b border-gray-100 pb-2">
                <span className="text-gray-500 text-sm">Roles</span>
                <span className="text-gray-700 font-medium">
                  {tokenInfo.roles.length > 0 ? (
                    <div className="flex flex-wrap gap-1 justify-end">
                      {tokenInfo.roles.slice(0, 3).map((role, i) => (
                        <span key={i} className="px-2 py-0.5 bg-purple-50 text-purple-700 rounded-md text-xs font-medium">
                          {role}
                        </span>
                      ))}
                      {tokenInfo.roles.length > 3 && (
                        <span className="text-xs text-gray-400">+{tokenInfo.roles.length - 3}</span>
                      )}
                    </div>
                  ) : 'N/A'}
                </span>
              </div>
              <div className="flex justify-between border-b border-gray-100 pb-2">
                <span className="text-gray-500 text-sm flex items-center gap-1">
                  <Clock className="h-3 w-3" />
                  Expires In
                </span>
                <span className={`font-medium ${tokenInfo.expiresIn < 5 ? 'text-red-600' : 'text-emerald-600'}`}>
                  {tokenInfo.expiresIn > 0 ? `${tokenInfo.expiresIn} minutes` : 'Expired'}
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500 text-sm">Issued At</span>
                <span className="text-gray-700 text-sm">{tokenInfo.issuedAt}</span>
              </div>
            </div>
          ) : (
            <div className="text-center py-4 text-gray-400">
              <p>No active session</p>
              <p className="text-sm">Please log in to see JWT details</p>
            </div>
          )}
        </div>
      </div>

      {/* ============================================================ */}
      {/* JWT RAW (opcional - expandible) */}
      {/* ============================================================ */}
      <details className="mt-8 bg-white rounded-xl border border-gray-200 shadow-sm p-4">
        <summary className="cursor-pointer text-sm text-gray-500 hover:text-gray-700 transition-colors font-medium">
          🔑 Show raw JWT
        </summary>
        <div className="mt-3 bg-gray-50 rounded-lg p-4 border border-gray-200 overflow-x-auto">
          <pre className="text-xs text-gray-600 font-mono break-all whitespace-pre-wrap">
            {token || 'No token found'}
          </pre>
        </div>
      </details>
    </div>
  );
}