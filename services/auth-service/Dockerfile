# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy parent pom (needed for inheritance)
COPY pom.xml ./pom.xml

# Copy auth-service pom
COPY auth-service/pom.xml ./auth-service/pom.xml

# Preload dependencies for this module ONLY
RUN mvn -f auth-service/pom.xml dependency:go-offline -B

# Copy the source code
COPY auth-service/src ./auth-service/src

# Build this module
RUN mvn -f auth-service/pom.xml clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install curl
RUN apk add --no-cache curl

# Copy the built artifact from build stage
COPY --from=build /app/auth-service/target/*.jar app.jar

# Expose the port the service runs on
EXPOSE 8081

# Run the application
ENTRYPOINT ["java",  "-jar", "app.jar", "--server.port=8081"]