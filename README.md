# Fleet Optimizer 2025

The overall objective of the project is to implement a microservices-based backend solution for the comprehensive management of a package transport logistics system. The system enables the management of a vehicle fleet, package handling, and efficient delivery route planning, optimizing costs and times through distance calculation.

[![Static Badge](https://img.shields.io/badge/Documentation-EN-blue)](https://github.com/LucasCallamullo/fleet-optimizer/blob/main/README.md) [![Documentation ES](https://img.shields.io/badge/Documentation-ES-green)](https://github.com/LucasCallamullo/fleet-optimizer/blob/main/README-es.md) [![Contact](https://img.shields.io/badge/Contact-FF6C37?style=for-the-badge&logo=gmail&logoColor=white)](#contact)


## Technology Stack

### Backend
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-6DB33F?style=for-the-badge&logo=spring&logoColor=white)

### Frontend
![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black)
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white)

### Database & ORM
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![H2](https://img.shields.io/badge/H2_Database-0040CA?style=for-the-badge&logo=h2&logoColor=white)
![JPA](https://img.shields.io/badge/JPA-Hibernate-59666C?style=for-the-badge&logo=hibernate&logoColor=white)

### Security
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white)
![Keycloak](https://img.shields.io/badge/Keycloak-0085CA?style=for-the-badge&logo=keycloak&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=json-web-tokens&logoColor=white)

### Tools & DevOps
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Docker Compose](https://img.shields.io/badge/Docker_Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Git Badge](https://img.shields.io/badge/git%20-%23F05033.svg?&style=for-the-badge&logo=git&logoColor=white) 
![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)

### Testing
![JUnit](https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white)
![Mockito](https://img.shields.io/badge/Mockito-78A641?style=for-the-badge&logo=mockito&logoColor=white)
![Postman](https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white)


<hr>

<h2>Quick Start Guide</h2>
<p>Follow these steps to set up the complete development environment.</p>

<h3>Prerequisites</h3>
<ul>
  <li>Docker and Docker Compose installed.</li>
  <li>Git to clone the repository.</li>
  <li>(Optional) Java 17 and Maven to run services without Docker.</li>
</ul>

<h3>Step by Step</h3>

<h4>1. Clone the repository</h4>
<pre><code>git clone https://github.com/LucasCallamullo/fleet-optimizer.git
cd fleet-optimizer</code></pre>

<h4>2. Configure environment variables</h4>
<p>The project includes an example file. Create your own <code>.env</code> file from it and adjust the values if needed.</p>
<pre><code>cp .env.example .env
# No need to edit the created .env file, the Keycloak keys are valid.</code></pre>

<h4>3. Build the microservices JARs</h4>
<p>For Docker to package the services, you need to generate the JAR files. You can do this using the provided script or manually:</p>

<p><strong>Option A: Use the automatic script</strong></p>
<pre><code>cd backend
chmod +x run.sh
./run.sh start</code></pre>

<p><strong>Option B: Build each service manually</strong></p>
<pre><code>mvn clean package -DskipTests -f ms-fleets/pom.xml
mvn clean package -DskipTests -f ms-routes/pom.xml
mvn clean package -DskipTests -f ms-auth/pom.xml
mvn clean package -DskipTests -f gateway/pom.xml</code></pre>

<h4>4. Start all services with Docker Compose</h4>
<p>This command downloads the necessary images (PostgreSQL, OSRM) and builds your microservices.</p>
<pre><code>docker-compose up -d</code></pre>

<h4>5. Verify everything is working</h4>
<ul>
  <li><strong>Gateway:</strong> <code>http://localhost:8080</code></li>
  <li><strong>Swagger UI (e.g. Routes):</strong> <code>http://localhost:8082/swagger-ui.html</code></li>
</ul>

<h4>6. (Optional) Stop the services</h4>
<pre><code>docker-compose down</code></pre>

<h4>7. (Optional) Init Frontend - React / Tailwind</h4>
<pre><code>cd ..
cd frontend
npm i
npm run dev</code></pre>

<ul>
  <li><strong>FrontEnd:</strong> <code>http://localhost:5173</code></li>
</ul>

<hr>

<h2>🐳 Docker Deployment</h2>
<p>All services are dockerized and can be started using Docker Compose, including:</p>
<ul>
  <li><strong>API Gateway (Gateway)</strong></li>
  <li><strong>Authentication Microservice (ms-auth)</strong></li>
  <li><strong>Fleet Microservice (ms-fleets)</strong></li>
  <li><strong>Routes Microservice (ms-routes)</strong></li>
  <li><strong>Packages Microservice (ms-packages)</strong></li>
  <li><strong>Geocoding Microservice (ms-geocoding)</strong></li>
  <li><strong>Frontend (React)</strong> - <em>(coming soon)</em></li>
  <li><strong>Databases (H2/PostgreSQL)</strong> per microservice.</li>
</ul>
<p>This provides a unified, reproducible environment ready for testing or deployment.</p>


<hr>

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


<hr>

<h2>C4 Model</h2>

![](https://raw.githubusercontent.com/LucasCallamullo/fleet-optimizer/refs/heads/main/docs/img/fleet_optimizer_c4.png)

<h2>DER</h2>

![](https://raw.githubusercontent.com/LucasCallamullo/fleet-optimizer/refs/heads/main/docs/img/fleet_optimizer_DER.png)


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


<hr>

<h2 id="contact"> 💻 Contact Lucas Callamullo - Back-End Developer </h2>

| [![GitHub](https://img.shields.io/badge/github-%23121011.svg?&style=for-the-badge&logo=github&logoColor=white)](https://github.com/LucasCallamullo) | [![LinkedIn](https://img.shields.io/badge/linkedin-%230077B5.svg?&style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/lucas-callamullo/) | [![Email](https://img.shields.io/badge/Email-D14836?style=for-the-badge&logo=gmail&logoColor=white)](mailto:lucas.callamullo.dev@gmail.com) |
|:-:|:-:|:-:|

| [![Portfolio](https://img.shields.io/badge/Portfolio-%23000000.svg?style=for-the-badge&logo=react&logoColor=white)](https://lucascallamullo.github.io) | [![Youtube Badge](https://img.shields.io/badge/YouTube%20-%23FF0000.svg?&style=for-the-badge&logo=YouTube&logoColor=white)](https://www.youtube.com/@lucas_clases_python) |
|:-:|:-:|
