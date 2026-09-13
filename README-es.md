# Fleet Optimizer 2025

El objetivo general del proyecto es implementar una solución backend basada en microservicios para la gestión integral de un sistema de logística de transporte de paquetes. El sistema permite administrar una flota de vehículos, gestionar paquetes y planificar rutas de entrega de forma eficiente, optimizando costos y tiempos mediante el cálculo de distancias.

[![Static Badge](https://img.shields.io/badge/Documentation-EN-blue)](https://github.com/LucasCallamullo/fleet-optimizer/blob/main/README.md) [![Documentation ES](https://img.shields.io/badge/Documentation-ES-green)](https://github.com/LucasCallamullo/fleet-optimizer/blob/main/README-es.md) [![Contact](https://img.shields.io/badge/Contact-FF6C37?style=flat&logo=gmail&logoColor=white)](#contact)


## Tecnologías Stack

|  | Tech Stack |
| :--- | :--- |
| **Backend** | ![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white) ![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-6DB33F?style=for-the-badge&logo=spring&logoColor=white) |
| **Frontend** | ![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB) ![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=for-the-badge&logo=typescript&logoColor=white) ![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white) |
| **Persistence & Data** | ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white) ![H2 Database](https://img.shields.io/badge/H2_Database-0040CA?style=for-the-badge&logo=h2&logoColor=white) |
| **Security & Auth** | ![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white) ![Keycloak](https://img.shields.io/badge/Keycloak-0085CA?style=for-the-badge&logo=keycloak&logoColor=white) ![OAuth 2.0 / OIDC](https://img.shields.io/badge/OAuth_2.0-EB5424?style=for-the-badge&logo=openid&logoColor=white) ![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=json-web-tokens&logoColor=white) |
| **DevOps & Infra** | ![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white) ![Docker Compose](https://img.shields.io/badge/Docker_Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white) ![Git](https://img.shields.io/badge/Git-F05033?style=for-the-badge&logo=git&logoColor=white) ![Swagger / OpenAPI](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black) |
| **Testing** | ![JUnit 5](https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white) ![Mockito](https://img.shields.io/badge/Mockito-78A641?style=for-the-badge&logo=mockito&logoColor=white) |


<hr>

<h2>Guía de inicio rápido</h2>
<p>Seguí estos pasos para levantar el entorno de desarrollo completo.</p>

<h3>Requisitos previos</h3>
<ul>
  <li>Docker y Docker Compose instalados.</li>
  <li>Git para clonar el repositorio.</li>
  <li>(Opcional) Java 17 y Maven para correr los servicios sin Docker.</li>
  <li>(Opcional) Node 20 y npm para correr el frontend sin Docker.</li>
</ul>

<h3>Paso a paso</h3>

<h4>1. Clonar el repositorio</h4>
<pre><code>git clone https://github.com/LucasCallamullo/fleet-optimizer.git
cd fleet-optimizer</code></pre>

<h4>2. Configurar las variables de entorno</h4>
<p>El proyecto incluye un archivo de ejemplo. Creá tu propio archivo <code>.env</code> a partir de él y ajustá los valores si hace falta.</p>
<pre><code>cp .env.example .env
# No hace falta editar el archivo .env creado, las claves de Keycloak son válidas.</code></pre>

<h4>3. 🐳 Levantar todos los servicios con Docker Compose</h4>
<p>Desde la raíz del repositorio, ejecutá:</p>
<pre><code>docker compose up -d --build</code></pre>
<p><strong>Esto descarga las imágenes base (PostgreSQL, nginx, node, Maven)</strong> y construye cada servicio del backend y el frontend. No hace falta compilar los JAR a mano: cada servicio tiene un Dockerfile multi-stage que compila el código dentro de la imagen.</p>

<h4>4. Verificar que todo funcione</h4>
<ul>
  <li><strong>Frontend:</strong> <code>http://localhost</code></li>
  <li><strong>Gateway:</strong> <code>http://localhost:8080</code></li>
</ul>
<p>El frontend lo sirve nginx en el puerto 80 y redirige las peticiones <code>/api</code> al Gateway. El Gateway es el único punto de entrada al backend; los microservicios y las bases de datos son internos y no están expuestos al host.</p>

<h4>5. (Opcional) Detener los servicios</h4>
<pre><code>docker compose down</code></pre>
<p>Para eliminar también los volúmenes de las bases de datos (esto borra todos los datos):</p>
<pre><code>docker compose down -v</code></pre>

<h4>6. (Opcional) Correr el backend o el frontend sin Docker</h4>

<p>Compilar los JAR a mano (solo si querés correr un servicio fuera de Docker)</p>
<pre><code>cd backend
mvn clean package -DskipTests -f gateway/pom.xml
mvn clean package -DskipTests -f ms-auth/pom.xml
mvn clean package -DskipTests -f ms-fleets/pom.xml
mvn clean package -DskipTests -f ms-packages/pom.xml
mvn clean package -DskipTests -f ms-geocoding/pom.xml
mvn clean package -DskipTests -f ms-routes/pom.xml
</code></pre>

<p>Si querés hot reload mientras desarrollás el frontend:</p>
<strong>Frontend (dev server):</strong> <code>http://localhost:5173</code>
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
<p>Cada microservicio posee su propia base de datos PostgreSQL. El siguiente diagrama muestra las tablas y las relaciones de ms-fleets, ms-routes y ms-packages.</p>

![](https://raw.githubusercontent.com/LucasCallamullo/fleet-optimizer/refs/heads/main/docs/img/fleet_optimizer_DER.png)


<hr>

<details>
  <summary>Service responsibilities</summary>

<h2>Arquitectura General</h2>

<p>El sistema sigue una arquitectura moderna compuesta por múltiples microservicios independientes, cada uno responsable de un dominio específico:</p>

<h3>🔹 API Gateway</h3>
<p>Punto de entrada único para todas las aplicaciones frontend o clientes externos. Encargado del enrutamiento y la comunicación hacia cada microservicio, además de validar los tokens JWT emitidos por Keycloak y propagar el contexto de usuario.</p>

<h3>🔹 Servicio de Autenticación (ms-auth)</h3>
<p>Centraliza la gestión de usuarios y la autenticación. Se integra con Keycloak (OAuth2/OpenID Connect) para la emisión y refresco de tokens JWT, y la gestión de roles y permisos.</p>

<h3>🔹 Servicio de Flotas (ms-fleets)</h3>
<p>Gestiona el catálogo de vehículos. Administra el CRUD de vehículos y sus categorías, incluyendo sus capacidades (peso y volumen máximo), costos operativos y estado de disponibilidad.</p>

<h3>🔹 Servicio de Paquetes (ms-packages)</h3>
<p>Administra el ciclo de vida de los paquetes, desde su creación (con peso, volumen y origen) hasta su estado final (CREADO, EN PROCESO, LISTO PARA RETIRAR, EN TRÁNSITO, ENTREGADO, etc.). Cada paquete está asociado a una tienda de origen.</p>

<h3>🔹 Servicio de Rutas (ms-routes)</h3>
<p>Orquesta el proceso de creación de envíos. Coordina la validación de paquetes y vehículos con otros microservicios, calcula distancias y tiempos a través del MS de Geocoding, y persiste las rutas y sus tramos (legs) de forma atómica. Cada paquete en un envío se convierte en un tramo de la ruta.</p>

<h3>🔹 Frontend (React)</h3>
<p>Aplicación cliente desarrollada en React que consume la API del Gateway. Proporciona una interfaz de usuario para la gestión de paquetes, vehículos, rutas y seguimiento de envíos. Se comunica exclusivamente con el API Gateway, que actúa como intermediario con el resto de los microservicios.</p>

<h3>🔹 Servicio de Geocodificación (ms-geocoding)</h3>
<p>Microservicio dedicado al cálculo de rutas y distancias en base a coordenadas geográficas (latitud/longitud). Consume la API de <strong>OpenRouteService (ORS)</strong>, un servicio de enrutamiento que requiere una clave de API para su uso. Soporta el cálculo de distancias y tiempos estimados para optimizar los costos y la logística del sistema. Implementa un endpoint batch para procesar múltiples ubicaciones en una sola llamada.</p>

</details>


<hr>


<h2>Flujo de Autenticación (OAuth2 + JWT)</h2>
<h3>Pasos del flujo:</h3>
<ol>
  <li>
    <strong>Inicio de Autenticación:</strong>
    El usuario inicia sesión desde el frontend con sus credenciales.
  </li>
  <li>
    <strong>Login en Gateway:</strong>
    El frontend envía las credenciales al endpoint <code>/api/v1/auth/login</code> del API Gateway.
  </li>
  <li>
    <strong>Validación en Keycloak:</strong>
    El Gateway enruta la petición al microservicio <code>ms-auth</code>, que valida las credenciales contra Keycloak y obtiene un token JWT.
  </li>
  <li>
    <strong>Token al Frontend:</strong>
    El Gateway devuelve el token JWT al frontend.
  </li>
  <li>
    <strong>Petición con Token:</strong>
    El frontend envía el token en el header <code>Authorization: Bearer &lt;token&gt;</code> en cada petición subsiguiente.
  </li>
  <li>
    <strong>Validación y Enrutamiento:</strong>
    El Gateway valida el token JWT (firma y expiración), extrae la información del usuario (ID, roles) del <code>realm_access</code> y la inyecta como headers (<code>X-User-Id</code>, <code>X-User-Roles</code>).
  </li>
  <li>
    <strong>Autorización en Microservicios:</strong>
    El microservicio destino recibe el contexto del usuario (a través de los headers) y utiliza <code>@PreAuthorize</code> para controlar el acceso a los endpoints según los roles.
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




<h2>🖥️ Capturas del Frontend</h2>
<p>La aplicación ofrece una interfaz intuitiva para gestionar todos los aspectos del sistema logístico. A continuación, se muestran algunas de las pantallas principales.</p>

<table>
  <tr>
    <td align="center">
      <strong>Dashboard</strong>
    </td>
    <td align="center">
      <strong>Gestión de Flota</strong>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://raw.githubusercontent.com/LucasCallamullo/fleet-optimizer/refs/heads/main/docs/img/front1.png" alt="Dashboard" width="400"/>
      <br>
      <em>Dashboard con información del JWT y accesos rápidos</em>
    </td>
    <td align="center">
      <img src="https://raw.githubusercontent.com/LucasCallamullo/fleet-optimizer/refs/heads/main/docs/img/front2.png" alt="Gestión de Flota" width="400"/>
      <br>
      <em>Tabla de vehículos con capacidades y estado</em>
    </td>
  </tr>


  <tr>
    <td align="center">
      <strong>Detalle de Paquete</strong>
    </td>
    <td align="center">
      <strong>Selección de Vehículo</strong>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://raw.githubusercontent.com/LucasCallamullo/fleet-optimizer/refs/heads/main/docs/img/front3.png" alt="Detalle de Paquete" width="400"/>
      <br>
      <em>Detalle del paquete con selector de destino</em>
    </td>
    <td align="center">
      <img src="https://raw.githubusercontent.com/LucasCallamullo/fleet-optimizer/refs/heads/main/docs/img/front4.png" alt="Selección de Vehículo" width="400"/>
      <br>
      <em>Vehículos disponibles filtrados por capacidad</em>
    </td>
  </tr>


  <tr>
    <td align="center">
      <strong>C4 Model</strong>
    </td>



  </tr>
  <tr>
    <td align="center">
      <img src="https://raw.githubusercontent.com/LucasCallamullo/fleet-optimizer/refs/heads/main/docs/img/fleet_optimizer_c4.png" alt="C4 Model" width="400"/>
      <br>
      <em>C4 con Draw.io Informal</em>
    </td>




  </tr>

</table>


<hr>

<h2 id="contact"> 💻 Contacto Lucas Callamullo - Back-End Developer </h2>

| [![GitHub](https://img.shields.io/badge/github-%23121011.svg?&style=for-the-badge&logo=github&logoColor=white)](https://github.com/LucasCallamullo) | [![LinkedIn](https://img.shields.io/badge/linkedin-%230077B5.svg?&style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/lucas-callamullo/) | [![Email](https://img.shields.io/badge/Email-D14836?style=for-the-badge&logo=gmail&logoColor=white)](mailto:lucas.callamullo.dev@gmail.com) |
|:-:|:-:|:-:|

| [![Portfolio](https://img.shields.io/badge/Portfolio-%23000000.svg?style=for-the-badge&logo=react&logoColor=white)](https://lucascallamullo.github.io) | [![Youtube Badge](https://img.shields.io/badge/YouTube%20-%23FF0000.svg?&style=for-the-badge&logo=YouTube&logoColor=white)](https://www.youtube.com/@lucas_clases_python) |
|:-:|:-:|