FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="Lucas Callamullo"
LABEL description="Authentication service for Fleet Optimization System"

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]