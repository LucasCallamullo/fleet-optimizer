## Fleet Optimizer API

Plataforma backend de microservicios para la **gestión y optimización de flotas de vehículos**.

### Capacidades principales:

- **Autenticación y roles** con Keycloak (OAuth2/JWT)
- **Gestión de flota** (ABM de vehículos, estado, categorías)
- **Planificación de viajes** (origen/destino, asignación de vehículos)
- **Optimización de rutas** (cálculo de distancia/tiempo con OSRM, intercambiable por Google Maps)
- **Facturación automática** (presupuestos por viaje basados en distancia × tarifa)

### Arquitectura:
- 6 microservicios independientes
- API Gateway con Spring Cloud Gateway
- Docker Compose para despliegue local
- Frontend en React (dashboard interactivo con mapas)

### Potencial de crecimiento:
- App móvil para conductores
- Panel de reportes y analytics
- Integración con pasarelas de pago reales
