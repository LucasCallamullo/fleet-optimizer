package com.packages.dto.response;

import com.packages.model.embedded.Location;

/**
 * Store response DTO with full location details.
 * Used within PackageDetailDTO.
 */
public record StoreResponseDTO(
    Long id,
    String name,
    String description,
    Location location,
    String ownerId
) {}

/*
JSON Resultante embebido

{
  "id": 1,
  "name": "Downtown Warehouse",
  "description": "Main warehouse in downtown Buenos Aires",
  "location": {
    "street": "Av. Libertador",
    "streetNumber": "1000",
    "city": "Buenos Aires",
    "state": "CABA",
    "country": "Argentina",
    "postalCode": "1000",
    "latitude": -34.6037,
    "longitude": -58.3816
  },
  "ownerId": "461f1c90-d3d9-4135-aa72-efc5911826ed"
}

*/