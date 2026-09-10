import { useState, type SyntheticEvent, type ChangeEvent } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { useAuth } from '@features/auth/hooks/useAuth';
import { Mail, Lock, AlertCircle, CheckCircle, Users, Eye, EyeOff } from 'lucide-react';

interface LocationState {
  from?: {
    pathname: string;
  };
  message?: string;
}

interface TestUser {
  email: string;
  password: 'pass1234';
  role: string;
  label: string;
}

export default function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();

  // Extract location state safely
  const state = location.state as LocationState | undefined;
  const from = state?.from?.pathname || '/';
  const initialSuccessMessage = state?.message || '';

  const [formData, setFormData] = useState({
    email: '',
    password: '',
  });

  const [error, setError] = useState<string>('');
  const [successMessage, setSuccessMessage] = useState<string>(initialSuccessMessage);
  const [loading, setLoading] = useState<boolean>(false);
  const [showTestUsers, setShowTestUsers] = useState<boolean>(false);
  const [showPassword, setShowPassword] = useState<boolean>(false);

  // ================================================================
  // TEST USERS - For demo purposes only
  // ================================================================
  const testUsers: TestUser[] = [
    {
      email: 'user_regular@gmail.com',
      password: 'pass1234',
      role: 'Usuario Regular',
      label: 'Regular User',
    },
    {
      email: 'admin_regular@gmail.com',
      password: 'pass1234',
      role: 'Administrador',
      label: 'Admin User',
    },
  ];

  /**
   * Handles input changes and clears previous error/success banners
   */
  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));

    if (error) setError('');
    if (successMessage) setSuccessMessage('');
  };

  /**
   * Auto-fills credentials for test user accounts
   */
  const fillTestUser = (email: string, password: 'pass1234') => {
    setFormData({ email, password });
    setShowTestUsers(false);
    if (error) setError('');
  };

  /**
   * Handles form submission using SyntheticEvent in React 19
   */
  const handleSubmit = async (e: SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const result = await login(formData.email, formData.password);

      if (result.success) {
        navigate(from, { replace: true });
      } else {
        setError(result.message || 'Invalid email or password');
      }
    } catch (err) {
      setError('Error connecting to the server');
      console.error('Login error:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-background p-4 text-foreground">
      <div className="w-full max-w-md bg-card border border-border rounded-xl shadow-2xl overflow-hidden">
        {/* Header */}
        <div className="p-6 pb-2 text-center">
          <h2 className="text-2xl font-bold text-foreground">Welcome Back</h2>
          <p className="text-sm text-muted-foreground mt-1">Sign in to access your account</p>
        </div>

        {/* Body */}
        <div className="p-6">
          {/* Success Message Banner */}
          {successMessage && (
            <div className="flex items-center gap-2 p-3 mb-4 bg-success/10 border border-success/30 rounded-lg text-success">
              <CheckCircle className="h-4 w-4 shrink-0" />
              <span className="text-sm">{successMessage}</span>
            </div>
          )}

          {/* Error Message Banner */}
          {error && (
            <div className="flex items-center gap-2 p-3 mb-4 bg-error/10 border border-error/30 rounded-lg text-error">
              <AlertCircle className="h-4 w-4 shrink-0" />
              <span className="text-sm">{error}</span>
            </div>
          )}

          {/* ============================================================ */}
          {/* TEST USERS SECTION - Demo credentials */}
          {/* ============================================================ */}
          <div className="mb-4">
            <button
              type="button"
              onClick={() => setShowTestUsers(!showTestUsers)}
              className="flex items-center gap-2 text-sm text-muted-foreground hover:text-primary transition-colors"
            >
              <Users className="h-4 w-4" />
              {showTestUsers ? 'Hide test users' : 'Show test users'}
            </button>

            {showTestUsers && (
              <div className="mt-2 space-y-2 bg-muted/50 rounded-lg p-3 border border-border">
                <p className="text-xs text-muted-foreground mb-2">
                  Click a user to auto-fill credentials:
                </p>
                {testUsers.map((user, index) => (
                  <button
                    key={index}
                    type="button"
                    onClick={() => fillTestUser(user.email, user.password)}
                    className="w-full text-left p-2 rounded-lg bg-card hover:bg-accent transition-colors border border-border"
                  >
                    <div className="flex justify-between items-center">
                      <div>
                        <span className="text-sm font-medium text-foreground">{user.label}</span>
                        <span className="text-xs text-muted-foreground ml-2">({user.role})</span>
                      </div>
                      <span className="text-xs text-muted-foreground">Click to fill</span>
                    </div>
                    <div className="text-xs text-muted-foreground mt-1">
                      {user.email} / {user.password}
                    </div>
                  </button>
                ))}
                <div className="text-xs text-muted-foreground mt-2 border-t border-border pt-2">
                  ⚡ These are demo accounts for testing purposes
                </div>
              </div>
            )}
          </div>

          {/* Form */}
          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Email Field */}
            <div className="space-y-2">
              <label htmlFor="email" className="text-sm font-medium text-foreground">
                Email
              </label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <input
                  id="email"
                  name="email"
                  type="email"
                  placeholder="you@example.com"
                  value={formData.email}
                  onChange={handleChange}
                  required
                  disabled={loading}
                  className="w-full pl-10 pr-3 py-2 bg-background border border-input rounded-lg text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent disabled:opacity-60 disabled:cursor-not-allowed"
                />
              </div>
            </div>

            {/* Password Field */}
            <div className="space-y-2">
              <label htmlFor="password" className="text-sm font-medium text-foreground">
                Password
              </label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <input
                  id="password"
                  name="password"
                  type={showPassword ? 'text' : 'password'}
                  placeholder="••••••••"
                  value={formData.password}
                  onChange={handleChange}
                  required
                  disabled={loading}
                  className="w-full pl-10 pr-10 py-2 bg-background border border-input rounded-lg text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent disabled:opacity-60 disabled:cursor-not-allowed"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
                >
                  {showPassword ? (
                    <EyeOff className="h-4 w-4" />
                  ) : (
                    <Eye className="h-4 w-4" />
                  )}
                </button>
              </div>
            </div>

            {/* Submit Button */}
            <button
              type="submit"
              disabled={loading}
              className="w-full py-2.5 px-4 bg-primary text-primary-foreground font-semibold rounded-lg transition-all duration-200 hover:opacity-90 disabled:opacity-60 disabled:cursor-not-allowed"
            >
              {loading ? (
                <span className="flex items-center justify-center gap-2">
                  <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24">
                    <circle
                      className="opacity-25"
                      cx="12"
                      cy="12"
                      r="10"
                      stroke="currentColor"
                      strokeWidth="4"
                      fill="none"
                    />
                    <path
                      className="opacity-75"
                      fill="currentColor"
                      d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                    />
                  </svg>
                  Signing in...
                </span>
              ) : (
                'Sign In'
              )}
            </button>
          </form>
        </div>

        {/* Footer */}
        <div className="p-4 text-center border-t border-border bg-card">
          <p className="text-sm text-muted-foreground">
            Don't have an account?{' '}
            <Link
              to="/register"
              className="font-semibold text-primary hover:underline transition-colors"
            >
              Register here
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}