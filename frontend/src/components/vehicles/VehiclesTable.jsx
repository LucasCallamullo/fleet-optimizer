// src/components/vehicles/VehiclesTable.jsx
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Pencil, Trash2 } from "lucide-react";

/**
 * VEHICLES TABLE - PRESENTATIONAL COMPONENT
 * 
 * ================================================================
 * PROP DRILLING EXPLANATION
 * ================================================================
 * 
 * This component receives ALL its data and functions via props:
 * 
 * ┌─────────────────────────────────────────────────────────────┐
 * │  PARENT (Vehicle.jsx)                                       │
 * │  - Owns the data (vehicles, loading, error)                 │
 * │  - Owns the functions (handleEdit, handleDelete)            │
 * │  - Passes them DOWN to children via props                   │
 * └─────────────────────────────────────────────────────────────┘
 *                            ↓ props
 * ┌─────────────────────────────────────────────────────────────┐
 * │  THIS COMPONENT (VehiclesTable.jsx)                         │
 * │  - Receives data as props                                   │
 * │  - Renders UI based on props                                │
 * │  - Calls callbacks when user interacts                      │
 * └─────────────────────────────────────────────────────────────┘
 *                            ↑ callback
 * ┌─────────────────────────────────────────────────────────────┐
 * │  EVENT: User clicks "Edit" button                           │
 * │  → Calls onEdit(vehicle) (passed from parent)               │
 * │  → Parent updates its state                                 │
 * │  → Parent re-renders with new state                         │
 * └─────────────────────────────────────────────────────────────┘
 */

/**
 * VehiclesTable - COMPONENT DEFINITION
 * 
 * THE THREE STATES:
 * 1. LOADING → Show loading message
 * 2. ERROR → Show error message
 * 3. SUCCESS → Show the table with data 
 * 
 * @component
 * @param {Object} props - Component props
 * @param {Array} props.vehicles - List of vehicles to display
 * @param {boolean} props.loading - Loading state indicator
 * @param {string|null} props.error - Error message if something fails
 * @param {Function} props.onEdit - Callback when edit button is clicked
 * @param {Function} props.onDelete - Callback when delete button is clicked
 * @returns {JSX.Element} Rendered table with vehicle data
 * 
 * @example
 * <VehiclesTable
 *   vehicles={vehicleList}
 *   loading={false}
 *   error={null}
 *   onEdit={(vehicle) => console.log('Edit:', vehicle)}
 *   onDelete={(id) => console.log('Delete:', id)}
 * />
 */
