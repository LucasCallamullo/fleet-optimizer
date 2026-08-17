// src/features/vehicles/components/VehicleForm.jsx
import { useState, useEffect } from "react";
import { Button } from "@/shared/components/ui/button";
import { Input } from "@/shared/components/ui/input";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/shared/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/shared/components/ui/select";
import { Label } from "@/shared/components/ui/label";
import { Weight, Package, Gauge, DollarSign, Percent, Loader2 } from 'lucide-react';

/**
 * VehicleForm Component
 * 
 * @component
 * @param {Object} props
 * @param {boolean} props.open - Modal visibility state
 * @param {Function} props.onOpenChange - Toggle modal function
 * @param {Object|null} props.initialData - Vehicle data for editing
 * @param {Function} props.onSave - Callback with form data
 * @param {boolean} props.isSaving - Disable form during save
 * @param {Array} props.categories - List of categories (from useCategories)
 * @param {boolean} props.categoriesLoading - Loading state for categories
 */
const VehicleForm = ({
  open,
  onOpenChange,
  initialData = null,
  onSave,
  isSaving = false,
  categories = [],
  categoriesLoading = false,
}) => {
  // ================================================================
  // LOCAL STATE - Form fields
  // ================================================================
  const [licensePlate, setLicensePlate] = useState("");
  const [year, setYear] = useState("");
  const [categoryId, setCategoryId] = useState("");
  const [maxWeightKg, setMaxWeightKg] = useState("");
  const [maxVolumeCbm, setMaxVolumeCbm] = useState("");
  const [fuelConsumptionPerKm, setFuelConsumptionPerKm] = useState("");
  const [costPerKm, setCostPerKm] = useState("");
  const [pricePerKm, setPricePerKm] = useState("");
  const [status, setStatus] = useState("AVAILABLE");

  // ================================================================
  // EFFECT - Reset form when modal opens or data changes
  // ================================================================
  useEffect(() => {
    if (initialData) {
      // EDIT MODE: Fill form with existing data
      setLicensePlate(initialData.licensePlate || "");
      setYear(initialData.year?.toString() || "");
      setCategoryId(initialData.category?.id?.toString() || "");
      setMaxWeightKg(initialData.maxWeightKg?.toString() || "");
      setMaxVolumeCbm(initialData.maxVolumeCbm?.toString() || "");
      setFuelConsumptionPerKm(initialData.fuelConsumptionPerKm?.toString() || "");
      setCostPerKm(initialData.costPerKm?.toString() || "");
      setPricePerKm(initialData.pricePerKm?.toString() || "");
      setStatus(initialData.status || "AVAILABLE");
    } else {
      // CREATE MODE: Clear form
      setLicensePlate("");
      setYear("");
      setCategoryId("");
      setMaxWeightKg("");
      setMaxVolumeCbm("");
      setFuelConsumptionPerKm("");
      setCostPerKm("");
      setPricePerKm("");
      setStatus("AVAILABLE");
    }
  }, [initialData, open]);

  // ================================================================
  // HANDLERS
  // ================================================================
  const handleSubmit = (e) => {
    e.preventDefault();
    
    const vehicleData = {
      licensePlate,
      year: parseInt(year) || 2026,
      category: categoryId ? { id: parseInt(categoryId) } : null,
      maxWeightKg: maxWeightKg ? parseFloat(maxWeightKg) : null,
      maxVolumeCbm: maxVolumeCbm ? parseFloat(maxVolumeCbm) : null,
      fuelConsumptionPerKm: fuelConsumptionPerKm ? parseFloat(fuelConsumptionPerKm) : null,
      costPerKm: costPerKm ? parseFloat(costPerKm) : null,
      pricePerKm: pricePerKm ? parseFloat(pricePerKm) : null,
      status,
    };

    onSave(vehicleData);
  };

  const isEditing = !!initialData;

  // Get category name by ID for display
  const getCategoryName = (id) => {
    const cat = categories.find(c => c.id === parseInt(id));
    return cat?.name || 'Unknown';
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[600px] bg-white max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="text-gray-800">
            {isEditing ? "Edit Vehicle" : "Register New Vehicle"}
          </DialogTitle>
          <DialogDescription className="text-gray-500">
            {isEditing
              ? "Update the vehicle details."
              : "Enter the vehicle information to add it to the fleet."}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-5 py-4">
          {/* ============================================================ */}
          {/* BASIC INFORMATION */}
          {/* ============================================================ */}
          <div className="space-y-3">
            <h4 className="text-sm font-semibold text-gray-700 border-b border-gray-200 pb-2">
              Basic Information
            </h4>
            
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {/* License Plate */}
              <div className="space-y-1.5">
                <Label htmlFor="licensePlate" className="text-sm text-gray-600">
                  License Plate *
                </Label>
                <Input
                  id="licensePlate"
                  placeholder="ABC123"
                  value={licensePlate}
                  onChange={(e) => setLicensePlate(e.target.value)}
                  disabled={isSaving}
                  required
                  className="border-gray-200 focus:border-blue-400"
                />
              </div>

              {/* Year */}
              <div className="space-y-1.5">
                <Label htmlFor="year" className="text-sm text-gray-600">
                  Year *
                </Label>
                <Input
                  id="year"
                  type="number"
                  placeholder="2023"
                  value={year}
                  onChange={(e) => setYear(e.target.value)}
                  disabled={isSaving}
                  required
                  className="border-gray-200 focus:border-blue-400"
                />
              </div>
            </div>

            {/* Category */}
            <div className="space-y-1.5 w-100">
              <Label htmlFor="category" className="text-sm text-gray-600">
                Category *
              </Label>
              <Select
                value={categoryId}
                onValueChange={setCategoryId}
                disabled={isSaving || categoriesLoading}
              >
                <SelectTrigger className="border-gray-200 focus:border-blue-400">
                  <SelectValue placeholder="Select a category" />
                </SelectTrigger>
                <SelectContent>
                  {categoriesLoading ? (
                    <div className="flex items-center justify-center py-4 text-gray-500">
                      <Loader2 className="h-4 w-4 animate-spin mr-2" />
                      Loading categories...
                    </div>
                  ) : categories.length > 0 ? (
                    categories.map((cat) => (
                      <SelectItem key={cat.id} value={cat.id.toString()}>
                        {cat.name}
                      </SelectItem>
                    ))
                  ) : (
                    <div className="text-center py-4 text-gray-400">
                      No categories available
                    </div>
                  )}
                </SelectContent>
              </Select>
            </div>
          </div>

          {/* ============================================================ */}
          {/* CAPACITY & PERFORMANCE */}
          {/* ============================================================ */}
          <div className="space-y-3">
            <h4 className="text-sm font-semibold text-gray-700 border-b border-gray-200 pb-2">
              Capacity & Performance
            </h4>
            
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <Label htmlFor="maxWeight" className="text-sm text-gray-600 flex items-center gap-1.5">
                  <Weight className="h-3.5 w-3.5 text-gray-400" />
                  Max Weight (kg)
                </Label>
                <Input
                  id="maxWeight"
                  type="number"
                  step="0.1"
                  placeholder="1000.0"
                  value={maxWeightKg}
                  onChange={(e) => setMaxWeightKg(e.target.value)}
                  disabled={isSaving}
                  className="border-gray-200 focus:border-blue-400"
                />
              </div>

              <div className="space-y-1.5">
                <Label htmlFor="maxVolume" className="text-sm text-gray-600 flex items-center gap-1.5">
                  <Package className="h-3.5 w-3.5 text-gray-400" />
                  Max Volume (m³)
                </Label>
                <Input
                  id="maxVolume"
                  type="number"
                  step="0.1"
                  placeholder="15.0"
                  value={maxVolumeCbm}
                  onChange={(e) => setMaxVolumeCbm(e.target.value)}
                  disabled={isSaving}
                  className="border-gray-200 focus:border-blue-400"
                />
              </div>
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="fuelConsumption" className="text-sm text-gray-600 flex items-center gap-1.5">
                <Gauge className="h-3.5 w-3.5 text-gray-400" />
                Fuel Consumption (L/km)
              </Label>
              <Input
                id="fuelConsumption"
                type="number"
                step="0.01"
                placeholder="0.08"
                value={fuelConsumptionPerKm}
                onChange={(e) => setFuelConsumptionPerKm(e.target.value)}
                disabled={isSaving}
                className="border-gray-200 focus:border-blue-400"
              />
            </div>
          </div>

          {/* ============================================================ */}
          {/* FINANCIAL INFORMATION */}
          {/* ============================================================ */}
          <div className="space-y-3">
            <h4 className="text-sm font-semibold text-gray-700 border-b border-gray-200 pb-2">
              Financial Information
            </h4>
            
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <Label htmlFor="costPerKm" className="text-sm text-gray-600 flex items-center gap-1.5">
                  <DollarSign className="h-3.5 w-3.5 text-gray-400" />
                  Cost per km ($)
                </Label>
                <Input
                  id="costPerKm"
                  type="number"
                  step="0.01"
                  placeholder="1.50"
                  value={costPerKm}
                  onChange={(e) => setCostPerKm(e.target.value)}
                  disabled={isSaving}
                  className="border-gray-200 focus:border-blue-400"
                />
              </div>

              <div className="space-y-1.5">
                <Label htmlFor="pricePerKm" className="text-sm text-gray-600 flex items-center gap-1.5">
                  <Percent className="h-3.5 w-3.5 text-gray-400" />
                  Price per km ($)
                </Label>
                <Input
                  id="pricePerKm"
                  type="number"
                  step="0.01"
                  placeholder="2.50"
                  value={pricePerKm}
                  onChange={(e) => setPricePerKm(e.target.value)}
                  disabled={isSaving}
                  className="border-gray-200 focus:border-blue-400"
                />
              </div>
            </div>
          </div>

          {/* ============================================================ */}
          {/* STATUS */}
          {/* ============================================================ */}
          <div className="space-y-3">
            <h4 className="text-sm font-semibold text-gray-700 border-b border-gray-200 pb-2">
              Status
            </h4>
            
            <div className="space-y-1.5">
              <Label htmlFor="status" className="text-sm text-gray-600">
                Vehicle Status
              </Label>
              <Select
                value={status}
                onValueChange={setStatus}
                disabled={isSaving}
              >
                <SelectTrigger className="border-gray-200 focus:border-blue-400">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="AVAILABLE">🟢 Available</SelectItem>
                  <SelectItem value="IN_ROUTE">🔵 In Route</SelectItem>
                  <SelectItem value="MAINTENANCE">🟡 Maintenance</SelectItem>
                  <SelectItem value="INACTIVE">⚪ Inactive</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          {/* ============================================================ */}
          {/* FOOTER */}
          {/* ============================================================ */}
          <DialogFooter className="pt-4 border-t border-gray-200">
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
              disabled={isSaving}
              className="border-gray-200 text-gray-600 hover:bg-gray-50"
            >
              Cancel
            </Button>
            <Button
              type="submit"
              disabled={isSaving}
              className="bg-blue-600 hover:bg-blue-700 text-white"
            >
              {isSaving 
                ? "Saving..." 
                : isEditing 
                  ? "Update Vehicle" 
                  : "Save Vehicle"
              }
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default VehicleForm;