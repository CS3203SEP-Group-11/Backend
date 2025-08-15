FROM eclipse-temurin:17-jre-alpine

ARG JAR_FILE=target/*.jar

COPY ./target/auth-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "/app.jar"]