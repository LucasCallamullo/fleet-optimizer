import type { ReactNode } from 'react';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/shared/components/ui/table";
import { Card, CardHeader, CardTitle, CardContent } from "@/shared/components/ui/card";
import { Button } from "@/shared/components/ui/button";
import { Pencil, Trash2, Truck, Car, Bike, Package, Weight, Gauge } from 'lucide-react';
import type { VehicleDetail, VehicleStatus } from "../types/vehiclesTypes";

// ================================================================
// COMPONENT PROPS INTERFACE
// ================================================================

export interface VehiclesTableProps {
  vehicles: VehicleDetail[];
  loading: boolean;
  error: string | null;
  onEdit: (vehicle: VehicleDetail) => void;
  onDelete: (id: number) => void;
}

interface StatusBadgeConfig {
  color: string;
  label: string;
}

// ================================================================
// COMPONENT
// ================================================================

const VehiclesTable = ({
  vehicles,
  loading,
  error,
  onEdit,
  onDelete,
}: VehiclesTableProps) => {

  // Helper: Get icon based on category name
  const getCategoryIcon = (categoryName?: string | null): ReactNode => {
    if (!categoryName) return <Truck className="h-4 w-4 text-muted-foreground" />;
    const name = categoryName.toLowerCase();
    if (name.includes('car')) return <Car className="h-4 w-4 text-muted-foreground" />;
    if (name.includes('motorcycle') || name.includes('moto')) return <Bike className="h-4 w-4 text-muted-foreground" />;
    return <Truck className="h-4 w-4 text-muted-foreground" />;
  };

  // Helper: Get status badge styling matching OKLCH theme variables
  const getStatusBadge = (status?: VehicleStatus): StatusBadgeConfig => {
    const statusMap: Record<VehicleStatus, StatusBadgeConfig> = {
      'AVAILABLE': { color: 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-500/20', label: 'Available' },
      'IN_TRANSIT': { color: 'bg-blue-500/10 text-blue-600 dark:text-blue-400 border-blue-500/20', label: 'In Use' },
      'MAINTENANCE': { color: 'bg-amber-500/10 text-amber-600 dark:text-amber-400 border-amber-500/20', label: 'Maintenance' },
      'OUT_OF_SERVICE': { color: 'bg-muted text-muted-foreground border-border', label: 'Discontinued' },
      'RESERVED': { color: 'bg-muted text-muted-foreground border-border', label: 'Reserved' },
    };

    if (status && status in statusMap) {
      return statusMap[status];
    }

    return { color: 'bg-muted text-muted-foreground border-border', label: status || 'Unknown' };
  };

  // Helper: Format capacity value
  const formatCapacity = (value?: number | null): string => {
    if (value === undefined || value === null) return '—';
    return value.toString();
  };

  /**
   * STATE 1: LOADING
   */
  if (loading) {
    return (
      <Card className="border-border bg-card shadow-sm">
        <CardHeader>
          <CardTitle className="text-foreground">Fleet Overview</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-muted-foreground text-center py-8 animate-pulse">
            Loading vehicles...
          </p>
        </CardContent>
      </Card>
    );
  }

  /**
   * STATE 2: ERROR
   */
  if (error) {
    return (
      <Card className="border-border bg-card shadow-sm">
        <CardHeader>
          <CardTitle className="text-foreground">Fleet Overview</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-error text-center py-8 font-medium">
            ⚠️ {error}
          </p>
        </CardContent>
      </Card>
    );
  }

  /**
   * STATE 3: SUCCESS - RENDER TABLE
   */
  return (
    <Card className="border-border bg-card shadow-sm">
      <CardHeader className="flex flex-row items-center justify-between">
        <CardTitle className="text-foreground">Fleet Overview</CardTitle>
        <span className="text-sm text-muted-foreground">
          {vehicles.length} {vehicles.length === 1 ? 'vehicle' : 'vehicles'}
        </span>
      </CardHeader>
      <CardContent>
        <div className="overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow className="bg-muted/50 border-border">
                <TableHead className="w-20 font-semibold text-muted-foreground">ID</TableHead>
                <TableHead className="font-semibold text-muted-foreground">Plate</TableHead>
                <TableHead className="font-semibold text-muted-foreground">Category</TableHead>
                <TableHead className="font-semibold text-muted-foreground">Year</TableHead>
                <TableHead className="font-semibold text-muted-foreground">Max Weight</TableHead>
                <TableHead className="font-semibold text-muted-foreground">Max Volume</TableHead>
                <TableHead className="font-semibold text-muted-foreground">Consumption</TableHead>
                <TableHead className="font-semibold text-muted-foreground">Status</TableHead>
                <TableHead className="text-right font-semibold text-muted-foreground">Actions</TableHead>
              </TableRow>
            </TableHeader>

            <TableBody>
              {vehicles.length === 0 ? (
                <TableRow className="border-border">
                  <TableCell colSpan={9} className="text-center text-muted-foreground py-8">
                    No vehicles registered in the system.
                  </TableCell>
                </TableRow>
              ) : (
                vehicles.map((vehicle) => {
                  const statusConfig = getStatusBadge(vehicle.status);
                  const CategoryIcon = getCategoryIcon(vehicle.category?.name);

                  return (
                    <TableRow key={vehicle.id} className="hover:bg-muted/30 border-border transition-colors">
                      {/* ID */}
                      <TableCell className="font-mono text-xs text-muted-foreground">
                        #{vehicle.id}
                      </TableCell>

                      {/* License Plate */}
                      <TableCell className="font-mono font-bold tracking-wider text-foreground">
                        {vehicle.licensePlate}
                      </TableCell>

                      {/* Category */}
                      <TableCell>
                        <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium bg-muted text-foreground border border-border">
                          {CategoryIcon}
                          {vehicle.category?.name || 'Uncategorized'}
                        </span>
                      </TableCell>

                      {/* Year */}
                      <TableCell className="text-foreground">
                        {vehicle.year || '—'}
                      </TableCell>

                      {/* Max Weight */}
                      <TableCell className="text-foreground">
                        {vehicle.maxWeightKg ? (
                          <span className="inline-flex items-center gap-1">
                            <Weight className="h-3 w-3 text-muted-foreground" />
                            {formatCapacity(vehicle.maxWeightKg)} kg
                          </span>
                        ) : (
                          <span className="text-muted-foreground">—</span>
                        )}
                      </TableCell>

                      {/* Max Volume */}
                      <TableCell className="text-foreground">
                        {vehicle.maxVolumeCbm ? (
                          <span className="inline-flex items-center gap-1">
                            <Package className="h-3 w-3 text-muted-foreground" />
                            {formatCapacity(vehicle.maxVolumeCbm)} m³
                          </span>
                        ) : (
                          <span className="text-muted-foreground">—</span>
                        )}
                      </TableCell>

                      {/* Fuel Consumption */}
                      <TableCell className="text-foreground">
                        {vehicle.fuelConsumptionPerKm ? (
                          <span className="inline-flex items-center gap-1">
                            <Gauge className="h-3 w-3 text-muted-foreground" />
                            {formatCapacity(vehicle.fuelConsumptionPerKm)} L/km
                          </span>
                        ) : (
                          <span className="text-muted-foreground">—</span>
                        )}
                      </TableCell>

                      {/* Status Badge */}
                      <TableCell>
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${statusConfig.color}`}>
                          <span className="w-1.5 h-1.5 rounded-full mr-1.5 bg-current opacity-75" />
                          {statusConfig.label}
                        </span>
                      </TableCell>

                      {/* Actions */}
                      <TableCell className="text-right">
                        <div className="flex justify-end gap-1.5">
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => onEdit(vehicle)}
                            className="h-8 px-2.5 border-border text-foreground hover:bg-muted hover:text-primary"
                          >
                            <Pencil className="h-3.5 w-3.5" />
                          </Button>
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => onDelete(vehicle.id)}
                            className="h-8 px-2.5 border-border text-foreground hover:bg-destructive/10 hover:text-destructive hover:border-destructive/30"
                          >
                            <Trash2 className="h-3.5 w-3.5" />
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  );
                })
              )}
            </TableBody>
          </Table>
        </div>
      </CardContent>
    </Card>
  );
};

export default VehiclesTable;