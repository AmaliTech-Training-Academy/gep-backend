# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy parent pom and install it
COPY pom.xml ./
RUN mvn install -N -B

# Copy auth-service pom and source
COPY auth-service/pom.xml ./auth-service/
COPY auth-service/src ./auth-service/src

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
