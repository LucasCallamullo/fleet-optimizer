# ================================================================
# DOCKERFILE - ms-geocoding
# ================================================================
# Base image: Java 17 JRE (runtime only, smaller than JDK)
# Uses Eclipse Temurin (OpenJDK distribution)
# ================================================================

FROM eclipse-temurin:17-jre-alpine

# Metadata
LABEL maintainer="Lucas Callamullo"
LABEL description="Geocoding microservice for Fleet Optimization System"

# Application directory
WORKDIR /app

# Copy JAR file
# The JAR is built by Maven: mvn clean package
# It will be placed in the target/ directory
COPY target/*.jar app.jar

# Expose port (must match server.port in application.yml)
EXPOSE 8084

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]