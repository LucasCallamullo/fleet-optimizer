// src/pages/Vehicle.jsx
import { useState } from "react";
import { Button } from "@/components/ui/button";
import VehiclesTable from "@/components/vehicles/VehiclesTable";
import VehicleForm from "@/components/vehicles/VehicleForm";
import { useVehicles } from "@/hooks/useVehicles";

export default function Vehicle() {

  const [openModal, setOpenModal] = useState(false);
  
  /**
   * ================================================================
   * useVehicles() - CUSTOM HOOK EXPLANATION
   * ================================================================
   * 
   * This is "destructuring" or "unpacking" all the values and functions that the custom hook returns.
   * 
   * WHAT'S HAPPENING HERE:
   * 1. The hook executes its internal logic (useState, useEffect, useCallback)
   * 2. It returns an object with all the state and functions
   * 3. We destructure that object to get individual variables
   */
  const {
    vehicles,        // State: Array of vehicles from the API
    loading,         // State: Boolean indicating if data is being fetched
    error,           // State: Error message if something fails
    isSaving,        // State: Boolean indicating if a save operation is in progress
    editingVehicle,  // State: The vehicle being edited (null if creating new)
    setEditingVehicle, // Function: Update the editingVehicle state
    createVehicle,   // Function: Makes API call to create a new vehicle
    updateVehicle,   // Function: Makes API call to update an existing vehicle
    deleteVehicle,   // Function: Makes API call to delete a vehicle
  } = useVehicles(); //   <-- THIS EXECUTES THE HOOK

  /**
   * ================================================================
   * handleSave() - SAVE HANDLER EXPLANATION
   * ================================================================
   * 
   * This is a "callback function" or "event handler" that:
   * 1. Determines if we're creating or updating based on editingVehicle
   * 2. Calls the appropriate function from the hook
   * 3. If successful, closes the modal and clears the editing state
   * 
   * WHY THIS PATTERN? It's a "Controller" or "Orchestrator" function
   * that coordinates between:
   * - The UI (modal, form)
   * - The hook (business logic)
   * - The API (via the hook)
   */
  const handleSave = async (vehicleData) => {
    let success = false;
    
    if (editingVehicle) {
      // UPDATE MODE: We have an editingVehicle, so we're updating
      success = await updateVehicle(editingVehicle.id, vehicleData);
    } else {
      // CREATE MODE: No editingVehicle, so we're creating new
      success = await createVehicle(vehicleData);
    }

    if (success) {
      setOpenModal(false);        // Close the modal
      setEditingVehicle(null);    // Clear the editing state
    }
  };

  /**
   * ================================================================
   * handleEdit() - EDIT HANDLER
   * ================================================================
   * 
   * Called when user clicks the edit button on a vehicle row.
   * Sets the editingVehicle and opens the modal.
   * 
   * FLOW: Table → onEdit prop → this function → sets state → modal opens
   * ================================================================
   */
  const handleEdit = (vehicle) => {
    setEditingVehicle(vehicle); // Set which vehicle is being edited
    setOpenModal(true);         // Open the modal
  };

  /**
   * ================================================================
   * handleDelete() - DELETE HANDLER
   * ================================================================
   * 
   * Called when user clicks the delete button on a vehicle row.
   * Just passes the ID to the hook's delete function.
   * 
   * WHY ASYNC? The delete operation is asynchronous (it makes an API call)
   * ================================================================
   */
  const handleDelete = async (id) => {
    await deleteVehicle(id); // The hook handles the confirmation dialog
  };

  /**
   * ================================================================
   * handleOpenCreate() - CREATE HANDLER
   * ================================================================
   * 
   * Called when user clicks "Agregar Vehículo" button.
   * Clears any editing state and opens the modal in "create" mode.
   * 
   * WHY setEditingVehicle(null)? This tells the form to show empty fields
   * ================================================================
   */
  const handleOpenCreate = () => {
    setEditingVehicle(null);  // Ensure we're in "create" mode
    setOpenModal(true);       // Open the modal
  };

  /**
   * ================================================================
   * RENDER EXPLANATION
   * ================================================================
   * 
   * This is the "View" part of the MVC pattern.
   * 
   * WHAT'S HAPPENING:
   * 1. We render the header with the "Add" button
   * 2. We render the VehiclesTable and pass it:
   *    - State data (vehicles, loading, error) as props
   *    - Callback functions (onEdit, onDelete) as props
   * 3. We render the VehicleForm and pass it:
   *    - State data (open, initialData, isSaving) as props
   *    - Callback functions (onOpenChange, onSave) as props
   * 
   * PROP DRILLING EXPLANATION:
   * - Parent (this component) owns the state
   * - Children receive state and callbacks via props
   * - When child needs to change something, it calls the callback
   * - The callback updates the state in the parent
   * - React re-renders and passes the new state back down
   * 
   * This is the "Lifting State Up" pattern in React.
   * ================================================================
   */
  return (
    <div className="p-6 max-w-5xl mx-auto space-y-4">
      {/* 
        ============================================================
        HEADER SECTION
        ============================================================
        Title and the "Add Vehicle" button.
        When clicked, it triggers handleOpenCreate()
      */}
      <div className="flex justify-between items-center">
        <h2 className="text-2xl font-bold tracking-tight text-gray-900">
          Gestión de Flota
        </h2>
        
        <Button
          onClick={handleOpenCreate}
          className="bg-blue-600 hover:bg-blue-700 text-white"
        >
          Agregar Vehículo
        </Button>
      </div>

      {/* 
        ============================================================
        VEHICLES TABLE COMPONENT
        ============================================================
        
        The hook fetches data and updates the state.
        The table receives the data as props.
        When data changes (via fetchVehicles), the table re-renders.
        The callback props allow the table to trigger actions in the parent.
      
        HOW IT WORKS:
        1. useVehicles fetches data and sets vehicles state
        2. This component receives vehicles from the hook
        3. Passes vehicles down to VehiclesTable via the "vehicles" prop
        4. VehiclesTable renders the data in a table
        5. When user clicks "Edit", it calls onEdit(vehicle)
        6. onEdit is handleEdit, which updates state in THIS component
        7. State change causes re-render, updating the modal
      */}
      <VehiclesTable
        vehicles={vehicles}     // The data to display
        loading={loading}       // Show loading state
        error={error}           // Show error state
        onEdit={handleEdit}     // Callback for edit action (table → parent)
        onDelete={handleDelete} // Callback for delete action (table → parent)
      />

      {/* 
        ============================================================
        VEHICLE FORM MODAL COMPONENT
        ============================================================
        
        HOW REACT "ACTS ALONE":
        1. openModal is state in this component
        2. When setOpenModal is called, React re-renders
        3. The VehicleForm receives the new open value
        4. The form responds by showing/hiding
        
        THE FLOW:
        User clicks "Guardar" → form calls onSave(vehicleData)
        onSave is handleSave → which calls create/update
        On success → setOpenModal(false) → closes modal
        On success → setEditingVehicle(null) → clears form
        fetchVehicles is called (inside the hook) → refreshes table
        
        PROP DRILLING:
        - open: Controls if modal is visible
        - onOpenChange: Allows form to close itself (via X button or click outside)
        - initialData: Tells form whether to show empty fields or pre-filled
        - onSave: Callback when user submits the form
        - isSaving: Disables buttons and shows loading state
      */}
      <VehicleForm
        open={openModal}                 // Controls modal visibility
        onOpenChange={setOpenModal}      // Allows modal to close itself
        initialData={editingVehicle}     // Data for editing (null for create)
        onSave={handleSave}              // Callback for save action
        isSaving={isSaving}              // Show saving state
      />
    </div>
  );
}
