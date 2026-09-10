import type { ApiResponse } from '@shared/types/commonTypes';

// ================================================================
// 1. CORE DOMAIN MODEL
// ================================================================

export interface User {
  id: string | number;
  email: string;
  name?: string;
  roles: string[];
  [key: string]: unknown; // Supports additional properties from keycloak/backend
}

// ================================================================
// 2. REQUEST PAYLOADS (DTOs)
// ================================================================

export interface LoginPayload {
  email: string;
  password: string;
}

export interface RegisterPayload {
  email: string;
  password: string;
  name?: string;
  [key: string]: unknown;
}

// ================================================================
// 3. RESPONSE DATA PAYLOADS (Inner Data)
// ================================================================

export interface AuthData {
  accessToken: string;
  refreshToken?: string;
  user: User;
}

export interface RefreshTokenData {
  accessToken: string;
  refreshToken?: string;
}

// ================================================================
// 4. API WRAPPED RESPONSES (Backend Contracts)
// ================================================================

export type LoginApiResponse = ApiResponse<AuthData>;
export type RefreshTokenApiResponse = ApiResponse<RefreshTokenData>;
export type RegisterApiResponse = ApiResponse<User>;

// ================================================================
// 5. INTERNAL SERVICE/HOOK RESPONSES (UI State)
// ================================================================

export interface AuthOperationResult {
  success: boolean;
  user?: User;
  message?: string;
}