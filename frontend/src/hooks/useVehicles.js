// src/hooks/useVehicles.js
import { useState, useEffect, useCallback } from "react";
import vehiclesApi from "@/api/vehiclesApi";

/**
 * ================================================================
 * useVehicles() - CUSTOM HOOK
 * ================================================================
 * 
 * WHAT IS A CUSTOM HOOK?
 * A function that starts with "use" and can use other React hooks.
 * It encapsulates REUSABLE STATE LOGIC.
 * 
 * WHY USE IT?
 * - Separates business logic from UI components
 * - Makes code more testable
 * - Allows reusing logic across multiple components
 * - Follows the "DRY" (Don't Repeat Yourself) principle
 * 
 * @returns {Object} State and functions for managing vehicles
 */
export const useVehicles = () => {
  /**
   * ================================================================
   * STATE DECLARATIONS
   * ================================================================
   * 
   * These are the "source of truth" for all vehicle data in this hook.
   * Each state variable has a specific purpose:
   */
  
  // Main data: Array of vehicles from the API
  const [vehicles, setVehicles] = useState([]);
  
  // Loading state: Shows if data is being fetched, this is for displaying loading messages in the UI
  const [loading, setLoading] = useState(true);
  
  // Error state: Stores error message if something fails, this is the message rendered in the table on error
  const [error, setError] = useState(null);
  
  // Saving state: Shows if a create/update is in progress
  const [isSaving, setIsSaving] = useState(false);
  
  // Edit state: Stores the vehicle being edited (null = creating new)
  const [editingVehicle, setEditingVehicle] = useState(null);

  /**
   * ================================================================
   * fetchVehicles() - FETCH DATA FROM API
   * ================================================================
   * 
   * This function:
   * 1. Sets loading to true (shows "loading" in UI)
   * 2. Clears any previous errors
   * 3. Calls the API to get all vehicles
   * 4. Updates the vehicles state with the data
   * 5. Sets loading to false (hides loading message)
   * 
   * WHY useCallback?
   * - It memoizes the function (prevents recreation on every render)
   * - It's passed as a dependency to useEffect
   * - Without it, useEffect would run infinitely (more on this below)
   * 
   * @param {string} setLoading - this shows loading verification message
   * @param {string} setError - this is rendered in the table on error
   */
  const fetchVehicles = useCallback(async () => {
    try {
      setLoading(true);     // Shows: "Conectando con el servidor..."
      setError(null);       // Clears any previous error
      const data = await vehiclesApi.getAll(); // talks to API and returns list
      setVehicles(data);    // Updates state with the data
    } catch (err) {
      console.error("Error fetching vehicles:", err);
      // this message is rendered in the table's error state
      setError("No se pudo cargar la flota de vehículos.");
    } finally {
      setLoading(false);    // Hides loading message
    }
  }, []); // Empty dependency array = runs once on mount (like componentDidMount)

  /**
   * ================================================================
   * createVehicle() - CREATE NEW VEHICLE
   * ================================================================
   * 
   * Flow:
   * 1. Set isSaving to true (disables form buttons)
   * 2. Call API to create the vehicle
   * 3. Refresh the list (fetchVehicles)
   * 4. Return true if successful
   * 5. Show alert if error
   * 6. Set isSaving to false (re-enables form)
   * 
   * @param {Object} vehicleData - Data from the form
   * @returns {boolean} - true if successful, false if error
   */
  const createVehicle = useCallback(async (vehicleData) => {
    try {
      setIsSaving(true); // Shows "some message on save..." in button
      await vehiclesApi.create(vehicleData); // POST to API
      await fetchVehicles(); // Refresh list (CRITICAL!)
      return true;     // Success
    } catch (err) {
      console.error("Error creating vehicle:", err);
      alert("Hubo un error al intentar guardar el vehículo.");
      return false; // Failure
    } finally {
      setIsSaving(false); // Re-enables button
    }
  }, [fetchVehicles]); // Depends on fetchVehicles (must refresh after create)

  /**
   * ================================================================
   * updateVehicle() - UPDATE EXISTING VEHICLE
   * ================================================================
   * 
   * Similar to createVehicle but with an ID.
   * 
   * @param {number} id - Vehicle ID to update
   * @param {Object} vehicleData - Updated data from form
   * @returns {boolean} - true if successful, false if error
   */
  const updateVehicle = useCallback(async (id, vehicleData) => {
    try {
      setIsSaving(true);
      await vehiclesApi.update(id, vehicleData); // PUT to API
      await fetchVehicles(); // Refresh list
      setEditingVehicle(null); // Clear edit state
      return true;
    } catch (err) {
      console.error("Error updating vehicle:", err);
      alert("Hubo un error al intentar actualizar el vehículo.");
      return false;
    } finally {
      setIsSaving(false);
    }
  }, [fetchVehicles]); // Depends on fetchVehicles

  /**
   * ================================================================
   * deleteVehicle() - DELETE VEHICLE
   * ================================================================
   * 
   * Shows a confirmation dialog before deleting.
   * 
   * @param {number} id - Vehicle ID to delete
   * @returns {boolean} - true if successful, false if cancelled or error
   */
  const deleteVehicle = useCallback(async (id) => {
    // Ask user for confirmation (UI blocking)
    if (!confirm("¿Estás seguro de que querés eliminar este vehículo?")) {
      return false; // User cancelled
    }

    try {
      await vehiclesApi.delete(id); //  DELETE from API
      await fetchVehicles(); //  Refresh list
      return true; // Success
    } catch (err) {
      console.error("Error deleting vehicle:", err);
      alert("Hubo un error al intentar eliminar el vehículo.");
      return false; //  Failure
    }
  }, [fetchVehicles]); //  Depends on fetchVehicles

  // MOMENTO CERO (Component mounts)
  useEffect(() => {
    fetchVehicles(); // Start fetching data
  }, [fetchVehicles]); // Dependency array: run when fetchVehicles changes

  // MOMENTO UNO (After fetch completes)
  // Now vehicles has data, loading is false, error is null

  return {
    vehicles,        // MOMENTO CERO: [] → MOMENTO UNO: [vehicle1, vehicle2, ...]
    loading,         // MOMENTO CERO: true → MOMENTO UNO: false
    error,           // MOMENTO CERO: null → MOMENTO UNO: null (or error message)
    isSaving,        // MOMENTO CERO: false → MOMENTO UNO: false
    editingVehicle,  // MOMENTO CERO: null → MOMENTO UNO: null (or vehicle being edited)
    setEditingVehicle, // Function to update editingVehicle
    createVehicle,   // Function: POST to API
    updateVehicle,   // Function: PUT to API
    deleteVehicle,   // Function: DELETE from API
    fetchVehicles,   // Function: GET from API (can be called manually)
  };
};

