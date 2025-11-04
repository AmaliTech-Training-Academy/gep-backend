# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy parent pom from services directory
COPY ../pom.xml ./pom.xml

# Copy service pom
COPY pom.xml ./auth-service/pom.xml

# Download dependencies
RUN mvn -f auth-service/pom.xml dependency:go-offline -B

# Copy source code
COPY src ./auth-service/src

# Build the service
RUN mvn -f auth-service/pom.xml clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install curl for health checks
RUN apk add --no-cache curl

# Copy the built JAR
COPY --from=build /app/auth-service/target/*.jar app.jar

# Expose port
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=8081"]