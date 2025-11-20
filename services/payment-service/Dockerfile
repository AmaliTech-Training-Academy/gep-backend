# build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy files from current context
COPY ../ /app

# Then build event-service
RUN mvn clean package -DskipTests -pl payment-service -am -B

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install curl
RUN apk add --no-cache curl

COPY --from=build /app/payment-service/target/*.jar app.jar

# Expose the port the service runs on
EXPOSE 8088

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=8088"]
