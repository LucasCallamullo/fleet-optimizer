// src/features/vehicles/components/VehicleForm.tsx
import { useState, useEffect, type FormEvent } from "react";
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
import type { VehicleDetail, VehicleDTO, VehicleStatus } from "../types/vehiclesTypes";
import type { CategoryResponseDTO } from "../types/categoriesTypes";

// ================================================================
// COMPONENT PROPS INTERFACE
// ================================================================

export interface VehicleFormProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  initialData?: VehicleDetail | null;
  onSave: (data: VehicleDTO) => void;
  isSaving?: boolean;
  categories?: CategoryResponseDTO[];
  categoriesLoading?: boolean;
}

// ================================================================
// COMPONENT
// ================================================================

const VehicleForm = ({
  open,
  onOpenChange,
  initialData = null,
  onSave,
  isSaving = false,
  categories = [],
  categoriesLoading = false,
}: VehicleFormProps) => {
  // ================================================================
  // LOCAL STATE - Form fields
  // ================================================================
  const [licensePlate, setLicensePlate] = useState<string>("");
  const [year, setYear] = useState<string>("");
  const [categoryId, setCategoryId] = useState<string>("");
  const [maxWeightKg, setMaxWeightKg] = useState<string>("");
  const [maxVolumeCbm, setMaxVolumeCbm] = useState<string>("");
  const [fuelConsumptionPerKm, setFuelConsumptionPerKm] = useState<string>("");
  const [costPerKm, setCostPerKm] = useState<string>("");
  const [pricePerKm, setPricePerKm] = useState<string>("");
  const [status, setStatus] = useState<VehicleStatus>("AVAILABLE");

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
  const handleSubmit = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    const vehicleData: VehicleDTO = {
      licensePlate,
      year: parseInt(year, 10) || new Date().getFullYear(),
      categoryId: parseInt(categoryId, 10) || 0,
      maxWeightKg: maxWeightKg ? parseFloat(maxWeightKg) : 0,
      maxVolumeCbm: maxVolumeCbm ? parseFloat(maxVolumeCbm) : 0,
      fuelConsumptionPerKm: fuelConsumptionPerKm ? parseFloat(fuelConsumptionPerKm) : 0,
      costPerKm: costPerKm ? parseFloat(costPerKm) : 0,
      pricePerKm: pricePerKm ? parseFloat(pricePerKm) : 0,
      status,
    };

    onSave(vehicleData);
  };

  const isEditing = !!initialData;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-150 bg-card text-card-foreground border-border max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="text-foreground">
            {isEditing ? "Edit Vehicle" : "Register New Vehicle"}
          </DialogTitle>
          <DialogDescription className="text-muted-foreground">
            {isEditing
              ? "Update the vehicle details."
              : "Enter the vehicle information to add it to the fleet."}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-5 py-4">
          {/* ============================================================ */}
          {/* BASIC INFORMATION                                            */}
          {/* ============================================================ */}
          <div className="space-y-3">
            <h4 className="text-sm font-semibold text-foreground border-b border-border pb-2">
              Basic Information
            </h4>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {/* License Plate */}
              <div className="space-y-1.5">
                <Label htmlFor="licensePlate" className="text-sm text-muted-foreground">
                  License Plate *
                </Label>
                <Input
                  id="licensePlate"
                  placeholder="ABC123"
                  value={licensePlate}
                  onChange={(e) => setLicensePlate(e.target.value)}
                  disabled={isSaving}
                  required
                  className="bg-background border-border focus:border-ring"
                />
              </div>

              {/* Year */}
              <div className="space-y-1.5">
                <Label htmlFor="year" className="text-sm text-muted-foreground">
                  Year *
                </Label>
                <Input
                  id="year"
                  type="number"
                  placeholder="2026"
                  value={year}
                  onChange={(e) => setYear(e.target.value)}
                  disabled={isSaving}
                  required
                  className="bg-background border-border focus:border-ring"
                />
              </div>
            </div>

            {/* Category */}
            <div className="space-y-1.5 w-full">
              <Label htmlFor="category" className="text-sm text-muted-foreground">
                Category *
              </Label>
              <Select
                value={categoryId}
                onValueChange={setCategoryId}
                disabled={isSaving || categoriesLoading}
              >
                <SelectTrigger className="bg-background border-border focus:border-ring">
                  <SelectValue placeholder="Select a category" />
                </SelectTrigger>
                <SelectContent className="bg-popover text-popover-foreground border-border">
                  {categoriesLoading ? (
                    <div className="flex items-center justify-center py-4 text-muted-foreground">
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
                    <div className="text-center py-4 text-muted-foreground">
                      No categories available
                    </div>
                  )}
                </SelectContent>
              </Select>
            </div>
          </div>

          {/* ============================================================ */}
          {/* CAPACITY & PERFORMANCE                                       */}
          {/* ============================================================ */}
          <div className="space-y-3">
            <h4 className="text-sm font-semibold text-foreground border-b border-border pb-2">
              Capacity & Performance
            </h4>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <Label htmlFor="maxWeight" className="text-sm text-muted-foreground flex items-center gap-1.5">
                  <Weight className="h-3.5 w-3.5 text-muted-foreground" />
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
                  className="bg-background border-border focus:border-ring"
                />
              </div>

              <div className="space-y-1.5">
                <Label htmlFor="maxVolume" className="text-sm text-muted-foreground flex items-center gap-1.5">
                  <Package className="h-3.5 w-3.5 text-muted-foreground" />
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
                  className="bg-background border-border focus:border-ring"
                />
              </div>
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="fuelConsumption" className="text-sm text-muted-foreground flex items-center gap-1.5">
                <Gauge className="h-3.5 w-3.5 text-muted-foreground" />
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
                className="bg-background border-border focus:border-ring"
              />
            </div>
          </div>

          {/* ============================================================ */}
          {/* FINANCIAL INFORMATION                                        */}
          {/* ============================================================ */}
          <div className="space-y-3">
            <h4 className="text-sm font-semibold text-foreground border-b border-border pb-2">
              Financial Information
            </h4>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <Label htmlFor="costPerKm" className="text-sm text-muted-foreground flex items-center gap-1.5">
                  <DollarSign className="h-3.5 w-3.5 text-muted-foreground" />
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
                  className="bg-background border-border focus:border-ring"
                />
              </div>

              <div className="space-y-1.5">
                <Label htmlFor="pricePerKm" className="text-sm text-muted-foreground flex items-center gap-1.5">
                  <Percent className="h-3.5 w-3.5 text-muted-foreground" />
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
                  className="bg-background border-border focus:border-ring"
                />
              </div>
            </div>
          </div>

          {/* ============================================================ */}
          {/* STATUS                                                       */}
          {/* ============================================================ */}
          <div className="space-y-3">
            <h4 className="text-sm font-semibold text-foreground border-b border-border pb-2">
              Status
            </h4>

            <div className="space-y-1.5">
              <Label htmlFor="status" className="text-sm text-muted-foreground">
                Vehicle Status
              </Label>
              <Select
                value={status}
                onValueChange={(val) => setStatus(val as VehicleStatus)}
                disabled={isSaving}
              >
                <SelectTrigger className="bg-background border-border focus:border-ring">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent className="bg-popover text-popover-foreground border-border">
                  <SelectItem value="AVAILABLE">🟢 Available</SelectItem>
                  <SelectItem value="IN_USE">🔵 In Use</SelectItem>
                  <SelectItem value="MAINTENANCE">🟡 Maintenance</SelectItem>
                  <SelectItem value="DISCONTINUED">⚪ Discontinued</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          {/* ============================================================ */}
          {/* FOOTER                                                       */}
          {/* ============================================================ */}
          <DialogFooter className="pt-4 border-t border-border">
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
              disabled={isSaving}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              disabled={isSaving}
              className="bg-primary text-primary-foreground hover:bg-primary/90"
            >
              {isSaving ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Saving...
                </>
              ) : isEditing ? (
                "Update Vehicle"
              ) : (
                "Save Vehicle"
              )}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default VehicleForm;