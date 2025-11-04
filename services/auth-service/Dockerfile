# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy service pom
COPY pom.xml ./pom.xml

# Preload dependencies
RUN mvn dependency:go-offline -B

# Copy the source code
COPY src ./src

# Build this module
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install curl
RUN apk add --no-cache curl

# Copy the built artifact from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port the service runs on
EXPOSE 8081

# Run the application
ENTRYPOINT ["java",  "-jar", "app.jar", "--server.port=8081"]