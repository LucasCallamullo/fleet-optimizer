# Fleet Optimizer 2025

The overall objective of the project is to implement a microservices-based backend solution for the comprehensive management of a package transport logistics system. The system enables the management of a vehicle fleet, package handling, and efficient delivery route planning, optimizing costs and times through distance calculation.

[![Static Badge](https://img.shields.io/badge/Documentation-EN-blue)](https://github.com/LucasCallamullo/fleet-optimizer/blob/main/README.md) [![Documentation ES](https://img.shields.io/badge/Documentation-ES-green)](https://github.com/LucasCallamullo/fleet-optimizer/blob/main/README-es.md) [![Contact](https://img.shields.io/badge/Contact-FF6C37?style=flat&logo=gmail&logoColor=white)](#contact)


## Technology Stack

|  | Tech Stack |
| :--- | :--- |
| **Backend** | ![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white) ![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-6DB33F?style=for-the-badge&logo=spring&logoColor=white) |
| **Frontend** | ![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB) ![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=for-the-badge&logo=typescript&logoColor=white) ![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white) |
| **Persistence & Data** | ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white) ![H2 Database](https://img.shields.io/badge/H2_Database-0040CA?style=for-the-badge&logo=h2&logoColor=white) |
| **Security & Auth** | ![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white) ![Keycloak](https://img.shields.io/badge/Keycloak-0085CA?style=for-the-badge&logo=keycloak&logoColor=white) ![OAuth 2.0 / OIDC](https://img.shields.io/badge/OAuth_2.0-EB5424?style=for-the-badge&logo=openid&logoColor=white) ![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=json-web-tokens&logoColor=white) |
| **DevOps & Infra** | ![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white) ![Docker Compose](https://img.shields.io/badge/Docker_Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white) ![Git](https://img.shields.io/badge/Git-F05033?style=for-the-badge&logo=git&logoColor=white) ![Swagger / OpenAPI](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black) |
| **Testing** | ![JUnit 5](https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white) ![Mockito](https://img.shields.io/badge/Mockito-78A641?style=for-the-badge&logo=mockito&logoColor=white) |


<hr>

<h2>Quick Start Guide</h2>
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
<pre><code>cp .env.example .env
# No need to edit the created .env file, the Keycloak keys are valid.</code></pre>

<h4>3. 🐳 Start all services with Docker Compose</h4>
<p>From the repository root, run:</p>
<pre><code>docker compose up -d --build</code></pre>
<p><strong>This downloads the base images (PostgreSQL, nginx, node, Maven)</strong>  and builds every backend service and the frontend. No manual JAR build is required; each service has a multi-stage Dockerfile that compiles the code inside the image.</p>

<h4>4. Verify everything is working</h4>
<ul>
  <li><strong>Frontend:</strong> <code>http://localhost</code></li>
  <li><strong>Gateway:</strong> <code>http://localhost:8080</code></li>
</ul>
<p>The frontend is served by nginx on port 80 and proxies <code>/api</code> requests to the Gateway. The Gateway is the only backend entry point; microservices and databases are internal and not exposed to the host.</p>

<h4>5. (Optional) Stop the services</h4>
<pre><code>docker compose down</code></pre>
<p>To also remove the database volumes (this deletes all data):</p>
<pre><code>docker compose down -v</code></pre>

<h4>6. (Optional) Run the backend or frontend without Docker</h4>

<p>Build the JARs manually (only if you want to run a service outside Docker)</p>
<pre><code>cd backend
mvn clean package -DskipTests -f gateway/pom.xml
mvn clean package -DskipTests -f ms-auth/pom.xml
mvn clean package -DskipTests -f ms-fleets/pom.xml
mvn clean package -DskipTests -f ms-packages/pom.xml
mvn clean package -DskipTests -f ms-geocoding/pom.xml
mvn clean package -DskipTests -f ms-routes/pom.xml
</code></pre>

<p>If you want hot reload while developing the frontend:</p><strong>Frontend (dev server):</strong> <code>http://localhost:5173</code>
<pre><code>cd frontend
npm install
npm run dev</code></pre>


<hr>

<h2>C4 Model</h2>

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
        MS_Auth["MS Auth<br/><i>(Java / Spring Boot)</i><br/>Login / Refresh JWT"]
        MS_Routes["MS Routes<br/><i>(Java / Spring Boot)</i><br/>Manages shipments, routes & legs"]
        MS_Geo["MS GeoCoding<br/><i>(Java / Spring Boot)</i><br/>Calculates distances"]
        MS_Packages["MS Packages<br/><i>(Java / Spring Boot)</i><br/>Package inventory & origin"]
        MS_Fleets["MS Fleets<br/><i>(Java / Spring Boot)</i><br/>Vehicle fleet management"]
    end

    %% External Services
    subgraph External ["External Services"]
        Keycloak["Keycloak<br/><i>(OAuth 2.0 / OIDC)</i>"]
        OSRM["OSRM API<br/><i>(External Map Service)</i>"]
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

    MS_Auth <-->|OAuth 2.0 / Token Validations| Keycloak

    MS_Routes -->|Distance Queries| MS_Geo
    MS_Routes <-->|Package Validation| MS_Packages
    MS_Routes <-->|Vehicle Availability| MS_Fleets

    MS_Geo -->|Distance Matrix| OSRM

    MS_Routes <-->|Reads / Writes| DB_Routes
    MS_Packages <-->|Reads / Writes| DB_Packages
    MS_Fleets <-->|Reads / Writes| DB_Fleets
```

<h2>Database Schema (DER)</h2>
<p>Each microservice owns its own PostgreSQL database. The diagram below shows the tables and relationships for ms-fleets, ms-routes and ms-packages.</p>

![](https://raw.githubusercontent.com/LucasCallamullo/fleet-optimizer/refs/heads/main/docs/img/fleet_optimizer_DER.png)


<hr>


<details>
  <summary>Service responsibilities</summary>

<h2>General Architecture</h2>

<p>The system follows a modern architecture composed of multiple independent microservices, each responsible for a specific domain:</p>

<h3>🔹 API Gateway</h3>
<p>Single entry point for all frontend applications or external clients. Responsible for routing and communication to each microservice, as well as validating JWT tokens issued by Keycloak and propagating user context.</p>

<h3>🔹 Authentication Service (ms-auth)</h3>
<p>Centralizes user management and authentication. Integrates with Keycloak (OAuth2/OpenID Connect) for JWT token issuance and refresh, and role/permission management.</p>

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

<hr>

<h2>Authentication Flow (OAuth2 + JWT)</h2>
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
    <strong>Validation in Keycloak:</strong>
    The Gateway routes the request to the <code>ms-auth</code> microservice, which validates the credentials against Keycloak and obtains a JWT token.
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
    The Gateway validates the JWT token (signature and expiration), extracts user information (ID, roles) from <code>realm_access</code>, and injects it as headers (<code>X-User-Id</code>, <code>X-User-Roles</code>).
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
    participant K as Keycloak

    U->>F: Enter credentials
    F->>G: POST /api/v1/auth/login
    G->>A: Forward request
    A->>K: Validate credentials
    K-->>A: JWT
    A-->>G: JWT
    G-->>F: JWT
    F->>G: Request with Authorization: Bearer <token>
    G->>G: Validate JWT, extract roles
    G->>G: Inject X-User-Id, X-User-Roles
    G->>A: Forward with headers
```

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



  </tr>
  <tr>
    <td align="center">
      <img src="https://raw.githubusercontent.com/LucasCallamullo/fleet-optimizer/refs/heads/main/docs/img/fleet_optimizer_c4.png" alt="C4 Model" width="400"/>
      <br>
      <em>graph services with Draw.io</em>
    </td>




  </tr>
</table>

<hr>

<h2 id="contact">Contact</h2>
<h3>Lucas Callamullo - Backend Developer</h3>

| [![GitHub](https://img.shields.io/badge/github-%23121011.svg?&style=for-the-badge&logo=github&logoColor=white)](https://github.com/LucasCallamullo) | [![LinkedIn](https://img.shields.io/badge/linkedin-%230077B5.svg?&style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/lucas-callamullo/) | [![Email](https://img.shields.io/badge/Email-D14836?style=for-the-badge&logo=gmail&logoColor=white)](mailto:lucas.callamullo.dev@gmail.com) |
|:-:|:-:|:-:|

| [![Portfolio](https://img.shields.io/badge/Portfolio-%23000000.svg?style=for-the-badge&logo=react&logoColor=white)](https://lucascallamullo.github.io) | [![Youtube Badge](https://img.shields.io/badge/YouTube%20-%23FF0000.svg?&style=for-the-badge&logo=YouTube&logoColor=white)](https://www.youtube.com/@lucas_clases_python) |
|:-:|:-:|
