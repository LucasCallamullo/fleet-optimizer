// src/features/auth/components/ProtectedRoute.tsx
import { useEffect, type ReactNode } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '@features/auth/hooks/useAuth';

interface ProtectedRouteProps {
  children: ReactNode;
}

/**
 * Route guard for authenticated users
 */
export function ProtectedRoute({ children }: ProtectedRouteProps) {
  const { isAuthenticated, loading } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  useEffect(() => {
    if (!loading && !isAuthenticated) {
      navigate('/login', { state: { from: location }, replace: true });
    }
  }, [loading, isAuthenticated, navigate, location]);

  if (loading) {
    return (
      <div className="flex items-center justify-center h-screen bg-background text-foreground">
        Loading session...
      </div>
    );
  }

  return isAuthenticated ? <>{children}</> : null;
}