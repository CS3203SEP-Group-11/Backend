# Simple single-stage Dockerfile for Spring Boot application
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Fix permission issue
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose application port
EXPOSE 8081

# Run the application (wildcard expansion works)
ENTRYPOINT sh -c "java -jar /app/target/*.jar"