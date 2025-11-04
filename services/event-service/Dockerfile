# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy parent pom (needed for inheritance)
COPY pom.xml ./pom.xml

# Copy event-service pom
COPY event-service/pom.xml ./event-service/pom.xml

# Preload dependencies for this module ONLY
RUN mvn -f event-service/pom.xml dependency:go-offline -B

# Copy the source code
COPY event-service/src ./event-service/src

# Build this module
RUN mvn -f event-service/pom.xml clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install curl
RUN apk add --no-cache curl

COPY --from=build /app/event-service/target/*.jar app.jar

# Expose the port the service runs on
EXPOSE 8082

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=8082"]
