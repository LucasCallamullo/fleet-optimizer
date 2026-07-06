# ================================================================
# DOCKERFILE - gateway
# ================================================================

FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="Lucas Callamullo"
LABEL description="API Gateway for Fleet Optimization System"

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]