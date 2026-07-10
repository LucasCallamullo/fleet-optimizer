package com.packages.config;

import com.packages.model.embedded.Location;
import com.packages.model.entity.Package;
import com.packages.model.entity.Store;
import com.packages.model.enums.PackageStatus;
import com.packages.repository.PackageRepository;
import com.packages.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SeedDataService {

    private final StoreRepository storeRepository;
    private final PackageRepository packageRepository;

    // ================================================================
    // USER IDs
    // ================================================================
    private static final String ADMIN_ID = "461f1c90-d3d9-4135-aa72-efc5911826ed";
    private static final String USER_ID = "811bbc42-afd8-4e58-ab51-5cb97c9ec347";

    /**
     * Seeds initial data for testing purposes.
     * Returns true if data was seeded, false if already exists.
     */
    public boolean seedAllData() {
        if (storeRepository.count() > 0) {
            log.info("Data already seeded, skipping...");
            return false;
        }

        log.info("Seeding initial data for ms-packages...");

        // Step 1: Create stores
        List<Store> stores = createStores();
        List<Store> savedStores = storeRepository.saveAll(stores);
        log.info("Created {} stores", savedStores.size());

        // Step 2: Create packages for each store
        List<Package> allPackages = new ArrayList<>();
        for (Store store : savedStores) {
            List<Package> packages = createPackagesForStore(store);
            allPackages.addAll(packages);
        }

        List<Package> savedPackages = packageRepository.saveAll(allPackages);
        log.info("Created {} packages successfully", savedPackages.size());

        logSummary(savedStores, savedPackages);
        return true;
    }

    /**
     * Creates 2 default stores with locations.
     */
    private List<Store> createStores() {
        // Store 1: Downtown Warehouse
        Location location1 = new Location();
        location1.setStreet("Av. Libertador");
        location1.setStreetNumber("1000");
        location1.setCity("Buenos Aires");
        location1.setState("CABA");
        location1.setCountry("Argentina");
        location1.setPostalCode("1000");
        location1.setLatitude(-34.6037);
        location1.setLongitude(-58.3816);

        Store store1 = new Store();
        store1.setName("Downtown Warehouse");
        store1.setDescription("Main warehouse in downtown Buenos Aires");
        store1.setLocation(location1);
        store1.setOwnerId(ADMIN_ID);

        // Store 2: North Distribution Center
        Location location2 = new Location();
        location2.setStreet("Av. Del Libertador");
        location2.setStreetNumber("2000");
        location2.setCity("Buenos Aires");
        location2.setState("CABA");
        location2.setCountry("Argentina");
        location2.setPostalCode("1428");
        location2.setLatitude(-34.5585);
        location2.setLongitude(-58.4499);

        Store store2 = new Store();
        store2.setName("North Distribution Center");
        store2.setDescription("Distribution center serving the northern area");
        store2.setLocation(location2);
        store2.setOwnerId(ADMIN_ID);

        return List.of(store1, store2);
    }

    /**
     * Creates 30 packages for a given store with various weights, volumes, and statuses.
     * 
     * Package distribution:
     * - CREATED: 5 packages
     * - PROCESSING: 5 packages
     * - READY_FOR_PICKUP: 5 packages
     * - IN_TRANSIT: 5 packages
     * - DELIVERED: 5 packages
     * - ON_HOLD: 3 packages
     * - CANCELLED: 2 packages
     * 
     * Owner distribution: Mixed between ADMIN and USER
     * Weight range: 0.5 kg to 50 kg
     * Volume range: 0.01 m³ to 1.5 m³
     */
    private List<Package> createPackagesForStore(Store store) {
        List<Package> packages = new ArrayList<>();
        String storePrefix = String.format("%02d", store.getId());

        // ================================================================
        // CREATED (5 packages)
        // ================================================================
        packages.add(createPackage("PKG-CRE-001-" + storePrefix, store, 0.5, 0.01, USER_ID, PackageStatus.CREATED));
        packages.add(createPackage("PKG-CRE-002-" + storePrefix, store, 1.2, 0.03, USER_ID, PackageStatus.CREATED));
        packages.add(createPackage("PKG-CRE-003-" + storePrefix, store, 2.5, 0.05, ADMIN_ID, PackageStatus.CREATED));
        packages.add(createPackage("PKG-CRE-004-" + storePrefix, store, 3.0, 0.08, USER_ID, PackageStatus.CREATED));
        packages.add(createPackage("PKG-CRE-005-" + storePrefix, store, 4.5, 0.10, ADMIN_ID, PackageStatus.CREATED));

        // ================================================================
        // PROCESSING (5 packages)
        // ================================================================
        packages.add(createPackage("PKG-PRO-001-" + storePrefix, store, 5.0, 0.15, USER_ID, PackageStatus.PROCESSING));
        packages.add(createPackage("PKG-PRO-002-" + storePrefix, store, 6.5, 0.18, ADMIN_ID, PackageStatus.PROCESSING));
        packages.add(createPackage("PKG-PRO-003-" + storePrefix, store, 7.0, 0.20, USER_ID, PackageStatus.PROCESSING));
        packages.add(createPackage("PKG-PRO-004-" + storePrefix, store, 8.5, 0.25, ADMIN_ID, PackageStatus.PROCESSING));
        packages.add(createPackage("PKG-PRO-005-" + storePrefix, store, 9.0, 0.28, USER_ID, PackageStatus.PROCESSING));

        // ================================================================
        // READY_FOR_PICKUP (5 packages)
        // ================================================================
        packages.add(createPackage("PKG-RDY-001-" + storePrefix, store, 10.0, 0.30, ADMIN_ID, PackageStatus.READY_FOR_PICKUP));
        packages.add(createPackage("PKG-RDY-002-" + storePrefix, store, 12.5, 0.35, USER_ID, PackageStatus.READY_FOR_PICKUP));
        packages.add(createPackage("PKG-RDY-003-" + storePrefix, store, 15.0, 0.50, ADMIN_ID, PackageStatus.READY_FOR_PICKUP));
        packages.add(createPackage("PKG-RDY-004-" + storePrefix, store, 18.0, 0.55, USER_ID, PackageStatus.READY_FOR_PICKUP));
        packages.add(createPackage("PKG-RDY-005-" + storePrefix, store, 20.0, 0.60, ADMIN_ID, PackageStatus.READY_FOR_PICKUP));

        // ================================================================
        // IN_TRANSIT (5 packages)
        // ================================================================
        packages.add(createPackage("PKG-TRN-001-" + storePrefix, store, 22.0, 0.65, USER_ID, PackageStatus.IN_TRANSIT));
        packages.add(createPackage("PKG-TRN-002-" + storePrefix, store, 25.0, 0.70, ADMIN_ID, PackageStatus.IN_TRANSIT));
        packages.add(createPackage("PKG-TRN-003-" + storePrefix, store, 28.0, 0.75, USER_ID, PackageStatus.IN_TRANSIT));
        packages.add(createPackage("PKG-TRN-004-" + storePrefix, store, 30.0, 0.80, ADMIN_ID, PackageStatus.IN_TRANSIT));
        packages.add(createPackage("PKG-TRN-005-" + storePrefix, store, 35.0, 0.90, USER_ID, PackageStatus.IN_TRANSIT));

        // ================================================================
        // DELIVERED (5 packages)
        // ================================================================
        packages.add(createPackage("PKG-DLV-001-" + storePrefix, store, 8.0, 0.25, USER_ID, PackageStatus.DELIVERED));
        packages.add(createPackage("PKG-DLV-002-" + storePrefix, store, 15.0, 0.40, ADMIN_ID, PackageStatus.DELIVERED));
        packages.add(createPackage("PKG-DLV-003-" + storePrefix, store, 20.0, 0.55, USER_ID, PackageStatus.DELIVERED));
        packages.add(createPackage("PKG-DLV-004-" + storePrefix, store, 25.0, 0.65, ADMIN_ID, PackageStatus.DELIVERED));
        packages.add(createPackage("PKG-DLV-005-" + storePrefix, store, 40.0, 1.00, USER_ID, PackageStatus.DELIVERED));

        // ================================================================
        // ON_HOLD (3 packages)
        // ================================================================
        packages.add(createPackage("PKG-HLD-001-" + storePrefix, store, 20.0, 0.60, ADMIN_ID, PackageStatus.ON_HOLD));
        packages.add(createPackage("PKG-HLD-002-" + storePrefix, store, 30.0, 0.75, USER_ID, PackageStatus.ON_HOLD));
        packages.add(createPackage("PKG-HLD-003-" + storePrefix, store, 45.0, 1.20, ADMIN_ID, PackageStatus.ON_HOLD));

        // ================================================================
        // CANCELLED (2 packages)
        // ================================================================
        packages.add(createPackage("PKG-CAN-001-" + storePrefix, store, 3.5, 0.10, USER_ID, PackageStatus.CANCELLED));
        packages.add(createPackage("PKG-CAN-002-" + storePrefix, store, 6.0, 0.18, ADMIN_ID, PackageStatus.CANCELLED));

        return packages;
    }

    /**
     * Helper method to create a single package.
     */
    private Package createPackage(String trackingNumber, Store store, double weight, double volume, String ownerId, PackageStatus status) {
        Package pkg = new Package();
        pkg.setTrackingNumber(trackingNumber);
        pkg.setStore(store);
        pkg.setTotalWeightKg(weight);
        pkg.setTotalVolumeCbm(volume);
        pkg.setOwnerId(ownerId);
        pkg.setStatus(status);
        return pkg;
    }

    /**
     * Logs summary of seeded data.
     */
    private void logSummary(List<Store> stores, List<Package> packages) {
        log.info("=== SEED DATA SUMMARY ===");
        log.info("Stores: {}, Packages: {}", stores.size(), packages.size());

        for (Store store : stores) {
            long count = packageRepository.findByStoreId(store.getId()).size();
            log.info("Store '{}' has {} packages", store.getName(), count);
        }

        log.info("=== USERS FOR TESTING ===");
        log.info("Admin: admin_regular@gmail.com / pass1234 (id: {})", ADMIN_ID);
        log.info("User: user_regular@gmail.com / pass1234 (id: {})", USER_ID);
        log.info("========================================");
    }
}
