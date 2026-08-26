# ================================================================
# DOCKERFILE - ms-packages (Multi-stage build)
# ================================================================
# 
# STAGE 1: BUILD - Compiles the application inside the container
# STAGE 2: RUNTIME - Runs the JAR with only JRE
# ================================================================

# ================================================================
# STAGE 1: BUILD
# ================================================================
FROM maven:3.9-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Dependency caching
COPY pom.xml .
RUN mvn dependency:go-offline

# Build the application
COPY src ./src
RUN mvn clean package -DskipTests

# ================================================================
# STAGE 2: RUNTIME
# ================================================================
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="Lucas Callamullo"
LABEL description="Packages microservice for Fleet Optimization System"

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8084

# HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
#  CMD wget --no-verbose --tries=1 --spider http://localhost:8084/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]