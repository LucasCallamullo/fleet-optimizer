// src/features/vehicles/components/VehiclesTable.jsx
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

/**
 * VehiclesTable - COMPONENT DEFINITION
 * 
 * Displays a list of vehicles with their details and actions.
 * 
 * @component
 * @param {Object} props - Component props
 * @param {Array} props.vehicles - List of vehicles to display
 * @param {boolean} props.loading - Loading state indicator
 * @param {string|null} props.error - Error message if something fails
 * @param {Function} props.onEdit - Callback when edit button is clicked
 * @param {Function} props.onDelete - Callback when delete button is clicked
 * @returns {JSX.Element} Rendered table with vehicle data
 */
const VehiclesTable = ({ 
  vehicles, 
  loading, 
  error, 
  onEdit, 
  onDelete 
}) => {
  
  // Helper: Get icon based on category
  const getCategoryIcon = (categoryName) => {
    if (!categoryName) return <Truck className="h-4 w-4" />;
    const name = categoryName.toLowerCase();
    if (name.includes('car')) return <Car className="h-4 w-4" />;
    if (name.includes('motorcycle') || name.includes('moto')) return <Bike className="h-4 w-4" />;
    return <Truck className="h-4 w-4" />;
  };

  // Helper: Get status badge color
  const getStatusBadge = (status) => {
    const statusMap = {
      'AVAILABLE': { color: 'bg-emerald-100 text-emerald-700', label: 'Available' },
      'IN_ROUTE': { color: 'bg-blue-100 text-blue-700', label: 'In Route' },
      'MAINTENANCE': { color: 'bg-amber-100 text-amber-700', label: 'Maintenance' },
      'INACTIVE': { color: 'bg-gray-100 text-gray-600', label: 'Inactive' },
    };
    const defaultStatus = { color: 'bg-gray-100 text-gray-600', label: status || 'Unknown' };
    return statusMap[status] || defaultStatus;
  };

  // Helper: Format capacity value
  const formatCapacity = (value) => {
    if (!value) return '—';
    return value;
  };

  /**
   * STATE 1: LOADING
   */
  if (loading) {
    return (
      <Card className="border-gray-200 shadow-sm">
        <CardHeader>
          <CardTitle className="text-gray-800">Fleet Overview</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-gray-500 text-center py-8 animate-pulse">
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
      <Card className="border-gray-200 shadow-sm">
        <CardHeader>
          <CardTitle className="text-gray-800">Fleet Overview</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-red-600 text-center py-8 font-medium">
            ⚠️ {error}
          </p>
        </CardContent>
      </Card>
    );
  }

  /**
   * STATE 3: SUCCESS - RENDER THE TABLE
   */
  return (
    <Card className="border-gray-200 shadow-sm">
      <CardHeader className="flex flex-row items-center justify-between">
        <CardTitle className="text-gray-800">Fleet Overview</CardTitle>
        <span className="text-sm text-gray-500">
          {vehicles.length} {vehicles.length === 1 ? 'vehicle' : 'vehicles'}
        </span>
      </CardHeader>
      <CardContent>
        <div className="overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow className="bg-gray-50">
                <TableHead className="w-[80px] font-semibold text-gray-600">ID</TableHead>
                <TableHead className="font-semibold text-gray-600">Plate</TableHead>
                <TableHead className="font-semibold text-gray-600">Category</TableHead>
                <TableHead className="font-semibold text-gray-600">Year</TableHead>
                <TableHead className="font-semibold text-gray-600">Max Weight</TableHead>
                <TableHead className="font-semibold text-gray-600">Max Volume</TableHead>
                <TableHead className="font-semibold text-gray-600">Consumption</TableHead>
                <TableHead className="font-semibold text-gray-600">Status</TableHead>
                <TableHead className="text-right font-semibold text-gray-600">Actions</TableHead>
              </TableRow>
            </TableHeader>

            <TableBody>
              {vehicles.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={9} className="text-center text-gray-400 py-8">
                    No vehicles registered in the system.
                  </TableCell>
                </TableRow>
              ) : (
                vehicles.map((vehicle) => {
                  const status = getStatusBadge(vehicle.status);
                  const CategoryIcon = getCategoryIcon(vehicle.category?.name);
                  
                  return (
                    <TableRow key={vehicle.id} className="hover:bg-gray-50 transition-colors">
                      {/* ID */}
                      <TableCell className="font-mono text-xs text-gray-500">
                        #{vehicle.id}
                      </TableCell>
                      
                      {/* License Plate */}
                      <TableCell className="font-mono font-bold tracking-wider text-gray-800">
                        {vehicle.licensePlate}
                      </TableCell>
                      
                      {/* Category */}
                      <TableCell>
                        <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-700">
                          {CategoryIcon}
                          {vehicle.category?.name || 'Uncategorized'}
                        </span>
                      </TableCell>
                      
                      {/* Year */}
                      <TableCell className="text-gray-700">
                        {vehicle.year || '—'}
                      </TableCell>
                      
                      {/* Max Weight */}
                      <TableCell className="text-gray-700">
                        {vehicle.maxWeightKg ? (
                          <span className="inline-flex items-center gap-1">
                            <Weight className="h-3 w-3 text-gray-400" />
                            {formatCapacity(vehicle.maxWeightKg)} kg
                          </span>
                        ) : (
                          <span className="text-gray-400">—</span>
                        )}
                      </TableCell>
                      
                      {/* Max Volume */}
                      <TableCell className="text-gray-700">
                        {vehicle.maxVolumeCbm ? (
                          <span className="inline-flex items-center gap-1">
                            <Package className="h-3 w-3 text-gray-400" />
                            {formatCapacity(vehicle.maxVolumeCbm)} m³
                          </span>
                        ) : (
                          <span className="text-gray-400">—</span>
                        )}
                      </TableCell>
                      
                      {/* Fuel Consumption */}
                      <TableCell className="text-gray-700">
                        {vehicle.fuelConsumptionPerKm ? (
                          <span className="inline-flex items-center gap-1">
                            <Gauge className="h-3 w-3 text-gray-400" />
                            {formatCapacity(vehicle.fuelConsumptionPerKm)} L/km
                          </span>
                        ) : (
                          <span className="text-gray-400">—</span>
                        )}
                      </TableCell>
                      
                      {/* Status Badge */}
                      <TableCell>
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${status.color}`}>
                          <span className="w-1.5 h-1.5 rounded-full mr-1.5 bg-current opacity-60" />
                          {status.label}
                        </span>
                      </TableCell>
                      
                      {/* Actions */}
                      <TableCell className="text-right">
                        <div className="flex justify-end gap-1.5">
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => onEdit(vehicle)}
                            className="h-8 px-2.5 border-gray-200 text-gray-600 hover:text-blue-600 hover:border-blue-300"
                          >
                            <Pencil className="h-3.5 w-3.5" />
                          </Button>
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => onDelete(vehicle.id)}
                            className="h-8 px-2.5 border-gray-200 text-gray-600 hover:text-red-600 hover:border-red-300"
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