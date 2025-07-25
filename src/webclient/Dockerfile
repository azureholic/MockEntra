# Multi-stage build for webclient
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /app

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Copy parent pom.xml for dependency management
COPY pom.xml .

# Copy webclient pom.xml
COPY src/webclient/pom.xml src/webclient/

# Download dependencies (run from webclient directory)
WORKDIR /app/src/webclient
RUN mvn dependency:go-offline -B

# Copy webclient source code
COPY src/webclient/src src

# Build the application (Spring Boot plugin will automatically repackage)
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy the built jar (look for the Spring Boot executable JAR)
COPY --from=build /app/src/webclient/target/*-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8082

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8082/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
