// src/features/auth/pages/LoginPage.jsx
import { useState } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { useAuth } from '@auth/hooks/useAuth';
import { Mail, Lock, AlertCircle, CheckCircle, Users, Eye, EyeOff } from 'lucide-react';

export default function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();

  const from = location.state?.from?.pathname || '/';
  const initialSuccessMessage = location.state?.message || '';

  const [formData, setFormData] = useState({
    email: '',
    password: ''
  });
  
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState(initialSuccessMessage);
  const [loading, setLoading] = useState(false);
  const [showTestUsers, setShowTestUsers] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  // ================================================================
  // TEST USERS - For demo purposes only
  // ================================================================
  const testUsers = [
    {
      email: 'user_regular@gmail.com',
      password: 'pass1234',
      role: 'Usuario Regular',
      label: 'Regular User'
    },
    {
      email: 'admin_regular@gmail.com',
      password: 'pass1234',
      role: 'Administrador',
      label: 'Admin User'
    }
  ];

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    
    if (error) setError('');
    if (successMessage) setSuccessMessage('');
  };

  // Auto-fill test user credentials
  const fillTestUser = (email, password) => {
    setFormData({ email, password });
    setShowTestUsers(false);
    // Clear any previous errors
    if (error) setError('');
  };

  const handleSubmit = async (e) => {
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
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-900 via-gray-800 to-gray-900 p-4">
      <div className="w-full max-w-md bg-gray-800/50 backdrop-blur-sm rounded-xl shadow-2xl border border-gray-700 overflow-hidden">
        {/* Header */}
        <div className="p-6 pb-2 text-center">
          <h2 className="text-2xl font-bold text-white">Welcome Back</h2>
          <p className="text-sm text-gray-400 mt-1">Sign in to access your account</p>
        </div>

        {/* Body */}
        <div className="p-6">
          {/* Success Message */}
          {successMessage && (
            <div className="flex items-center gap-2 p-3 mb-4 bg-green-900/30 border border-green-600 rounded-lg text-green-200">
              <CheckCircle className="h-4 w-4 flex-shrink-0" />
              <span className="text-sm">{successMessage}</span>
            </div>
          )}

          {/* Error Message */}
          {error && (
            <div className="flex items-center gap-2 p-3 mb-4 bg-red-900/30 border border-red-600 rounded-lg text-red-200">
              <AlertCircle className="h-4 w-4 flex-shrink-0" />
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
              className="flex items-center gap-2 text-sm text-gray-400 hover:text-yellow-400 transition-colors"
            >
              <Users className="h-4 w-4" />
              {showTestUsers ? 'Hide test users' : 'Show test users'}
            </button>

            {showTestUsers && (
              <div className="mt-2 space-y-2 bg-gray-900/50 rounded-lg p-3 border border-gray-700">
                <p className="text-xs text-gray-500 mb-2">Click a user to auto-fill credentials:</p>
                {testUsers.map((user, index) => (
                  <button
                    key={index}
                    type="button"
                    onClick={() => fillTestUser(user.email, user.password)}
                    className="w-full text-left p-2 rounded-lg bg-gray-800/50 hover:bg-gray-700/50 transition-colors border border-gray-700"
                  >
                    <div className="flex justify-between items-center">
                      <div>
                        <span className="text-sm font-medium text-white">{user.label}</span>
                        <span className="text-xs text-gray-400 ml-2">({user.role})</span>
                      </div>
                      <span className="text-xs text-gray-500">Click to fill</span>
                    </div>
                    <div className="text-xs text-gray-500 mt-1">
                      {user.email} / {user.password}
                    </div>
                  </button>
                ))}
                <div className="text-xs text-gray-600 mt-2 border-t border-gray-700 pt-2">
                  ⚡ These are demo accounts for testing purposes
                </div>
              </div>
            )}
          </div>

          {/* Form */}
          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Email Field */}
            <div className="space-y-2">
              <label htmlFor="email" className="text-sm font-medium text-gray-300">
                Email
              </label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
                <input
                  id="email"
                  name="email"
                  type="email"
                  placeholder="you@example.com"
                  value={formData.email}
                  onChange={handleChange}
                  required
                  disabled={loading}
                  className="w-full pl-10 pr-3 py-2 bg-gray-900/50 border border-gray-600 rounded-lg text-white placeholder:text-gray-500 focus:outline-none focus:ring-2 focus:ring-yellow-400 focus:border-transparent disabled:opacity-60 disabled:cursor-not-allowed"
                />
              </div>
            </div>

            {/* Password Field */}
            <div className="space-y-2">
              <label htmlFor="password" className="text-sm font-medium text-gray-300">
                Password
              </label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
                <input
                  id="password"
                  name="password"
                  type={showPassword ? 'text' : 'password'}
                  placeholder="••••••••"
                  value={formData.password}
                  onChange={handleChange}
                  required
                  disabled={loading}
                  className="w-full pl-10 pr-10 py-2 bg-gray-900/50 border border-gray-600 rounded-lg text-white placeholder:text-gray-500 focus:outline-none focus:ring-2 focus:ring-yellow-400 focus:border-transparent disabled:opacity-60 disabled:cursor-not-allowed"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-300 transition-colors"
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
              className="w-full py-2.5 px-4 bg-yellow-400 hover:bg-yellow-500 text-gray-900 font-semibold rounded-lg transition-all duration-200 hover:scale-[1.02] disabled:opacity-60 disabled:cursor-not-allowed disabled:hover:scale-100"
            >
              {loading ? (
                <span className="flex items-center justify-center gap-2">
                  <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
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
        <div className="p-4 text-center border-t border-gray-700">
          <p className="text-sm text-gray-400">
            Don't have an account?{' '}
            <Link 
              to="/register" 
              className="font-semibold text-yellow-400 hover:text-yellow-300 hover:underline transition-colors"
            >
              Register here
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}