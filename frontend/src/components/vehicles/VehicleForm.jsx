// src/components/vehicles/VehicleForm.jsx
import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";

/**
 * ================================================================
 * PROP DRILLING - Communication with Parent
 * ================================================================
 * 
 * PARENT (Vehicle.jsx)
 *   ↓ open={openModal}              - Controls if modal is visible
 *   ↓ onOpenChange={setOpenModal}   - Allows modal to close itself
 *   ↓ initialData={editingVehicle}  - Data for editing (null for create)
 *   ↓ onSave={handleSave}           - Called when form is submitted
 *   ↓ isSaving={isSaving}           - Disables button during save
 * 
 * THIS COMPONENT
 *   ↑ Calls onSave(vehicleData) when user clicks submit
 *   ↑ Calls onOpenChange(false) when user clicks X or outside
 */

/**
 * VehicleForm Component
 * 
 * @component
 * @param {Object} props
 * @param {boolean} props.open - Modal visibility state from parent
 * @param {Function} props.onOpenChange - Function to toggle modal (from parent)
 * @param {Object|null} props.initialData - Vehicle data for editing (null = create mode)
 * @param {Function} props.onSave - Callback to parent with form data
 * @param {boolean} props.isSaving - Disables form during save operation
 * @returns {JSX.Element} Rendered form inside a dialog/modal
 * 
 * @example
 * // Create mode
 * <VehicleForm
 *   open={true}
 *   onOpenChange={setOpen}
 *   initialData={null}
 *   onSave={handleCreate}
 *   isSaving={false}
 * />
 * 
 * @example
 * // Edit mode
 * <VehicleForm
 *   open={true}
 *   onOpenChange={setOpen}
 *   initialData={vehicleToEdit}
 *   onSave={handleUpdate}
 *   isSaving={false}
 * />
 */
const VehicleForm = ({
  open,
  onOpenChange,
  initialData = null,
  onSave,
  isSaving = false,
}) => {
  // LOCAL STATE - Form fields
  const [patente, setPatente] = useState("");
  const [modelo, setModelo] = useState("");

  /**
   * useEffect - RESET FORM WHEN MODAL OPENS OR DATA CHANGES
   * 
   * ================================================================
   * SCENARIO 1: CREATE MODE (Opening modal for new vehicle)
   * ================================================================
   * 
   * 1. Parent: setOpenModal(true)
   * 2. open prop changes: false → true
   * 3. useEffect runs because [open] changed
   * 4. initialData is null → set fields to empty strings
   * 5. Form shows empty fields
   * 6. User fills form and saves
   * 7. Parent: setOpenModal(false)
   * 8. open prop changes: true → false
   * 9. useEffect runs again → fields stay empty (ready for next open)
   * 
   * ================================================================
   * SCENARIO 2: EDIT MODE (Opening modal to edit existing vehicle)
   * ================================================================
   * 
   * 1. Parent: setEditingVehicle(vehicle) → sets initialData
   * 2. Parent: setOpenModal(true)
   * 3. open prop changes: false → true
   * 4. initialData changes: null → { id: 1, licensePlate: 'ABC123', ... }
   * 5. useEffect runs because BOTH [initialData, open] changed
   * 6. initialData exists → set fields with vehicle data
   * 7. Form shows pre-filled fields
   * 8. User edits and saves
   * 9. Parent: setEditingVehicle(null) → initialData becomes null
   * 10. Parent: setOpenModal(false)
   * 11. open changes: true → false
   * 12. initialData changes: { vehicle } → null
   * 13. useEffect runs → fields reset to empty strings
   */
  useEffect(() => {
    if (initialData) {
      // 📝 EDIT MODE: Fill form with existing data
      setPatente(initialData.licensePlate || "");
      setModelo(initialData.year?.toString() || "");
    } else {
      // ➕ CREATE MODE: Clear form
      setPatente("");
      setModelo("");
    }
  }, [initialData, open]); // 👈 Effect runs when these change

  /**
   * handleSubmit - Form Submission Handler
   * 
   * Called when user submits the form.
   * Prevents default browser behavior and calls parent's onSave.
   */
  const handleSubmit = (e) => {
    e.preventDefault(); 
    
    const vehicleData = {
      licensePlate: patente,
      year: parseInt(modelo) || 2026,
      category: { id: 1 }, // TODO: Make dynamic when categories are implemented
    };

    onSave(vehicleData); // Send data to parent
  };

  const isEditing = !!initialData; // Check if we're editing

  /**
   * ================================================================
   * RENDER - Dialog with Form
   * ================================================================
   * 
   * Dialog (shadcn/ui) - Modal component
   * DialogContent - The modal content container
   * DialogHeader - Title and description
   * DialogFooter - Action buttons at bottom
   * 
   * The form is CONTROLLED:
   * - value={patente} → Controlled by React state
   * - onChange={setPatente} → Updates React state
   * - disabled={isSaving} → Disabled during save
   */
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[425px] bg-white">
        <DialogHeader>
          <DialogTitle>
            {isEditing ? "Editar Vehículo" : "Registrar Nuevo Vehículo"}
          </DialogTitle>
          <DialogDescription>
            {isEditing
              ? "Modificá los datos del vehículo seleccionado."
              : "Ingresá los datos del camión o auto para darlo de alta en el sistema."}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4 py-4">
          {/* 
            License Plate Field
            - Controlled by React state (patente)
            - Updates on every keystroke
            - Required field
            - Disabled during saving
          */}
          <div className="space-y-2">
            <label className="text-sm font-medium text-gray-700">
              Patente / Dominio
            </label>
            <Input
              placeholder="Ej: ABC123"
              value={patente}
              onChange={(e) => setPatente(e.target.value)}
              disabled={isSaving}
              required
            />
          </div>

          {/* 
            Year/Model Field
            - Controlled by React state (modelo)
            - String input (converted to int on submit)
            - Required field
          */}
          <div className="space-y-2">
            <label className="text-sm font-medium text-gray-700">
              Año / Modelo
            </label>
            <Input
              placeholder="Ej: 2023"
              value={modelo}
              onChange={(e) => setModelo(e.target.value)}
              disabled={isSaving}
              required
            />
          </div>

          {/* 
            Submit Button
            - Changes text based on mode (create/update)
            - Shows "Guardando..." when saving
            - Disabled during save to prevent double submission
            - Full width on mobile (w-full), auto on desktop (sm:w-auto)
          */}
          <DialogFooter className="pt-4">
            <Button
              type="submit"
              disabled={isSaving}
              className="bg-blue-600 hover:bg-blue-700 text-white w-full sm:w-auto"
            >
              {isSaving 
                ? "Guardando..." 
                : isEditing 
                  ? "Actualizar Vehículo" 
                  : "Guardar Vehículo"
              }
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default VehicleForm;
