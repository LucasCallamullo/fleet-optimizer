import { useNavigate } from 'react-router-dom';
import { LogOut } from 'lucide-react';
import { Button } from '@shared/components/ui/button';
import { useAuth } from '@features/auth/hooks/useAuth';

interface LogoutButtonProps {
  /**
   * Button variant. Defaults to "ghost" for use in toolbars and navbars.
   */
  variant?: 'ghost' | 'outline' | 'default' | 'destructive';

  /**
   * Show the "Cerrar sesión" label next to the icon.
   * Defaults to false (icon only).
   */
  showLabel?: boolean;

  /**
   * Optional callback fired after logout completes.
   */
  onLogout?: () => void;
}

/**
 * LogoutButton
 *
 * Renders a button that logs the current user out and redirects to /login.
 * Uses the useAuth hook so it can be safely placed anywhere inside AuthProvider.
 *
 * Usage:
 *   <LogoutButton />
 *   <LogoutButton variant="outline" showLabel />
 */
export default function LogoutButton({
  variant = 'ghost',
  showLabel = false,
  onLogout,
}: LogoutButtonProps) {
  const { logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      await logout();
    } finally {
      onLogout?.();
      navigate('/login', { replace: true });
    }
  };

  return (
    <Button
      variant={variant}
      size={showLabel ? 'default' : 'icon'}
      onClick={handleLogout}
      aria-label="Cerrar sesión"
      title="Cerrar sesión"
    >
      <LogOut className="h-5 w-5" />
      {showLabel && <span className="ml-2">Cerrar sesión</span>}
    </Button>
  );
}