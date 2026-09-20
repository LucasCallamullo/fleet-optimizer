# Fleet Optimizer

The overall objective of the project is to implement a microservices-based backend solution for the comprehensive management of a package transport logistics system. The system enables the management of a vehicle fleet, package handling, and efficient delivery route planning, optimizing costs and times through distance calculation.

[![Static Badge](https://img.shields.io/badge/Documentation-EN-blue)](https://github.com/LucasCallamullo/fleet-optimizer/blob/main/README.md) [![Documentation ES](https://img.shields.io/badge/Documentation-ES-green)](https://github.com/LucasCallamullo/fleet-optimizer/blob/main/README-es.md) [![Contact](https://img.shields.io/badge/Contact-FF6C37?style=flat&logo=gmail&logoColor=white)](#contact)



<h2>Table of Contents</h2>
<ul>
  <li><a href="#technology-stack">Technology Stack</a></li>
  <li><a href="#quick-start-guide">Quick Start Guide</a></li>
  <li><a href="#c4-model">C4 Model</a></li>
  <li><a href="#database-schema-der">Database Schema</a></li>
  <li><a href="#authentication-flow">Authentication Flow</a></li>
  <li><a href="#shipment-creation-flow">Shipment Creation Flow</a></li>
  <li><a href="#key-design-decisions">Key Design Decisions</a></li>
  <li><a href="#contact">Contact</a></li>
</ul>



<h2 id="technology-stack">Technology Stack</h2>

|  | Tech Stack |
| :--- | :--- |
| **Backend** | ![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white) ![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-6DB33F?style=for-the-badge&logo=spring&logoColor=white) |
| **Frontend** | ![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB) ![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=for-the-badge&logo=typescript&logoColor=white) ![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white) |
| **Database** | ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white) ![H2 Database](https://img.shields.io/badge/H2_Database-0040CA?style=for-the-badge&logo=h2&logoColor=white) |
| **Security & Auth** | ![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white) ![OAuth 2.0 / OIDC](https://img.shields.io/badge/OAuth_2.0-EB5424?style=for-the-badge&logo=openid&logoColor=white) ![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=json-web-tokens&logoColor=white) |
| **DevOps & Infra** | ![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white) ![Docker Compose](https://img.shields.io/badge/Docker_Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white) ![Git](https://img.shields.io/badge/Git-F05033?style=for-the-badge&logo=git&logoColor=white) ![Nginx](https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white) |
| **Testing** | ![JUnit 5](https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white) ![Mockito](https://img.shields.io/badge/Mockito-78A641?style=for-the-badge&logo=mockito&logoColor=white) |
| **Docs** | ![Swagger / OpenAPI](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black) |



<h2 id="quick-start-guide">Quick Start Guide</h2>
<p>Follow these steps to set up the complete development environment.</p>

<h3>Prerequisites</h3>
<ul>
  <li>Docker and Docker Compose installed.</li>
  <li>Git to clone the repository.</li>
  <li>(Optional) Java 17 and Maven to run services without Docker.</li>
  <li>(Optional) Node 20 and npm to run the frontend without Docker.</li>
</ul>

<h3>Step by Step</h3>

<h4>1. Clone the repository</h4>
<pre><code>git clone https://github.com/LucasCallamullo/fleet-optimizer.git
cd fleet-optimizer</code></pre>

<h4>2. Configure environment variables</h4>
<p>The project includes an example file. Create your own <code>.env</code> file from it and adjust the values if needed.</p>
<pre><code>cp .env.example .env</code></pre>

<blockquote>
  <b>Note:</b> The <code>.env.example</code> file uses placeholders. To run the authentication flow,
  you need to create a free Auth0 tenant and configure it. The full step-by-step guide
  is available at <a href="./docs/AUTH0_SETUP.md"><code>docs/AUTH0_SETUP.md</code></a>.
</blockquote>

<h4>3. 🐳 Start all services with Docker Compose</h4>
<p>From the repository root, run:</p>
<pre><code>docker compose up -d --build</code></pre>
<p><strong>This downloads the base images (PostgreSQL, nginx, node, Maven)</strong>  and builds every backend service and the frontend. No manual JAR build is required; each service has a multi-stage Dockerfile that compiles the code inside the image.</p>

<h4>4. Verify everything is working</h4>
<ul>
  <li>
    <strong>Frontend App:</strong> <code>http://localhost</code> 
    (served by Nginx, proxies <code>/api</code> requests to the Gateway)
  </li>
  <li>
    <strong>Gateway (via Nginx):</strong> <code>http://localhost/api</code>
    (all API calls go through Nginx)
  </li>
</ul>
<p>
  Nginx acts as the single entry point on port 80. All <code>/api</code>
  traffic is internally proxied to the Gateway container. The Gateway is
  <strong>not exposed to the host</strong>; only Nginx can reach it.
</p>

<details>
  <summary>Optional Steps</summary>

  <h4>(Optional) Development / Debugging</h4>
  <p>
    By default, the Gateway is only reachable through Nginx. If you want to
    hit the API directly (e.g. with Postman or Swagger UI), temporarily
    uncomment the <code>ports</code> block in <code>docker-compose.yml</code>:
  </p>
  <pre><code>gateway-service:
    # ...
    ports:
      - "8080:8080"  # Uncomment for local debugging only</code></pre>
  <p>Then restart the stack and access:</p>
  <ul>
    <li>Gateway direct: <code>http://localhost:8080</code></li>
    <li>Swagger UI: <code>http://localhost:8080/swagger-ui.html</code></li>
  </ul>
  <p>
    <strong>Note:</strong> When the Gateway is exposed directly, the browser
    (or Postman) bypasses Nginx. CORS configuration in the Gateway applies,
    and headers like <code>X-Forwarded-For</code> will reflect the Gateway's
    own address instead of the real client IP. This is fine for debugging,
    but <strong>never</strong> use this in production.
  </p>

  <h4>(Optional) Run the backend or frontend without Docker</h4>
  <p>
    You still need the databases running. You can start only the databases
    with Docker while running the apps natively:
  </p>
  <pre><code>docker compose up -d postgres-fleets postgres-routes postgres-packages</code></pre>

  <p>Build the JARs manually (only if you want to run a service outside Docker):</p>
  <pre><code>cd backend
  mvn clean package -DskipTests -f gateway/pom.xml
  mvn clean package -DskipTests -f ms-auth/pom.xml
  mvn clean package -DskipTests -f ms-fleets/pom.xml
  mvn clean package -DskipTests -f ms-packages/pom.xml
  mvn clean package -DskipTests -f ms-geocoding/pom.xml
  mvn clean package -DskipTests -f ms-routes/pom.xml</code></pre>

  <p>If you want hot reload while developing the frontend:</p>
  <strong>Frontend (dev server):</strong> <code>http://localhost:5173</code>
  <pre><code>cd frontend
  npm install
  npm run dev</code></pre>

  <h4>(Optional) Stop the services</h4>
  <pre><code>docker compose down</code></pre>
  <p>To also remove the database volumes (this deletes all data):</p>
  <pre><code>docker compose down -v</code></pre>
</details>



<br></br>
<h2 id="c4-model">C4 Model</h2>

```mermaid
flowchart TD
    %% Users
    subgraph Users ["Users & Roles"]
        Client["Client"]
        Admin["Admin"]
        Shipper["Shipper"]
    end

    %% Frontend & Gateway
    subgraph Entry ["Entry Point"]
        Frontend["Client Interface FrontEnd<br/><i>(React / TypeScript)</i>"]
        Gateway["API Gateway<br/><i>(Java / Spring Cloud)</i><br/>Validate JWT + propagate headers"]
    end

    %% Core System
    subgraph System ["Logistics & Fleet Management System"]
        MS_Auth["MS Auth<br/><i>(Java / Spring Boot)</i><br/>Register / Login / Refresh / Logout (via Auth0)"]
        MS_Routes["MS Routes<br/><i>(Java / Spring Boot)</i><br/>Manages shipments, routes & legs"]
        MS_Geo["MS GeoCoding<br/><i>(Java / Spring Boot)</i><br/>Calculates distances"]
        MS_Packages["MS Packages<br/><i>(Java / Spring Boot)</i><br/>Package inventory & origin"]
        MS_Fleets["MS Fleets<br/><i>(Java / Spring Boot)</i><br/>Vehicle fleet management"]
    end

    %% External Services
    subgraph External ["External Services"]
        Auth0["Auth0<br/><i>(OAuth 2.0 / OIDC)</i>"]
        ORS["ORS API<br/><i>(External Map Service)</i>"]
    end

    %% Databases
    subgraph Databases ["Persistence Layer"]
        DB_Routes[("Routes DB<br/><i>PostgreSQL</i>")]
        DB_Packages[("Packages DB<br/><i>PostgreSQL</i>")]
        DB_Fleets[("Fleets DB<br/><i>PostgreSQL</i>")]
    end

    %% Flow Relationships
    Client & Admin & Shipper -->|Uses HTTPS| Frontend
    Frontend -->|API Requests| Gateway

    Gateway -->|Auth Requests| MS_Auth
    Gateway -->|Shipment Requests| MS_Routes
    Gateway -->|Package Requests| MS_Packages
    Gateway -->|Fleet Requests| MS_Fleets

    MS_Auth <-->|OAuth 2.0 / Token Validations| Auth0

    MS_Routes -->|Distance Queries| MS_Geo
    MS_Routes <-->|Package Validation| MS_Packages
    MS_Routes <-->|Vehicle Availability| MS_Fleets

    MS_Geo -->|Distance Matrix| ORS

    MS_Routes <-->|Reads / Writes| DB_Routes
    MS_Packages <-->|Reads / Writes| DB_Packages
    MS_Fleets <-->|Reads / Writes| DB_Fleets
```



<br></br>
<h2 id="database-schema-der">Database Schema (DER)</h2>
<p>Each microservice owns its own PostgreSQL database. The diagram below shows the tables and relationships for ms-fleets, ms-routes and ms-packages.</p>

```mermaid
  erDiagram
    CATEGORIES ||--o{ VEHICLES : "categorizes"
    STORES ||--o{ PACKAGES : "owns"
    ROUTES ||--o{ LEGS : "contains"
    VEHICLES ||--o{ LEGS : "assigned to"
    PACKAGES ||--o{ LEGS : "transported in"

    CATEGORIES {
        bigint id PK
        string name
        boolean active
    }

    VEHICLES {
        bigint id PK
        bigint category_id FK
        string license_plate
        decimal max_weight_kg
        decimal max_volume_cbm
        string status
    }

    STORES {
        bigint id PK
        string owner_id "Auth0 sub"
        string name
        string city
        string country
    }

    PACKAGES {
        bigint id PK
        bigint store_id FK
        string owner_id "Auth0 sub"
        string tracking_number
        string status
        decimal total_weight_kg
    }

    ROUTES {
        bigint id PK
        string owner_id "Auth0 sub"
        string status
        decimal estimated_distance_km
        int estimated_duration_minutes
    }

    LEGS {
        bigint id PK
        bigint route_id FK
        bigint vehicle_id FK
        bigint package_id FK
        int sequence
        decimal distance_km
        int duration_minutes
    }
```

<blockquote>
  <p><strong>Notes:</strong></p>
  <ul>
    <li>
      There is no local <code>users</code> table. User identity is managed by
      <strong>Auth0</strong>, and referenced by the <code>sub</code> claim
      (e.g. <code>auth0|6aaf...</code>) stored in the <code>owner_id</code> columns.
    </li>
    <li>
      <code>owner_id</code> is <strong>denormalized</strong> in
      <code>stores</code>, <code>packages</code> and <code>routes</code> to avoid
      N+1 cross-service calls when filtering resources by user.
    </li>
    <li>
      <code>legs.origin_*</code> and <code>legs.destination_*</code> store a
      <strong>snapshot</strong> of the address at shipment time, so historical
      routes remain accurate even if a store's address changes later.
    </li>
  </ul>
</blockquote>



<br></br>
<h2 id="authentication-flow">Authentication Flow (OAuth2 + JWT)</h2>
<h3>Flow steps:</h3>
<ol>
  <li>
    <strong>Authentication Start:</strong>
    The user logs in from the frontend with their credentials.
  </li>
  <li>
    <strong>Login in Gateway:</strong>
    The frontend sends the credentials to the <code>/api/v1/auth/login</code> endpoint of the API Gateway.
  </li>
  <li>
    <strong>Validation in Auth0:</strong>
    The Gateway routes the request to the <code>ms-auth</code> microservice, which validates the credentials against Auth0 and obtains a JWT token.
  </li>
  <li>
    <strong>Token to Frontend:</strong>
    The Gateway returns the JWT token to the frontend.
  </li>
  <li>
    <strong>Request with Token:</strong>
    The frontend sends the token in the <code>Authorization: Bearer &lt;token&gt;</code> header in every subsequent request.
  </li>
  <li>
    <strong>Validation and Routing:</strong>
    The Gateway validates the JWT token (signature and expiration), extracts user information (ID, roles, email) 
    from the JWT custom claims (https://fo.dev/roles, https://fo.dev/email) and injects them as headers
    (<code>X-User-Id</code>, <code>X-User-Email</code>, <code>X-User-Roles</code>).
  </li>
  <li>
    <strong>Authorization in Microservices:</strong>
    The destination microservice receives the user context (via headers) and uses <code>@PreAuthorize</code> to control access to endpoints based on roles.
  </li>
</ol>

```mermaid
  sequenceDiagram
    participant U as User
    participant F as Frontend
    participant G as Gateway
    participant A as ms-auth
    participant K as Auth0

    U->>F: Enter credentials
    F->>G: POST /api/v1/auth/login
    G->>A: Forward request
    A->>K: Validate credentials
    K-->>A: JWT
    A-->>G: JWT
    G-->>F: JWT
    F->>G: Request with Authorization: Bearer <token>
    G->>G: Validate JWT, extract roles from custom claims
    G->>G: Inject X-User-Id, X-User-Roles, X-User-Email
    G->>A: Forward with headers
```



<br></br>
<h2 id="shipment-creation-flow">Shipment Creation Flow (Core Business Orchestration)</h2>
<p>
  This sequence diagram illustrates the main business flow of the system: creating a shipment.
  It highlights how <code>ms-routes</code> orchestrates validation calls to other microservices
  <code>ms-fleets</code>, <code>ms-packages</code>,
  calculates distances via <code>ms-geocoding</code>, and atomically persists the route and its legs.
</p>

```mermaid
sequenceDiagram
    autonumber
    actor User as User (Admin | Client)
    participant F as Frontend (React)
    participant G as API Gateway
    participant R as ms-routes
    participant P as ms-packages
    participant Fl as ms-fleets
    participant Geo as ms-geocoding
    participant DB_R as Routes DB
    participant DB_P as Packages DB
    participant ORS as OpenRouteService

    Note over User,F: User is already authenticated (JWT stored in frontend)

    User->>F: Open "Create Shipment" page
    F->>G: GET /api/v1/packages (with Bearer token)
    G->>G: Validate JWT, inject X-User-Id, X-User-Roles
    G->>P: Forward request + user headers
    P->>DB_P: Fetch available packages for user
    DB_P-->>P: Packages list
    P-->>G: Packages response
    G-->>F: Packages response
    F-->>User: Render packages list

    F->>G: GET /api/v1/vehicles?minWeight=&minVolume=
    G->>Fl: Forward request
    Fl-->>G: Filtered vehicles
    G-->>F: Filtered vehicles
    F-->>User: Render vehicle options

    User->>F: Select destination (Geocoding autocomplete)

    User->>F: Select vehicle (filtered by weight/volume)

    User->>F: Submit shipment form
    F->>G: POST /api/v1/shipments (packageId, vehicleId, destination)
    G->>G: Validate JWT, inject X-User-Id, X-User-Roles
    G->>R: Forward shipment creation request

    Note over R: Orchestration starts

    R->>P: GET /api/v1/packages/{id} (validate package exists & status)
    P-->>R: Package details

    R->>Fl: GET /api/v1/vehicles/{id} (validate vehicle availability)
    Fl-->>R: Vehicle details

    R->>Fl: POST /api/v1/vehicles/{id}/reserve (optional: mark as in-use)
    Fl-->>R: Vehicle reserved

    R->>Geo: POST /api/v1/distance (batch: all legs)
    Geo->>ORS: Request route matrix
    ORS-->>Geo: Distances + ETAs
    Geo-->>R: Distances + ETAs

    R->>DB_R: Persist Route + Legs (atomic transaction)
    DB_R-->>R: Route saved

    R->>P: PATCH /api/v1/packages/{id}/status (IN_TRANSIT)
    P->>DB_P: Update package status
    DB_P-->>P: Updated
    P-->>R: Status updated

    R-->>G: Shipment created (route + legs + totals)
    G-->>F: Shipment response
    F-->>User: Redirect to shipment detail page
```



<br></br>
<h2>🖥️ Frontend Screenshots</h2>
<p>The application provides an intuitive interface for managing all aspects of the logistics system. Below are some of the main screens.</p>

<table>
  <tr>
    <td align="center">
      <strong>Dashboard</strong>
    </td>
    <td align="center">
      <strong>Fleet Management</strong>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://raw.githubusercontent.com/LucasCallamullo/fleet-optimizer/refs/heads/main/docs/img/front1.png" alt="Dashboard" width="400"/>
      <br>
      <em>Dashboard with JWT info and quick access</em>
    </td>
    <td align="center">
      <img src="https://raw.githubusercontent.com/LucasCallamullo/fleet-optimizer/refs/heads/main/docs/img/front2.png" alt="Fleet Management" width="400"/>
      <br>
      <em>Vehicles table with capacities and status</em>
    </td>
  </tr>

  <tr>
    <td align="center">
      <strong>Package Detail</strong>
    </td>
    <td align="center">
      <strong>Vehicle Selection</strong>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://raw.githubusercontent.com/LucasCallamullo/fleet-optimizer/refs/heads/main/docs/img/front3.png" alt="Package Detail" width="400"/>
      <br>
      <em>Package details with destination selector</em>
    </td>
    <td align="center">
      <img src="https://raw.githubusercontent.com/LucasCallamullo/fleet-optimizer/refs/heads/main/docs/img/front4.png" alt="Vehicle Selection" width="400"/>
      <br>
      <em>Available vehicles filtered by capacity</em>
    </td>
  </tr>

  <tr>
    <td align="center">
      <strong>graph services</strong>
    </td>
    <td align="center">
      <strong>DER</strong>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://raw.githubusercontent.com/LucasCallamullo/fleet-optimizer/refs/heads/main/docs/img/fleet_optimizer_c4.png" 
      alt="C4 Model" width="400"/>
      <br>
      <em>graph services with Draw.io</em>
    </td>
    <td align="center">
      <img src="https://raw.githubusercontent.com/LucasCallamullo/fleet-optimizer/refs/heads/main/docs/img/fleet_optimizer_DER.png" alt="C4 Model" width="400" height="300"/>
      <br>
      <em>DER with Draw.io</em>
    </td>
  </tr>
</table>



<br></br>
<h2 id="key-design-decisions">Key Design Decisions</h2>
<ul>
  <li>
    <strong>Single Gateway entry point:</strong> only the Gateway is exposed to the host.
    All microservices and databases live on an internal Docker network.
    This reduces the attack surface and centralizes cross-cutting concerns (auth, logging, rate limiting).
  </li>
  <li>
    <strong>Database per service:</strong> each microservice owns its PostgreSQL database.
    No shared schema, no cross-service joins. Communication is via HTTP only.
  </li>
  <li>
    <strong>Managed Identity Provider (Auth0):</strong> instead of self-hosting Keycloak,
    the project uses Auth0 to avoid the operational overhead of running and patching an IdP.
  </li>
  <li>
    <strong>Orchestration in ms-routes:</strong> the Routes Service acts as the orchestrator
    for shipment creation, coordinating validation with Packages and Fleets before persisting.
  </li>
  <li>
    <strong>Nginx as reverse proxy:</strong> serves the compiled SPA and proxies <code>/api</code>
    traffic to the Gateway, keeping the browser on a single origin (no CORS issues).
  </li>
</ul>

<blockquote>
  <b>Future work:</b> rate limiting, observability, and CI/CD are on the roadmap 
  and will be added as the project evolves.
</blockquote>

<br></br>
<details>
  <summary>Service responsibilities</summary>

  <h2>General Architecture</h2>

  <p>The system follows a modern architecture composed of multiple independent microservices, each responsible for a specific domain:</p>

  <h3>🔹 API Gateway</h3>
  <p>Single entry point for all frontend applications or external clients. Responsible for routing and communication to each microservice, as well as validating JWT tokens issued by Auth0 and propagating user context.</p>

  <h3>🔹 Authentication Service (ms-auth)</h3>
  <p>Centralizes user management and authentication. Integrates with Auth0 (OAuth2/OpenID Connect) for JWT token issuance and refresh, and role/permission management.</p>

  <h3>🔹 Fleet Service (ms-fleets)</h3>
  <p>Manages the vehicle catalog. Handles CRUD operations for vehicles and their categories, including capacities (maximum weight and volume), operational costs, and availability status.</p>

  <h3>🔹 Packages Service (ms-packages)</h3>
  <p>Manages the package lifecycle from creation (with weight, volume, and origin) to final status (CREATED, PROCESSING, READY_FOR_PICKUP, IN_TRANSIT, DELIVERED, etc.). Each package is associated with a source store.</p>

  <h3>🔹 Routes Service (ms-routes)</h3>
  <p>Orchestrates the shipment creation process. Coordinates package and vehicle validation with other microservices, calculates distances and times through the Geocoding MS, and atomically persists routes and their legs. Each package in a shipment becomes a leg of the route.</p>

  <h3>🔹 Frontend (React)</h3>
  <p>Client application developed with React that consumes the Gateway API. Provides a user interface for managing packages, vehicles, routes, and shipment tracking. Communicates exclusively with the API Gateway, which acts as an intermediary with the rest of the microservices.</p>

  <h3>🔹 Geocoding Service (ms-geocoding)</h3>
  <p>Microservice dedicated exclusively to route and distance calculation based on geographic coordinates (latitude/longitude). Consumes the <strong>OpenRouteService (ORS)</strong> API, a routing service that requires an API key for usage. Supports distance and estimated time calculation to optimize system costs and logistics. Implements a batch endpoint to process multiple locations in a single call.</p>
</details>



<br></br>
<div align="center">
  <h2 id="contact">Contact</h2>
  <h3>Lucas Callamullo | Backend Software Engineer</h3>
  <sub>Specialized in Backend Development & Full Stack Web Solutions</sub>
  <br></br>

  | [![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/LucasCallamullo) | [![LinkedIn](https://img.shields.io/badge/LinkedIn-0A66C2?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/lucas-callamullo/) | [![Gmail](https://img.shields.io/badge/Email-EA4335?style=for-the-badge&logo=gmail&logoColor=white)](mailto:lucas.callamullo.dev@gmail.com) | [![Portfolio](https://img.shields.io/badge/Portfolio-000000?style=for-the-badge&logo=react&logoColor=61DAFB)](https://lucascallamullo.github.io) | [![YouTube](https://img.shields.io/badge/YouTube-FF0000?style=for-the-badge&logo=youtube&logoColor=white)](https://www.youtube.com/@lucas_backend13/featured) |
  |:-:|:-:|:-:|:-:|:-:|

</div>