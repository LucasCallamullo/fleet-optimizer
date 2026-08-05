package com.routes.dto.client.fleets;

/**
 * Vehicle DTO for inter-service communication with the Fleet Microservice (ms-fleets).
 * 
 * This DTO contains the vehicle information needed by ms-routes for:
 * - Capacity validation (weight and volume limits)
 * - Cost calculation for route planning (fuel consumption, cost per km)
 * - Vehicle availability checking (status)
 * 
 * The DTO is intentionally minimal - it only includes fields that ms-routes
 * actually uses. Extra fields from ms-fleets responses are ignored by Feign.
 * 
 * Field Mapping from ms-fleets VehicleDetailDTO:
 * - id → Vehicle identifier
 * - licensePlate → Vehicle registration
 * - fuelConsumptionPerKm → Fuel efficiency for cost estimation
 * - costPerKm → Operational cost per kilometer
 * - pricePerKm → Sale price per kilometer
 * - maxWeightKg → Maximum cargo weight capacity
 * - maxVolumeCbm → Maximum cargo volume capacity
 * - status → Vehicle availability (AVAILABLE, IN_TRANSIT, etc.)
 * 
 * Why no external DTO in ms-fleets?
 * This DTO is defined in ms-routes (the consumer), not in ms-fleets (the provider).
 * Feign automatically maps the VehicleDetailDTO response to this DTO, ignoring
 * extra fields like category, year, createdAt, etc.
 * 
 * Usage Example:
 *   FleetVehicleDTO vehicle = fleetClient.getVehicleById(vehicleId);
 *   if (vehicle.status().equals("AVAILABLE")) {
 *       validateCapacity(vehicle.maxWeightKg(), vehicle.maxVolumeCbm());
 *       double cost = vehicle.costPerKm() * distance;
 *   }
 * 
 * @see com.routes.client.FleetClient
 * @see com.routes.service.ShipmentService
 */
public record FleetVehicleDTO(
    
    /**
     * Unique identifier of the vehicle in ms-fleets.
     */
    Long id,
    
    /**
     * Vehicle license plate number.
     * Used for identification and logging.
     */
    String licensePlate,
    
    /**
     * Fuel consumption in liters per kilometer.
     * Used to estimate fuel costs for the route.
     * Optional - may be null if not configured.
     */
    Double fuelConsumptionPerKm,
    
    /**
     * Operational cost per kilometer.
     * Includes maintenance, depreciation, etc.
     * Used for cost calculation in route planning.
     * Optional - may be null if not configured.
     */
    Double costPerKm,
    
    /**
     * Sale price per kilometer charged to the customer.
     * Used for revenue estimation.
     * Optional - may be null if not configured.
     */
    Double pricePerKm,
    
    /**
     * Maximum cargo capacity in kilograms.
     * Used to validate that packages do not exceed vehicle weight limit.
     * Optional - may be null if not configured.
     */
    Double maxWeightKg,
    
    /**
     * Maximum cargo capacity in cubic meters.
     * Used to validate that packages do not exceed vehicle volume limit.
     * Optional - may be null if not configured.
     */
    Double maxVolumeCbm,
    
    /**
     * Current vehicle status.
     * Must be "AVAILABLE" to be used for new shipments.
     * Other values: "IN_TRANSIT", "MAINTENANCE", "OUT_OF_SERVICE", "RESERVED"
     */
    String status
) {}