// src/features/home/pages/Home.tsx
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
  Handshake,
  type LucideIcon
} from 'lucide-react';

// ================================================================
// TYPES & INTERFACES
// ================================================================

interface JWTPayload {
  sub?: string;
  email?: string;
  realm_access?: {
    roles?: string[];
  };
  iss?: string;
  iat?: number;
  exp?: number;
  [key: string]: unknown;
}

interface TokenInfo {
  userId: string;
  email: string;
  roles: string[];
  issuer: string;
  issuedAt: string;
  expiresAt: string;
  expiresIn: number;
}

interface NavigationModule {
  name: string;
  icon: LucideIcon;
  path: string;
  description: string;
  color: string;
  bgColor: string;
  textColor: string;
}

// ================================================================
// HELPER FUNCTIONS
// ================================================================

/**
 * Safely decodes a Base64 URL encoded JWT string into a typed payload
 */
const decodeJWT = (token: string): JWTPayload | null => {
  try {
    const base64Url = token.split('.')[1];
    if (!base64Url) return null;
    
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload) as JWTPayload;
  } catch (e) {
    console.error('Failed to decode JWT token:', e);
    return null;
  }
};

// ================================================================
// COMPONENT
// ================================================================

export default function Home() {
  const { user } = useAuth();

  // Retrieve & parse JWT token
  const token = localStorage.getItem('accessToken');
  const decodedToken = token ? decodeJWT(token) : null;

  const tokenInfo: TokenInfo | null = decodedToken
    ? {
        userId: decodedToken.sub || 'N/A',
        email: decodedToken.email || 'N/A',
        roles: decodedToken.realm_access?.roles || [],
        issuer: decodedToken.iss || 'N/A',
        issuedAt: decodedToken.iat ? new Date(decodedToken.iat * 1000).toLocaleString() : 'N/A',
        expiresAt: decodedToken.exp ? new Date(decodedToken.exp * 1000).toLocaleString() : 'N/A',
        expiresIn: decodedToken.exp ? Math.floor((decodedToken.exp * 1000 - Date.now()) / 60000) : 0,
      }
    : null;

  // Available dashboard modules
  const modules: NavigationModule[] = [
    {
      name: 'Packages',
      icon: Package,
      path: '/packages',
      description: 'Manage your packages',
      color: 'from-blue-400 to-blue-500',
      bgColor: 'bg-blue-500/10',
      textColor: 'text-blue-500',
    },
    {
      name: 'Fleets',
      icon: Truck,
      path: '/vehicles',
      description: 'Manage your vehicles',
      color: 'from-emerald-400 to-emerald-500',
      bgColor: 'bg-emerald-500/10',
      textColor: 'text-emerald-500',
    },
    {
      name: 'Routes',
      icon: Route,
      path: '/routes',
      description: 'Plan and track routes',
      color: 'from-purple-400 to-purple-500',
      bgColor: 'bg-purple-500/10',
      textColor: 'text-purple-500',
    },
    {
      name: 'Geocoding',
      icon: MapPin,
      path: '/geocoding',
      description: 'Calculate distances',
      color: 'from-orange-400 to-orange-500',
      bgColor: 'bg-orange-500/10',
      textColor: 'text-orange-500',
    },
  ];

  return (
    <div className="max-w-7xl mx-auto px-4 py-8 text-foreground">
      {/* ============================================================ */}
      {/* HEADER - Welcome + User Info                                 */}
      {/* ============================================================ */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-8">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-foreground flex items-center gap-2">
            Welcome back, {user?.name || user?.email || 'User'}{' '}
            <Handshake className="h-7 w-7 text-primary" />
          </h1>
          <p className="text-muted-foreground mt-1">
            Fleet Optimizer Dashboard — Manage your operations
          </p>
        </div>
        <div className="flex items-center gap-3 bg-card border border-border rounded-lg px-4 py-2 shadow-sm">
          <Shield className="h-5 w-5 text-primary" />
          <span className="text-sm font-medium text-foreground">
            {user?.roles?.includes('admin') ? '🔑 Admin' : '👤 User'}
          </span>
        </div>
      </div>

      {/* ============================================================ */}
      {/* QUICK ACTIONS - Navigation Modules                            */}
      {/* ============================================================ */}
      <h2 className="text-xl font-semibold text-foreground mb-4 flex items-center gap-2">
        <Calendar className="h-5 w-5 text-muted-foreground" />
        Quick Access
      </h2>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {modules.map((module) => {
          const Icon = module.icon;
          return (
            <Link
              key={module.path}
              to={module.path}
              className="group relative overflow-hidden rounded-xl bg-card border border-border 
                p-6 transition-all duration-300 hover:shadow-lg hover:border-primary/40 hover:-translate-y-0.5"
            >
              {/* Icon Subtle Background Hover Gradient */}
              <div
                className={`absolute inset-0 bg-linear-to-br ${module.color} opacity-0 group-hover:opacity-5 transition-opacity duration-300`}
              />

              <div className="relative z-10">
                <div className="flex items-center justify-between mb-3">
                  <div className={`p-2.5 rounded-xl ${module.bgColor}`}>
                    <Icon className={`h-5 w-5 ${module.textColor}`} />
                  </div>
                  <ChevronRight className="h-4 w-4 text-muted-foreground group-hover:text-primary transition-colors" />
                </div>
                <h3 className="text-base font-semibold text-foreground group-hover:text-primary transition-colors">
                  {module.name}
                </h3>
                <p className="text-sm text-muted-foreground mt-1">{module.description}</p>
              </div>
            </Link>
          );
        })}
      </div>

      {/* ============================================================ */}
      {/* USER & SESSION CARDS                                         */}
      {/* ============================================================ */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8 mt-8">
        {/* User Information */}
        <div className="bg-card rounded-xl border border-border shadow-sm p-6">
          <h3 className="text-sm font-semibold text-foreground mb-4 flex items-center gap-2">
            <User className="h-4 w-4 text-muted-foreground" />
            User Information
          </h3>
          <div className="space-y-3">
            <div className="flex justify-between border-b border-border/60 pb-2">
              <span className="text-muted-foreground text-sm">Name</span>
              <span className="text-foreground font-medium">{user?.name || 'N/A'}</span>
            </div>
            <div className="flex justify-between border-b border-border/60 pb-2">
              <span className="text-muted-foreground text-sm">Email</span>
              <span className="text-foreground font-medium flex items-center gap-2">
                <Mail className="h-4 w-4 text-muted-foreground" />
                {user?.email || 'N/A'}
              </span>
            </div>
            <div className="flex justify-between border-b border-border/60 pb-2">
              <span className="text-muted-foreground text-sm">Roles</span>
              <span className="text-foreground font-medium">
                {user?.roles && user.roles.length > 0 ? (
                  <div className="flex flex-wrap gap-1 justify-end">
                    {user.roles.map((role, i) => (
                      <span
                        key={i}
                        className="px-2 py-0.5 bg-primary/10 text-primary rounded-md text-xs font-medium border border-primary/20"
                      >
                        {role}
                      </span>
                    ))}
                  </div>
                ) : (
                  'N/A'
                )}
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground text-sm">User ID</span>
              <span className="text-muted-foreground font-mono text-xs truncate max-w-45">
                {user?.id || 'N/A'}
              </span>
            </div>
          </div>
        </div>

        {/* JWT Session Details */}
        <div className="bg-card rounded-xl border border-border shadow-sm p-6">
          <h3 className="text-sm font-semibold text-foreground mb-4 flex items-center gap-2">
            <Key className="h-4 w-4 text-muted-foreground" />
            JWT Session Info
          </h3>
          {tokenInfo ? (
            <div className="space-y-3">
              <div className="flex justify-between border-b border-border/60 pb-2">
                <span className="text-muted-foreground text-sm">Issuer</span>
                <span className="text-foreground text-sm font-mono truncate max-w-45">
                  {tokenInfo.issuer}
                </span>
              </div>
              <div className="flex justify-between border-b border-border/60 pb-2">
                <span className="text-muted-foreground text-sm">User ID (sub)</span>
                <span className="text-foreground font-mono text-xs truncate max-w-45">
                  {tokenInfo.userId}
                </span>
              </div>
              <div className="flex justify-between border-b border-border/60 pb-2">
                <span className="text-muted-foreground text-sm">Roles</span>
                <span className="text-foreground font-medium">
                  {tokenInfo.roles.length > 0 ? (
                    <div className="flex flex-wrap gap-1 justify-end">
                      {tokenInfo.roles.slice(0, 3).map((role, i) => (
                        <span
                          key={i}
                          className="px-2 py-0.5 bg-purple-500/10 text-purple-600 dark:text-purple-400 rounded-md text-xs font-medium border border-purple-500/20"
                        >
                          {role}
                        </span>
                      ))}
                      {tokenInfo.roles.length > 3 && (
                        <span className="text-xs text-muted-foreground">
                          +{tokenInfo.roles.length - 3}
                        </span>
                      )}
                    </div>
                  ) : (
                    'N/A'
                  )}
                </span>
              </div>
              <div className="flex justify-between border-b border-border/60 pb-2">
                <span className="text-muted-foreground text-sm flex items-center gap-1">
                  <Clock className="h-3 w-3" />
                  Expires In
                </span>
                <span
                  className={`font-medium ${
                    tokenInfo.expiresIn < 5 ? 'text-error' : 'text-success'
                  }`}
                >
                  {tokenInfo.expiresIn > 0 ? `${tokenInfo.expiresIn} minutes` : 'Expired'}
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground text-sm">Issued At</span>
                <span className="text-foreground text-sm">{tokenInfo.issuedAt}</span>
              </div>
            </div>
          ) : (
            <div className="text-center py-6 text-muted-foreground">
              <p>No active session</p>
              <p className="text-xs mt-1">Please log in to see JWT details</p>
            </div>
          )}
        </div>
      </div>

      {/* ============================================================ */}
      {/* RAW JWT TOKEN COLLAPSIBLE                                    */}
      {/* ============================================================ */}
      <details className="mt-8 bg-card rounded-xl border border-border shadow-sm p-4 group">
        <summary className="cursor-pointer text-sm text-muted-foreground hover:text-foreground transition-colors font-medium select-none flex items-center gap-2">
          <span>🔑 Show raw JWT</span>
        </summary>
        <div className="mt-3 bg-muted/40 rounded-lg p-4 border border-border overflow-x-auto">
          <pre className="text-xs text-muted-foreground font-mono break-all whitespace-pre-wrap">
            {token || 'No token found'}
          </pre>
        </div>
      </details>
    </div>
  );
}