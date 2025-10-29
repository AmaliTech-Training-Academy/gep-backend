FROM openjdk:17-jre-slim
VOLUME /tmp

# Install curl
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

COPY target/user-service-*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 8080