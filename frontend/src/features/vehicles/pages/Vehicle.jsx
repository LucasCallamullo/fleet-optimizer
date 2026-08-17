// src/features/vehicles/pages/VehiclePage.jsx
import { useState } from "react";
import { Button } from "@/shared/components/ui/button";
import VehiclesTable from "@/features/vehicles/components/VehiclesTable";
import VehicleForm from "@/features/vehicles/components/VehicleForm";
import { useVehicles } from "@/features/vehicles/hooks/useVehicles";
import { useCategories } from "@/features/vehicles/hooks/useCategories";
import { Plus } from "lucide-react";

/**
 * VehiclePage - MAIN VEHICLE MANAGEMENT COMPONENT
 * 
 * This component orchestrates the vehicle management UI.
 * It acts as the "Controller" between:
 * - The UI (header, table, modal)
 * - The business logic (useVehicles hook)
 * - The data layer (categories via useCategories hook)
 * 
 * @component
 * @returns {JSX.Element} Rendered vehicle management page
 * 
 * ================================================================
 * COMPONENT HIERARCHY
 * ================================================================
 * 
 * VehiclePage (this component)
 *   ├── Header (title + "Add Vehicle" button)
 *   ├── VehiclesTable (displays vehicle list)
 *   │   └── VehicleRow (each vehicle with actions)
 *   └── VehicleForm (modal for create/edit)
 *       ├── Basic Information (plate, year, category)
 *       ├── Capacity & Performance (weight, volume, fuel)
 *       ├── Financial Information (cost, price)
 *       └── Status (AVAILABLE, IN_ROUTE, MAINTENANCE, INACTIVE)
 * 
 * ================================================================
 * STATE MANAGEMENT
 * ================================================================
 * 
 * UI State (this component):
 * - openModal: Controls form visibility
 * 
 * Data State (from hooks):
 * - vehicles: List of vehicles from API
 * - categories: List of categories from API
 * - loading: Data fetching status
 * - isSaving: Form submission status
 * - editingVehicle: Vehicle being edited (null = create mode)
 * 
 * ================================================================
 */
export default function VehiclePage() {
  // ================================================================
  // 1. STATE - UI Control
  // ================================================================
  const [openModal, setOpenModal] = useState(false);

  // ================================================================
  // 2. HOOKS - Data & Business Logic
  // ================================================================
  
  /**
   * useVehicles - Manages vehicle CRUD operations
   * 
   * Returns:
   * - vehicles: Array of vehicle objects
   * - loading: Boolean for data fetching
   * - error: Error message or null
   * - isSaving: Boolean for form submission
   * - editingVehicle: Vehicle being edited
   * - setEditingVehicle: Update editing state
   * - createVehicle: Create new vehicle
   * - updateVehicle: Update existing vehicle
   * - deleteVehicle: Delete vehicle
   */
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

  /**
   * useCategories - Manages categories data
   * 
   * Returns:
   * - categories: Array of category objects
   * - loading: Boolean for data fetching
   * - error: Error message or null
   */
  const {
    categories,
    loading: categoriesLoading,
    error: categoriesError,
  } = useCategories();

  // ================================================================
  // 3. HANDLERS - Event Callbacks
  // ================================================================

  /**
   * handleSave - Orchestrates create/update operations
   * 
   * Determines if we're creating or updating based on editingVehicle,
   * calls the appropriate hook function, and handles UI state.
   * 
   * @param {Object} vehicleData - Form data from VehicleForm
   * 
   * FLOW:
   * 1. Check if editingVehicle exists
   * 2. If yes → call updateVehicle(editingVehicle.id, data)
   * 3. If no → call createVehicle(data)
   * 4. On success → close modal, clear editing state
   * 5. On error → error handled by hook
   */
  const handleSave = async (vehicleData) => {
    let success = false;
    
    if (editingVehicle) {
      // UPDATE: We have an existing vehicle
      success = await updateVehicle(editingVehicle.id, vehicleData);
    } else {
      // CREATE: New vehicle
      success = await createVehicle(vehicleData);
    }

    if (success) {
      setOpenModal(false);        // Close the modal
      setEditingVehicle(null);    // Clear editing state
    }
  };

  /**
   * handleEdit - Opens modal in edit mode
   * 
   * Called when user clicks the edit button on a vehicle row.
   * 
   * @param {Object} vehicle - The vehicle to edit
   * 
   * FLOW:
   * 1. Set editingVehicle to the selected vehicle
   * 2. Open modal
   * 3. VehicleForm receives initialData and shows pre-filled fields
   */
  const handleEdit = (vehicle) => {
    setEditingVehicle(vehicle);
    setOpenModal(true);
  };

  /**
   * handleDelete - Deletes a vehicle
   * 
   * Called when user clicks the delete button on a vehicle row.
   * The hook handles the confirmation dialog.
   * 
   * @param {number|string} id - ID of the vehicle to delete
   */
  const handleDelete = async (id) => {
    await deleteVehicle(id);
  };

  /**
   * handleOpenCreate - Opens modal in create mode
   * 
   * Called when user clicks "Add Vehicle" button.
   * Clears editing state so form shows empty fields.
   */
  const handleOpenCreate = () => {
    setEditingVehicle(null);  // Ensure "create" mode
    setOpenModal(true);
  };

  // ================================================================
  // 4. RENDER
  // ================================================================

  return (
    <div className="max-w-7xl mx-auto px-4 py-8 space-y-6">
      {/* ============================================================ */}
      {/* HEADER - Title and Actions */}
      {/* ============================================================ */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">
            Fleet Management
          </h1>
          <p className="text-sm text-gray-500 mt-0.5">
            Manage your vehicles and their capabilities
          </p>
        </div>
        
        <Button
          onClick={handleOpenCreate}
          className="bg-blue-600 hover:bg-blue-700 text-white flex items-center gap-2"
        >
          <Plus className="h-4 w-4" />
          Add Vehicle
        </Button>
      </div>

      {/* ============================================================ */}
      {/* VEHICLES TABLE - Data display */}
      {/* ============================================================ */}
      <VehiclesTable
        vehicles={vehicles}
        loading={loading}
        error={error}
        onEdit={handleEdit}
        onDelete={handleDelete}
      />

      {/* ============================================================ */}
      {/* VEHICLE FORM - Create/Edit Modal */}
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