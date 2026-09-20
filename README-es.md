# Fleet Optimizer 2025

El objetivo general del proyecto es implementar una solución backend basada en microservicios para la gestión integral de un sistema de logística de transporte de paquetes. El sistema permite administrar una flota de vehículos, gestionar paquetes y planificar rutas de entrega de forma eficiente, optimizando costos y tiempos mediante el cálculo de distancias.

[![Static Badge](https://img.shields.io/badge/Documentation-EN-blue)](https://github.com/LucasCallamullo/fleet-optimizer/blob/main/README.md) [![Documentation ES](https://img.shields.io/badge/Documentation-ES-green)](https://github.com/LucasCallamullo/fleet-optimizer/blob/main/README-es.md) [![Contact](https://img.shields.io/badge/Contact-FF6C37?style=flat&logo=gmail&logoColor=white)](#contact)


<h2>Índice</h2>
<ul>
  <li><a href="#technology-stack">Stack tecnológico</a></li>
  <li><a href="#quick-start-guide">Guía de inicio rápido</a></li>
  <li><a href="#c4-model">C4 Model</a></li>
  <li><a href="#database-schema-der">DER - Base de Datos</a></li>
  <li><a href="#authentication-flow">Flujo de autenticación</a></li>
  <li><a href="#shipment-creation-flow">Flujo de creación de envíos</a></li>
  <li><a href="#key-design-decisions">Decisiones de diseño</a></li>
  <li><a href="#contact">Contacto</a></li>
</ul>


<h2 id="technology-stack">Tecnologías Stack</h2>

|  | Tech Stack |
| :--- | :--- |
| **Backend** | ![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white) ![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-6DB33F?style=for-the-badge&logo=spring&logoColor=white) |
| **Frontend** | ![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB) ![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=for-the-badge&logo=typescript&logoColor=white) ![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white) |
| **Database** | ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white) ![H2 Database](https://img.shields.io/badge/H2_Database-0040CA?style=for-the-badge&logo=h2&logoColor=white) |
| **Security & Auth** | ![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white) ![OAuth 2.0 / OIDC](https://img.shields.io/badge/OAuth_2.0-EB5424?style=for-the-badge&logo=openid&logoColor=white) ![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=json-web-tokens&logoColor=white) |
| **DevOps & Infra** | ![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white) ![Docker Compose](https://img.shields.io/badge/Docker_Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white) ![Git](https://img.shields.io/badge/Git-F05033?style=for-the-badge&logo=git&logoColor=white) ![Nginx](https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white) |
| **Testing** | ![JUnit 5](https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white) ![Mockito](https://img.shields.io/badge/Mockito-78A641?style=for-the-badge&logo=mockito&logoColor=white) |
| **Docs** | ![Swagger / OpenAPI](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black) |




<h2 id="quick-start-guide">Guía de Inicio Rápido</h2>
<p>Seguí estos pasos para configurar el entorno de desarrollo completo.</p>

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

<h4>2. Configurar variables de entorno</h4>
<p>El proyecto incluye un archivo de ejemplo. Creá tu propio archivo <code>.env</code> a partir de él y ajustá los valores si es necesario.</p>
<pre><code>cp .env.example .env</code></pre>

<blockquote>
  <b>Nota:</b> El archivo <code>.env.example</code> usa placeholders. Para que funcione el flujo de autenticación,
  necesitás crear un tenant gratuito en Auth0 y configurarlo. La guía completa paso a paso
  está disponible en <a href="./docs/AUTH0_SETUP.md"><code>docs/AUTH0_SETUP.md</code></a>.
</blockquote>

<h4>3. 🐳 Levantar todos los servicios con Docker Compose</h4>
<p>Desde la raíz del repositorio, ejecutá:</p>
<pre><code>docker compose up -d --build</code></pre>
<p><strong>Esto descarga las imágenes base (PostgreSQL, nginx, node, Maven)</strong> y construye cada servicio del backend y el frontend. No hace falta compilar los JAR manualmente; cada servicio tiene un Dockerfile multi-stage que compila el código dentro de la imagen.</p>

<h4>4. Verificar que todo funcione</h4>
<ul>
  <li>
    <strong>Frontend:</strong> <code>http://localhost</code>
    (servido por Nginx, redirige las peticiones <code>/api</code> al Gateway)
  </li>
  <li>
    <strong>Gateway (a través de Nginx):</strong> <code>http://localhost/api</code>
    (todas las llamadas a la API pasan por Nginx)
  </li>
</ul>
<p>
  Nginx actúa como único punto de entrada en el puerto 80. Todo el tráfico
  <code>/api</code> se redirige internamente al contenedor del Gateway. El Gateway
  <strong>no está expuesto al host</strong>; solo Nginx puede alcanzarlo.
</p>

<details>
  <summary>Pasos opcionales</summary>

  <h4>(Opcional) Desarrollo / Debugging</h4>
  <p>
    Por defecto, el Gateway solo es accesible a través de Nginx. Si querés
    pegarle a la API directamente (por ejemplo, con Postman o Swagger UI),
    descomentá temporalmente el bloque <code>ports</code> en <code>docker-compose.yml</code>:
  </p>
  <pre><code>gateway-service:
    # ...
    ports:
      - "8080:8080"  # Descomentar solo para debugging local</code></pre>
  <p>Después reiniciá el stack y accedé a:</p>
  <ul>
    <li>Gateway directo: <code>http://localhost:8080</code></li>
    <li>Swagger UI: <code>http://localhost:8080/swagger-ui.html</code></li>
  </ul>
  <p>
    <strong>Nota:</strong> Cuando el Gateway está expuesto directamente, el navegador
    (o Postman) evita Nginx. Aplica la configuración de CORS del Gateway, y headers
    como <code>X-Forwarded-For</code> van a reflejar la dirección del Gateway en vez
    de la IP real del cliente. Esto está bien para debugging, pero
    <strong>nunca</strong> lo uses en producción.
  </p>

  <h4>(Opcional) Correr el backend o el frontend sin Docker</h4>
  <p>
    Igual necesitás las bases de datos corriendo. Podés levantar solo las bases
    de datos con Docker mientras corrés las apps de forma nativa:
  </p>
  <pre><code>docker compose up -d postgres-fleets postgres-routes postgres-packages</code></pre>

  <p>Compilá los JARs manualmente (solo si querés correr un servicio fuera de Docker):</p>
  <pre><code>cd backend
  mvn clean package -DskipTests -f gateway/pom.xml
  mvn clean package -DskipTests -f ms-auth/pom.xml
  mvn clean package -DskipTests -f ms-fleets/pom.xml
  mvn clean package -DskipTests -f ms-packages/pom.xml
  mvn clean package -DskipTests -f ms-geocoding/pom.xml
  mvn clean package -DskipTests -f ms-routes/pom.xml</code></pre>

  <p>Si querés hot reload mientras desarrollás el frontend:</p>
  <strong>Frontend (servidor de desarrollo):</strong> <code>http://localhost:5173</code>
  <pre><code>cd frontend
  npm install
  npm run dev</code></pre>

  <h4>(Opcional) Detener los servicios</h4>
  <pre><code>docker compose down</code></pre>
  <p>Para eliminar también los volúmenes de las bases de datos (esto borra todos los datos):</p>
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
<p>Cada microservicio posee su propia base de datos PostgreSQL. El siguiente diagrama muestra las tablas y las relaciones de ms-fleets, ms-routes y ms-packages.</p>

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
  <p><strong>Notas:</strong></p>
  <ul>
    <li>
      No existe una tabla <code>users</code> local. La identidad del usuario la gestiona
      <strong>Auth0</strong>, y se referencia mediante el claim <code>sub</code>
      (ej: <code>auth0|6aaf...</code>) almacenado en las columnas <code>owner_id</code>.
    </li>
    <li>
      <code>owner_id</code> está <strong>desnormalizado</strong> en
      <code>stores</code>, <code>packages</code> y <code>routes</code> para evitar
      llamadas N+1 entre servicios al filtrar recursos por usuario.
    </li>
    <li>
      <code>legs.origin_*</code> y <code>legs.destination_*</code> almacenan una
      <strong>instantánea</strong> de la dirección al momento del envío, para que las
      rutas históricas sigan siendo precisas aunque la dirección de un local cambie después.
    </li>
  </ul>
</blockquote>



<br></br>
<h2 id="authentication-flow">Flujo de Autenticación (OAuth2 + JWT)</h2>
<h3>Pasos del flujo:</h3>
<ol>
  <li>
    <strong>Inicio de autenticación:</strong>
    El usuario inicia sesión desde el frontend con sus credenciales.
  </li>
  <li>
    <strong>Login en el Gateway:</strong>
    El frontend envía las credenciales al endpoint <code>/api/v1/auth/login</code> del API Gateway.
  </li>
  <li>
    <strong>Validación en Auth0:</strong>
    El Gateway redirige la petición al microservicio <code>ms-auth</code>, que valida las credenciales contra Auth0 y obtiene un token JWT.
  </li>
  <li>
    <strong>Token al frontend:</strong>
    El Gateway devuelve el token JWT al frontend.
  </li>
  <li>
    <strong>Petición con token:</strong>
    El frontend envía el token en el header <code>Authorization: Bearer &lt;token&gt;</code> en cada petición posterior.
  </li>
  <li>
    <strong>Validación y enrutamiento:</strong>
    El Gateway valida el token JWT (firma y expiración), extrae la información del usuario (ID, roles, email)
    de los claims personalizados del JWT (https://fo.dev/roles, https://fo.dev/email) y los inyecta como headers
    (<code>X-User-Id</code>, <code>X-User-Email</code>, <code>X-User-Roles</code>).
  </li>
  <li>
    <strong>Autorización en los microservicios:</strong>
    El microservicio destino recibe el contexto del usuario (vía headers) y usa <code>@PreAuthorize</code> para controlar el acceso a los endpoints según los roles.
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
<h2 id="shipment-creation-flow">Flujo de Creación de Envío (Orquestación Principal del Negocio)</h2>
<p>
  Este diagrama de secuencia ilustra el flujo principal del sistema: la creación de un envío.
  Muestra cómo <code>ms-routes</code> orquesta las llamadas de validación a otros microservicios
  (<code>ms-fleets</code>, <code>ms-packages</code>), calcula las distancias a través de
  <code>ms-geocoding</code>, y persiste la ruta y sus tramos de forma atómica.
</p>

```mermaid
  sequenceDiagram
    autonumber
    actor User as Usuario (Admin | Cliente)
    participant F as Frontend (React)
    participant G as API Gateway
    participant R as ms-routes
    participant P as ms-packages
    participant Fl as ms-fleets
    participant Geo as ms-geocoding
    participant DB_R as DB Rutas
    participant DB_P as DB Paquetes
    participant ORS as OpenRouteService

    Note over User,F: El usuario ya está autenticado (JWT almacenado en el frontend)

    User->>F: Abre la página "Crear Envío"
    F->>G: GET /api/v1/packages (con Bearer token)
    G->>G: Valida JWT, inyecta X-User-Id, X-User-Roles
    G->>P: Reenvía petición + headers de usuario
    P->>DB_P: Obtiene paquetes disponibles del usuario
    DB_P-->>P: Lista de paquetes
    P-->>G: Respuesta con paquetes
    G-->>F: Respuesta con paquetes
    F-->>User: Renderiza la lista de paquetes

    F->>G: GET /api/v1/vehicles?minWeight=&minVolume=
    G->>Fl: Reenvía petición
    Fl-->>G: Vehículos filtrados
    G-->>F: Vehículos filtrados
    F-->>User: Renderiza opciones de vehículos

    User->>F: Selecciona destino (autocompletado de Geocoding)

    User->>F: Selecciona vehículo (filtrado por peso/volumen)

    User->>F: Envía el formulario de envío
    F->>G: POST /api/v1/shipments (packageId, vehicleId, destino)
    G->>G: Valida JWT, inyecta X-User-Id, X-User-Roles
    G->>R: Reenvía petición de creación de envío

    Note over R: Comienza la orquestación

    R->>P: GET /api/v1/packages/{id} (valida que el paquete existe y su estado)
    P-->>R: Detalles del paquete

    R->>Fl: GET /api/v1/vehicles/{id} (valida disponibilidad del vehículo)
    Fl-->>R: Detalles del vehículo

    R->>Fl: POST /api/v1/vehicles/{id}/reserve (opcional: marcar como en uso)
    Fl-->>R: Vehículo reservado

    R->>Geo: POST /api/v1/distance (batch: todos los tramos)
    Geo->>ORS: Solicita matriz de rutas
    ORS-->>Geo: Distancias + tiempos estimados
    Geo-->>R: Distancias + tiempos estimados

    R->>DB_R: Persiste Ruta + Tramos (transacción atómica)
    DB_R-->>R: Ruta guardada

    R->>P: PATCH /api/v1/packages/{id}/status (IN_TRANSIT)
    P->>DB_P: Actualiza estado del paquete
    DB_P-->>P: Actualizado
    P-->>R: Estado actualizado

    R-->>G: Envío creado (ruta + tramos + totales)
    G-->>F: Respuesta con el envío
    F-->>User: Redirige a la página de detalle del envío
```



<br></br>
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
<h2 id="key-design-decisions">Decisiones Clave de Diseño</h2>
<ul>
  <li>
    <strong>Punto de entrada único a través del Gateway:</strong> solo el Gateway está expuesto al host.
    Todos los microservicios y bases de datos viven en una red interna de Docker.
    Esto reduce la superficie de ataque y centraliza las preocupaciones transversales (autenticación, logging, rate limiting).
  </li>
  <li>
    <strong>Base de datos por servicio:</strong> cada microservicio es dueño de su propia base de datos PostgreSQL.
    Sin esquema compartido, sin joins entre servicios. La comunicación es únicamente vía HTTP.
  </li>
  <li>
    <strong>Proveedor de Identidad gestionado (Auth0):</strong> en lugar de auto-hospedar Keycloak,
    el proyecto usa Auth0 para evitar la carga operativa de correr y parchear un IdP.
  </li>
  <li>
    <strong>Orquestación en ms-routes:</strong> el Servicio de Rutas actúa como orquestador
    para la creación de envíos, coordinando la validación con Paquetes y Flota antes de persistir.
  </li>
  <li>
    <strong>Nginx como reverse proxy:</strong> sirve el SPA compilado y redirige el tráfico
    <code>/api</code> al Gateway, manteniendo al navegador en un solo origen (sin problemas de CORS).
  </li>
</ul>

<blockquote>
  <b>Trabajo futuro:</b> rate limiting, observabilidad y CI/CD están en el roadmap
  y se irán agregando a medida que el proyecto evolucione.
</blockquote>

<br></br>
<details>
  <summary>Responsabilidades de los servicios</summary>

  <h2>Arquitectura General</h2>

  <p>El sistema sigue una arquitectura moderna compuesta por múltiples microservicios independientes, cada uno responsable de un dominio específico:</p>

  <h3>🔹 API Gateway</h3>
  <p>Punto de entrada único para todas las aplicaciones frontend o clientes externos. Responsable del enrutamiento y la comunicación hacia cada microservicio, así como de validar los tokens JWT emitidos por Auth0 y propagar el contexto del usuario.</p>

  <h3>🔹 Servicio de Autenticación (ms-auth)</h3>
  <p>Centraliza la gestión de usuarios y la autenticación. Se integra con Auth0 (OAuth2/OpenID Connect) para la emisión y renovación de tokens JWT, y la gestión de roles y permisos.</p>

  <h3>🔹 Servicio de Flota (ms-fleets)</h3>
  <p>Gestiona el catálogo de vehículos. Maneja operaciones CRUD para vehículos y sus categorías, incluyendo capacidades (peso y volumen máximos), costos operativos y estado de disponibilidad.</p>

  <h3>🔹 Servicio de Paquetes (ms-packages)</h3>
  <p>Gestiona el ciclo de vida de los paquetes desde su creación (con peso, volumen y origen) hasta su estado final (CREATED, PROCESSING, READY_FOR_PICKUP, IN_TRANSIT, DELIVERED, etc.). Cada paquete está asociado a un local de origen.</p>

  <h3>🔹 Servicio de Rutas (ms-routes)</h3>
  <p>Orquesta el proceso de creación de envíos. Coordina la validación de paquetes y vehículos con otros microservicios, calcula distancias y tiempos a través del MS de Geocoding, y persiste rutas y sus tramos de forma atómica. Cada paquete en un envío se convierte en un tramo de la ruta.</p>

  <h3>🔹 Frontend (React)</h3>
  <p>Aplicación cliente desarrollada con React que consume la API del Gateway. Proporciona una interfaz de usuario para gestionar paquetes, vehículos, rutas y seguimiento de envíos. Se comunica exclusivamente con el API Gateway, que actúa como intermediario con el resto de los microservicios.</p>

  <h3>🔹 Servicio de Geocoding (ms-geocoding)</h3>
  <p>Microservicio dedicado exclusivamente al cálculo de rutas y distancias basado en coordenadas geográficas (latitud/longitud). Consume la API de <strong>OpenRouteService (ORS)</strong>, un servicio de ruteo que requiere una API key para su uso. Soporta el cálculo de distancias y tiempos estimados para optimizar los costos y la logística del sistema. Implementa un endpoint batch para procesar múltiples ubicaciones en una sola llamada.</p>
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