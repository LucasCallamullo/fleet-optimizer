import { useState } from "react";
import { Button } from "@/shared/components/ui/button";
import VehiclesTable from "@/features/vehicles/components/VehiclesTable";
import VehicleForm from "@/features/vehicles/components/VehicleForm";
import { useVehicles } from "@/features/vehicles/hooks/useVehicles";
import { useCategories } from "@/features/vehicles/hooks/useCategories";
import type { VehicleDetail, VehicleDTO } from "@/features/vehicles/types/vehiclesTypes";
import { Plus } from "lucide-react";

/**
 * VehiclePage - MAIN VEHICLE MANAGEMENT COMPONENT
 * 
 * Orchestrates the vehicle management UI, acting as the Controller between
 * presentation components, business logic hooks, and API layer.
 */
export default function VehiclePage() {
  // ================================================================
  // 1. STATE - UI Control
  // ================================================================
  const [openModal, setOpenModal] = useState<boolean>(false);

  // ================================================================
  // 2. HOOKS - Data & Business Logic
  // ================================================================
  const {
    vehicles,
    loading,
    error,
    isSaving,
    editingVehicle,
    setEditingVehicle,
    createVehicle,
    updateVehicle,
    deleteVehicle,
  } = useVehicles();

  const {
    categories,
    loading: categoriesLoading,
  } = useCategories();

  // ================================================================
  // 3. HANDLERS - Event Callbacks
  // ================================================================

  /**
   * handleSave - Orchestrates create/update operations
   */
  const handleSave = async (vehicleData: VehicleDTO): Promise<void> => {
    let success = false;

    if (editingVehicle) {
      // Step-by-step explanation: Execute update mutation for existing vehicle record
      success = await updateVehicle(editingVehicle.id, vehicleData);
    } else {
      // Step-by-step explanation: Execute creation mutation for new vehicle entry
      success = await createVehicle(vehicleData);
    }

    if (success) {
      setOpenModal(false);
      setEditingVehicle(null);
    }
  };

  /**
   * handleEdit - Opens modal pre-populated in edit mode
   */
  const handleEdit = (vehicle: VehicleDetail): void => {
    setEditingVehicle(vehicle);
    setOpenModal(true);
  };

  /**
   * handleDelete - Deletes a vehicle by ID
   */
  const handleDelete = async (id: number): Promise<void> => {
    await deleteVehicle(id);
  };

  /**
   * handleOpenCreate - Opens modal reset for creation mode
   */
  const handleOpenCreate = (): void => {
    setEditingVehicle(null);
    setOpenModal(true);
  };

  // ================================================================
  // 4. RENDER
  // ================================================================
  return (
    <div className="max-w-7xl mx-auto px-4 py-8 space-y-6 text-foreground">
      {/* ============================================================ */}
      {/* HEADER - Title and Actions                                  */}
      {/* ============================================================ */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground">
            Fleet Management
          </h1>
          <p className="text-sm text-muted-foreground mt-0.5">
            Manage your vehicles and their capabilities
          </p>
        </div>

        <Button
          onClick={handleOpenCreate}
          className="bg-primary text-primary-foreground hover:bg-primary/90 flex items-center gap-2"
        >
          <Plus className="h-4 w-4" />
          Add Vehicle
        </Button>
      </div>

      {/* ============================================================ */}
      {/* VEHICLES TABLE - Data display                              */}
      {/* ============================================================ */}
      <VehiclesTable
        vehicles={vehicles}
        loading={loading}
        error={error}
        onEdit={handleEdit}
        onDelete={handleDelete}
      />

      {/* ============================================================ */}
      {/* VEHICLE FORM - Create/Edit Modal                             */}
      {/* ============================================================ */}
      <VehicleForm
        open={openModal}
        onOpenChange={setOpenModal}
        initialData={editingVehicle}
        onSave={handleSave}
        isSaving={isSaving}
        categories={categories}
        categoriesLoading={categoriesLoading}
      />
    </div>
  );
}