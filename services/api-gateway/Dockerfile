FROM openjdk:17-jre-slim
VOLUME /tmp
COPY target/api-gateway-*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 8080