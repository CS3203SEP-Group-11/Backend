# Multi-stage build for Spring Boot application
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml and pre-fetch dependencies (use mvn from base image)
COPY pom.xml .
RUN mvn -B -e -Dmaven.test.skip=true dependency:go-offline

# Copy source code and build
COPY src ./src
RUN mvn -q clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy only the re-packaged Spring Boot jar (avoid copying the *.jar.original)
COPY --from=build /app/target/*-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]