/**
 * ================================================================
 * SUMMARY - YOUR QUESTIONS ANSWERED:
 * ================================================================
 * 
 * ❓ "loading es para mostrar un mensaje de verificacion?"
 * ✅ YES! Shows "Conectando con el servidor..." while fetching.
 * 
 * ❓ "setError es el mensaje a renderizar en caso de error?"
 * ✅ YES! Rendered in the table as "⚠️ No se pudo cargar..."
 * 
 * ❓ "vehiclesApi.getAll() se comunica con la API nomas?"
 * ✅ YES! Returns the list of vehicles from the backend.
 * 
 * ❓ "que significa [fetchVehicles] en useEffect?"
 * ✅ It's a DEPENDENCY ARRAY! It means "run when fetchVehicles changes".
 * Since fetchVehicles is stable (useCallback with []), it runs once on mount.
 * 
 * ❓ "no entiendo el porque la sintaxis es asi?"
 * ✅ It's React's way of controlling when effects run.
 * Without dependencies, useEffect runs on EVERY render.
 * With [fetchVehicles], it runs when fetchVehicles changes.
 * Since it never changes, it runs ONCE on mount.
 * 
 * ================================================================
 * PATTERNS USED:
 * ================================================================
 * 
 * 1. ✅ Custom Hook Pattern - Encapsulates logic
 * 2. ✅ useCallback - Memoizes functions to prevent unnecessary re-renders
 * 3. ✅ useEffect - Handles side effects (API calls)
 * 4. ✅ useState - Manages component state
 * 5. ✅ Dependency Array - Controls when effects run
 * 6. ✅ Error Handling - try/catch with user feedback (alert + state)
 * 7. ✅ Loading States - Visual feedback for async operations
 * 
 * ================================================================
 * BEST PRACTICES FOLLOWED:
 * ================================================================
 * 
 * 1. ✅ Custom hooks start with "use" prefix
 * 2. ✅ Each function has a single responsibility
 * 3. ✅ Async operations use try/catch
 * 4. ✅ Loading states for better UX
 * 5. ✅ Error states for user feedback
 * 6. ✅ useCallback to prevent infinite loops
 * 7. ✅ Proper dependency arrays in useEffect
 * 8. ✅ Descriptive variable names
 * ================================================================
 */