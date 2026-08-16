// src/main.jsx
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from '@/features/auth/context/AuthContext';
import App from '@/App.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    {/*
      ============================================================
      IMPORTANT: React 19+ & React Router v7+ REQUIREMENT
      ============================================================
      
      AuthProvider MUST wrap BrowserRouter, not the other way around.
      
      WHY?
      - AuthProvider uses React Context, which must be available
        at the highest level before routing logic executes.
      - React Router hooks (useNavigate, useLocation, etc.) 
        need BrowserRouter context, but AuthProvider's context
        must be established first.
      - This order ensures:
        1. Auth state is available globally
        2. ProtectedRoute can use useAuth() within routes
        3. No "useAuth must be used within AuthProvider" errors
      
      REACT VERSION: 19.2.6
      REACT ROUTER: 7.17.0
      ============================================================
    */}
    <AuthProvider>
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </AuthProvider>
  </StrictMode>,
)