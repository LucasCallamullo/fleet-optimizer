import {ApiResponse} from "@shared/types/commonTypes";

export interface Location {
  street: string;
  streetNumber: string;
  city: string;
  state: string;
  country: string;
  postalCode: string;
  latitude: number;
  longitude: number;
}

export type RouteStatus = 'PLANNED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
export type LegStatus = 'PENDING' | 'IN_TRANSIT' | 'CANCELLED';

export interface RouteLeg {
  id: number;
  sequence: number;
  status: LegStatus;
  distanceKm: number;
  durationMinutes: number;
  createdAt: string;
  updatedAt: string;
  startedAt: string | null;
  completedAt: string | null;
  vehicleId: number;
  packageId: number;
  origin: Location;
  destination: Location;
}

export interface RouteDetail {
  id: number;
  name: string;
  description: string;
  status: RouteStatus;
  estimatedDistanceKm: number;
  estimatedDurationMinutes: number;
  createdAt: string;
  updatedAt: string;
  legs: RouteLeg[];
}


export type ApiRouteDetail = ApiResponse<RouteDetail>;
export type ApiRouteList = ApiResponse<RouteDetail[]>; 