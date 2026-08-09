# Fleet Optimizer 2025

El objetivo general del proyecto es implementar una solución backend basada en microservicios para la gestión integral de un sistema de logística de transporte de paquetes. El sistema permite administrar una flota de vehículos, gestionar paquetes y planificar rutas de entrega de forma eficiente, optimizando costos y tiempos mediante el cálculo de distancias.

[![Static Badge](https://img.shields.io/badge/Documentation-EN-blue)](https://github.com/LucasCallamullo/fleet-optimizer/blob/main/README.md) [![Documentation ES](https://img.shields.io/badge/Documentation-ES-green)](https://github.com/LucasCallamullo/fleet-optimizer/blob/main/README-es.md) [![Contact](https://img.shields.io/badge/Contact-FF6C37?style=for-the-badge&logo=gmail&logoColor=white)](#contact)


## Tecnologías Stack

### Backend
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-6DB33F?style=for-the-badge&logo=spring&logoColor=white)

### Frontend
![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black)
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white)

### Base de Datos & ORM
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![H2](https://img.shields.io/badge/H2_Database-0040CA?style=for-the-badge&logo=h2&logoColor=white)
![JPA](https://img.shields.io/badge/JPA-Hibernate-59666C?style=for-the-badge&logo=hibernate&logoColor=white)

### Seguridad
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

<h2>Guía de Inicio Rápido</h2>
<p>Sigue estos pasos para levantar el entorno de desarrollo completo.</p>

<h3>Prerrequisitos</h3>
<ul>
  <li>Docker y Docker Compose instalados.</li>
  <li>Git para clonar el repositorio.</li>
  <li>(Opcional) Java 17 y Maven para ejecutar los servicios sin Docker.</li>
</ul>

<h3>Paso a paso</h3>

<h4>1. Clonar el repositorio</h4>
<pre><code>git clone https://github.com/LucasCallamullo/fleet-optimizer.git
cd fleet-optimizer</code></pre>

<h4>2. Configurar variables de entorno</h4>
<p>El proyecto incluye un archivo de ejemplo. Crea tu propio archivo <code>.env</code> a partir de él y ajusta los valores si es necesario.</p>
<pre><code>cp .env.example .env
# No es necesario Editar el archivo .env creado, las claves de keycloack son válidas.</code></pre>

<h4>3. Construir los JARs de los microservicios</h4>
<p>Para que Docker pueda empaquetar los servicios, necesitas generar los archivos JAR. Puedes hacerlo con el script proporcionado o manualmente:</p>

<p><strong>Opción A: Usar el script automático</strong></p>
<pre><code>./build-and-run.sh</code></pre>

<p><strong>Opción B: Construir manualmente cada servicio</strong></p>
<pre><code>mvn clean package -DskipTests -f ms-fleets/pom.xml
mvn clean package -DskipTests -f ms-routes/pom.xml
mvn clean package -DskipTests -f ms-auth/pom.xml
mvn clean package -DskipTests -f gateway/pom.xml</code></pre>

<h4>4. Levantar todos los servicios con Docker Compose</h4>
<p>Este comando descarga las imágenes necesarias (PostgreSQL, OSRM) y construye las de tus microservicios.</p>
<pre><code>docker-compose up -d</code></pre>

<h4>5. Verificar que todo esté funcionando</h4>
<ul>
  <li><strong>Gateway:</strong> <code>http://localhost:8080</code></li>
  <li><strong>Swagger UI (ej. Routes):</strong> <code>http://localhost:8082/swagger-ui.html</code></li>
</ul>

<h4>6. (Opcional) Detener los servicios</h4>
<pre><code>docker-compose down</code></pre>


<hr>

<h2>🐳 Despliegue con Docker</h2>
<p>Todos los servicios están dockerizados y pueden levantarse mediante Docker Compose, incluyendo:</p>

<ul>
  <li><strong>API Gateway (Gateway)</strong></li>
  <li><strong>Microservicio de Autenticación (ms-auth)</strong></li>
  <li><strong>Microservicio de Flotas (ms-fleets)</strong></li>
  <li><strong>Microservicio de Rutas (ms-routes)</strong></li>
  <li><strong>Microservicio de Paquetes (ms-packages)</strong></li>
  <li><strong>Microservicio de Geocodificación (ms-geocoding)</strong></li>
  <li><strong>Frontend (React)</strong> - <em>(coming soon)</em></li>
  <li><strong>Bases de datos (H2/PostgreSQL)</strong> por microservicio.</li>
</ul>
<p>Esto permite un entorno unificado, reproducible y listo para pruebas o despliegue.</p>


<hr>

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


<hr>

<h2>C4 Model</h2>

![](https://raw.githubusercontent.com/LucasCallamullo/fleet-optimizer/refs/heads/main/docs/img/fleet_optimizer_c4.png)

<h2>DER</h2>

![](https://raw.githubusercontent.com/LucasCallamullo/fleet-optimizer/refs/heads/main/docs/img/fleet_optimizer_DER.png)


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


<hr>

<h2 id="contact"> 💻 Contacto Lucas Callamullo - Back-End Developer </h2>

| [![GitHub](https://img.shields.io/badge/github-%23121011.svg?&style=for-the-badge&logo=github&logoColor=white)](https://github.com/LucasCallamullo) | [![LinkedIn](https://img.shields.io/badge/linkedin-%230077B5.svg?&style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/lucas-callamullo/) | [![Email](https://img.shields.io/badge/Email-D14836?style=for-the-badge&logo=gmail&logoColor=white)](mailto:lucas.callamullo.dev@gmail.com) |
|:-:|:-:|:-:|

| [![Portfolio](https://img.shields.io/badge/Portfolio-%23000000.svg?style=for-the-badge&logo=react&logoColor=white)](https://lucascallamullo.github.io) | [![Youtube Badge](https://img.shields.io/badge/YouTube%20-%23FF0000.svg?&style=for-the-badge&logo=YouTube&logoColor=white)](https://www.youtube.com/@lucas_clases_python) |
|:-:|:-:|