const VehiclesTable = ({ 
  vehicles, 
  loading, 
  error, 
  onEdit, 
  onDelete 
}) => {
   
  /**
   * STATE 1: LOADING
   * 
   * Shows while data is being fetched.
   * The parent (useVehicles) sets loading = true
   * and passes it down via props.
   * 
   * VISUAL: Animated pulse effect (animate-pulse)
   * MESSAGE: "Conectando con el servidor..."
   */
  if (loading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Flota Activa</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-gray-500 text-center py-4 animate-pulse">
            Conectando con el servidor y recuperando flota...
          </p>
        </CardContent>
      </Card>
    );
  }

  /**
   * STATE 2: ERROR
   * 
   * Shows if there was an error fetching data.
   * The parent (useVehicles) sets error = "message"
   * and passes it down via props.
   */
  if (error) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Flota Activa</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-red-500 text-center py-4 font-medium">
           {error}
          </p>
        </CardContent>
      </Card>
    );
  }

  /**
   * STATE 3: SUCCESS - RENDER THE TABLE
   * 
   * Data is available! Render the table.
   * 
   * TWO SUBCASES:
   * A. No vehicles → Show empty message
   * B. Has vehicles → Show the table rows
   */
  return (
    <Card>
      <CardHeader>
        <CardTitle>Flota Activa</CardTitle>
      </CardHeader>
      <CardContent>
        <Table>
          {/* 
            ==========================================================
            TABLE HEADER - Column definitions
            ==========================================================
            w-[100px] = fixed width for ID column
            className="text-right" = right-align the actions column
          */}
          <TableHeader>
            <TableRow>
              <TableHead className="w-[100px] align-center">ID</TableHead>
              <TableHead className="align-center">Patente</TableHead>
              <TableHead>Año</TableHead>
              <TableHead>Categoría</TableHead>
              <TableHead className="text-right">Acciones</TableHead>
            </TableRow>
          </TableHeader>

          <TableBody>
            {/* 
              ==========================================================
              SUBCASE A: EMPTY STATE
              ==========================================================
              Show when vehicles array is empty.
              colSpan={5} = spans all 5 columns
              className="text-center text-gray-400" = centered, gray text
            */}
            {vehicles.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} className="text-center text-gray-400 py-4">
                  No hay vehículos registrados en el sistema.
                </TableCell>
              </TableRow>
            ) : (
              /**
               * ==========================================================
               * SUBCASE B: RENDER VEHICLE ROWS
               * ==========================================================
               * 
               * WHY key={vehicle.id}?
               * - React needs a unique key for each element in a list
               * - Helps React identify which items changed, added, or removed
               * - Optimizes re-rendering performance
               * 
               * SAFE TO USE vehicle.id? YES! IDs are unique and stable.
               * 
               * WHAT IS OPTIONAL CHAINING (?.)
               * vehicle.category?.name
               * - If category exists, get its name
               * - If category is null/undefined, return undefined
               * - Prevents "Cannot read property 'name' of undefined"
               * 
               * FALLBACK VALUE (||)
               * vehicle.category?.name || "Sin Categoría"
               * - If category.name is undefined, use "Sin Categoría"
               * - Provides a default value
               */
              vehicles.map((vehicle) => (
                <TableRow key={vehicle.id}>
                  {/* ID Column - Gray, medium font weight */}
                  <TableCell className="font-medium text-gray-500">
                    #{vehicle.id}
                  </TableCell>
                  
                  {/* License Plate - Monospace, bold, tracking-wider for readability */}
                  <TableCell className="font-mono font-bold tracking-wider">
                    {vehicle.licensePlate}
                  </TableCell>
                  
                  {/* Year - Simple display */}
                  <TableCell>{vehicle.year}</TableCell>
                  
                  {/* Category - Badge style */}
                  <TableCell>
                    <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-slate-100 text-slate-800">
                      {vehicle.category?.name || "Sin Categoría"}
                    </span>
                  </TableCell>
                  
                  {/* 
                    ==========================================================
                    ACTIONS COLUMN - Edit and Delete buttons
                    ==========================================================
                    
                    CRITICAL: These buttons use CALLBACK PROPS
                    
                    onClick={() => onEdit(vehicle)}
                    - When clicked, calls the onEdit function (passed from parent)
                    - Passes the entire vehicle object to the parent
                    - Parent decides what to do (open modal, set editing state)
                    
                    onClick={() => onDelete(vehicle.id)}
                    - When clicked, calls the onDelete function (passed from parent)
                    - Passes only the vehicle ID to the parent
                    - Parent decides what to do (confirm, delete, refresh)
                    
                    WHY ARROW FUNCTION? () => onEdit(vehicle)
                    - If we did: onClick={onEdit(vehicle)} → would execute IMMEDIATELY
                    - Arrow function delays execution until the button is clicked
                    - "Call me when the button is clicked, not now!"
                  */}
                  <TableCell className="text-right">
                    <div className="flex justify-end gap-2">
                      {/* 
                        ==========================================================
                        EDIT BUTTON
                        ==========================================================
                        variant="outline" → Bordered, no background
                        size="sm" → Small button
                        onClick={() => onEdit(vehicle)} → Passes vehicle to parent
                      */}
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => onEdit(vehicle)}
                        className="h-8 px-2"
                      >
                        <Pencil className="h-4 w-4" />
                      </Button>
                      
                      {/* 
                        ==========================================================
                        DELETE BUTTON
                        ==========================================================
                        variant="destructive" → Red button (danger)
                        size="sm" → Small button
                        onClick={() => onDelete(vehicle.id)} → Passes ID to parent
                      */}
                      <Button
                        variant="destructive"
                        size="sm"
                        onClick={() => onDelete(vehicle.id)}
                        className="h-8 px-2"
                      >
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </CardContent>
    </Card>
  );
};

export default VehiclesTable;

/**
 * ================================================================
 * SUMMARY - WHAT THIS COMPONENT DOES
 * ================================================================
 * 
 * ✅ Renders a table with vehicle data
 * ✅ Shows loading state (while fetching)
 * ✅ Shows error state (if something fails)
 * ✅ Shows empty state (when no vehicles)
 * ✅ Calls onEdit when edit button is clicked
 * ✅ Calls onDelete when delete button is clicked
 * 
 * ================================================================
 * WHAT THIS COMPONENT DOESN'T DO
 * ================================================================
 * 
 * ❌ No API calls (fetching is done by parent via useVehicles)
 * ❌ No state management (all data comes from props)
 * ❌ No business logic (just renders UI and calls callbacks)
 * ❌ No side effects (no useEffect, no API calls)
 * 
 * ================================================================
 * "simplemente es un componente de ui, que recibe props y callbacks"
 * 
 * ✅ YES! EXACTLY!
 * 
 * "en algun punto si llama a acciones son acciones necesarias 
 *  por callback que tendría que pasar"
 * 
 * ✅ YES! The parent MUST pass onEdit and onDelete callbacks.
 * 
 * This is a "Pure Presentational Component" - it just renders
 * and reports user interactions back to the parent.
 * 
 * ================================================================
 * BEST PRACTICES FOLLOWED
 * ================================================================
 * 
 * 1. ✅ Single Responsibility - Only renders UI
 * 2. ✅ Props Destructuring - Clean, readable code
 * 3. ✅ Early Returns - Handles loading/error states
 * 4. ✅ Conditional Rendering - Shows different UI for different states
 * 5. ✅ Prop Types - (consider adding PropTypes or TypeScript)
 * 6. ✅ Controlled Components - Data flows down, events flow up
 * 7. ✅ Accessibility - Semantic HTML (table, th, tr, td)
 * 8. ✅ Tailwind Classes - Consistent styling
 * 9. ✅ Shadcn/ui Components - Consistent design system
 * 
 * ================================================================
 * POSSIBLE IMPROVEMENTS
 * ================================================================
 * 
 * 1. Add Pagination for large datasets
 * 2. Add Sorting functionality (click on column headers)
 * 3. Add Search/Filter bar
 * 4. Add "Select All" checkbox
 * 5. Add Batch Delete
 * 6. Add Export to CSV
 * 7. Add PropTypes for type checking
 * 
 * But for now, KEEP IT SIMPLE! ✨
 * ================================================================
 */