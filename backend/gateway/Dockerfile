# ================================================================
# DOCKERFILE - gateway (Multi-stage build)
# ================================================================
# 
# STAGE 1: BUILD - Compiles the application inside the container
# Uses Maven + JDK to build the JAR
# 
# STAGE 2: RUNTIME - Runs the JAR with only JRE
# Smaller image, no build tools included
# ================================================================

# ================================================================
# STAGE 1: BUILD
# ================================================================
FROM maven:3.9-eclipse-temurin-17-alpine AS build

# Set working directory inside container
WORKDIR /app

# Step 1: Copy pom.xml first (for dependency caching)
COPY pom.xml .
RUN mvn dependency:go-offline

# Step 2: Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# ================================================================
# STAGE 2: RUNTIME
# ================================================================
FROM eclipse-temurin:17-jre-alpine

# Metadata
LABEL maintainer="Lucas Callamullo"
LABEL description="API Gateway for Fleet Optimization System"

# Application directory
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Health check (optional)
# HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
#  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]