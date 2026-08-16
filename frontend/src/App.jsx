import React, { useEffect } from 'react';
import { Routes, Route, useNavigate, useLocation } from 'react-router-dom';

import { useAuth } from '@/features/auth/hooks/useAuth';

// CURRENT COMPONENTS
import MainLayout from "@/shared/components/base/MainLayout";
import Home from "@/shared/pages/Home";
import Vehicle from "@/features/vehicles/pages/Vehicle";
import Error404 from "@/shared/pages/Error404";
import '@/index.css';

// === NEW AUTHENTICATION COMPONENTS ===
import LoginForm from '@/features/auth/pages/LoginForm';
import Register from '@/features/auth/pages/RegisterPage';

// ================================================================
// 1. PROTECTED ROUTE COMPONENT (Logged-in users only)
// ================================================================
// PURPOSE:
// - Ensures only authenticated users can access certain pages
// - Redirects unauthenticated users to the login page
// - Preserves the intended destination URL for post-login redirect
// 
// REDIRECT FLOW:
// - If not authenticated: navigate('/login', { state: { from: location } })
// - The 'state' object preserves the original URL so we can redirect back after login
// 
function ProtectedRoute({ children }) {
  const { isAuthenticated, loading } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  // ============================================================
  // Effect to handle redirect when auth state changes
  // ============================================================
  // 
  // Dependencies:
  // - loading: Wait for session check to complete
  // - isAuthenticated: React to authentication changes
  // - navigate: React Router navigation function
  // - location: Original URL to preserve for redirect
  // 
  // This effect runs whenever any dependency changes
  // (e.g., after login, logout, or initial session check)
  useEffect(() => {
    // Only check after loading is complete
    if (!loading && !isAuthenticated) {
      // Redirect to login with:
      // - state: { from: location } → Original URL for post-login redirect
      // - replace: true → Replace current history entry (prevents back button issues)
      navigate('/login', { state: { from: location }, replace: true });
    }
  }, [loading, isAuthenticated, navigate, location]);

  // Loading state handler
  if (loading) {
    return <div className="flex items-center justify-center h-screen">Loading session...</div>;
  }

  // - If authenticated: render the children (the actual page)
  // - If not authenticated: return null (useEffect handles the redirect)
  return isAuthenticated ? children : null;
}

// 2. ADMIN ROUTE COMPONENT (Administrators only)
function AdminRoute({ children }) {
  const { isAdmin, isAuthenticated, loading } = useAuth();
  const navigate = useNavigate();

  // ============================================================
  // Effect to handle role-based redirects
  // ============================================================
  // 
  // Priority of checks:
  // 1. Loading state → Wait for session check
  // 2. Not authenticated → Redirect to /login
  // 3. Not admin → Redirect to homepage /
  // 4. Admin → Render children (no redirect)
  useEffect(() => {
    // Only proceed after loading is complete
    if (!loading) {
      // Check 1: Not authenticated → login
      if (!isAuthenticated) {
        navigate('/login', { replace: true });
      }
      // Check 2: Authenticated but not admin → homepage
      else if (!isAdmin()) {
        navigate('/', { replace: true });
      }
      // Check 3: isAdmin() returns true → No redirect, render children
    }
  }, [loading, isAuthenticated, isAdmin, navigate]);

  // Loading state handler
  if (loading) {
    return <div className="flex items-center justify-center h-screen">Loading session...</div>;
  }

  // Returns children only if:
  // - User is authenticated AND User has admin role
  return isAuthenticated && isAdmin() ? children : null;
}

// 3. MAIN APP COMPONENT (Route Configuration)
export default function App() {
  return (
    <Routes>
      {/* ============================================ */}
      {/* PUBLIC ROUTES - No authentication required */}
      {/* ============================================ */}
      
      {/*
        Route: /login
        - Login page for user authentication
        - Access: Public (anyone)
        - Features: Email/password form, redirect after login
        - No Navbar (clean authentication screen)
      */}
      <Route
        path="/login"
        element={
            <LoginForm />
        }
      />

      {/*
        Route: /register
        - Registration page for new users
        - Access: Public (anyone)
        - Features: Email/password/name form
        - No Navbar (clean registration screen)
      */}
      <Route
        path="/register"
        element={
            <Register />
        }
      />

      {/* ============================================ */}
      {/* PROTECTED ROUTES - Authentication required */}
      {/* ALL PROTECTED ROUTES HAVE NAVBAR via MainLayout */}
      {/* ============================================ */}
      
      {/*
        Route: /
        - Homepage / Dashboard
        - Access: Only logged-in users
        - Features: Welcome message, dashboard content
        - Wrapped in: ProtectedRoute + MainLayout
      */}
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <MainLayout>
              <Home nombre="Lucas" edad={25} activo={true} />
            </MainLayout>
          </ProtectedRoute>
        }
      />

      {/*
        Route: /vehicles
        - Vehicle management page
        - Access: Only logged-in users
        - Features: Vehicle list, CRUD operations
        - Wrapped in: ProtectedRoute + MainLayout
      */}
      <Route
        path="/vehicles"
        element={
          <ProtectedRoute>
            <MainLayout>
              <Vehicle />
            </MainLayout>
          </ProtectedRoute>
        }
      />

      {/* ============================================ */}
      {/* 404 - CATCH-ALL ROUTE */}
      {/* ============================================ */}
      
      {/*
        Route: * (wildcard)
        - Page Not Found
        - Access: Public (anyone)
        - Features: Shows 404 error page
        - MUST BE THE LAST ROUTE (matches any unmatched URL)
      */}
      <Route path="*" element={<Error404 />} />
    </Routes>
  );
}