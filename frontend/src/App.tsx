// import React from 'react'
import { Routes, Route } from 'react-router-dom'
import "./index.css"

// SHARED COMPONENTS & PAGES
import MainLayout from '@shared/components/base/MainLayout'
import Home from '@shared/pages/Home'
import Error404 from '@shared/pages/Error404'

// AUTH COMPONENTS & PAGES
// import { AdminRoute } from '@features/auth/components/AdminRoute'
import { ProtectedRoute } from '@features/auth/components/ProtectedRoute'
import LoginPage from '@features/auth/pages/LoginPage' 
import Register from '@features/auth/pages/RegisterPage'

// FEATURE PAGES
import Vehicle from '@/features/vehicles/pages/VehiclePage'
import GeocodingPage from '@features/geocoding/pages/GeocodingPage'
import PackagesPage from '@features/packages/pages/PackagesPage'
import PackageDetailPage from '@features/packages/pages/PackageDetailPage'


// ================================================================
// 3. MAIN APP COMPONENT
// ================================================================
export default function App() {
  return (
    <Routes>
      {/* PUBLIC ROUTES */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<Register />} />

      {/* PROTECTED ROUTES */}
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <MainLayout>
              <Home />
            </MainLayout>
          </ProtectedRoute>
        }
      />

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

      <Route
        path="/geocoding"
        element={
          <ProtectedRoute>
            <MainLayout>
              <GeocodingPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/packages"
        element={
          <ProtectedRoute>
            <MainLayout>
              <PackagesPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/packages/:id"
        element={
          <ProtectedRoute>
            <MainLayout>
              <PackageDetailPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />

      {/* CATCH-ALL ROUTE */}
      <Route path="*" element={<Error404 />} />
    </Routes>
  )
}