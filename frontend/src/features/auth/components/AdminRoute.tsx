// src/features/auth/components/AdminRoute.tsx
import { useEffect, type ReactNode } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@features/auth/hooks/useAuth';

interface AdminRouteProps {
  children: ReactNode;
}

/**
 * Route guard for administrator access
 */
export function AdminRoute({ children }: AdminRouteProps) {
  const { isAdmin, isAuthenticated, loading } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!loading) {
      if (!isAuthenticated) {
        navigate('/login', { replace: true });
      } else if (!isAdmin()) {
        navigate('/', { replace: true });
      }
    }
  }, [loading, isAuthenticated, isAdmin, navigate]);

  if (loading) {
    return (
      <div className="flex items-center justify-center h-screen bg-background text-foreground">
        Loading session...
      </div>
    );
  }

  return isAuthenticated && isAdmin() ? <>{children}</> : null;
}