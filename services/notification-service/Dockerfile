# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy parent pom (needed for inheritance)
COPY pom.xml ./pom.xml

# Copy notification-service pom
COPY notification-service/pom.xml ./notification-service/pom.xml

# Preload dependencies for this module ONLY
RUN mvn -f notification-service/pom.xml dependency:go-offline -B

# Copy the source code
COPY notification-service/src ./notification-service/src

# Build this module
RUN mvn -f notification-service/pom.xml clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install curl
RUN apk add --no-cache curl

COPY --from=build /app/notification-service/target/*.jar app.jar

# Expose the port the service runs on
EXPOSE 8085

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=8085"]
