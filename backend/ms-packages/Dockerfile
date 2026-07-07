# ================================================================
# DOCKERFILE - ms-fleets
# ================================================================

FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="Lucas Callamullo"
LABEL description="Packages microservice for Fleet Optimization System"

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]