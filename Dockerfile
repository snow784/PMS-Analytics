# ===============================
# Stage 1: Build
# ===============================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml and download dependencies (cache layer)
COPY pom.xml .
RUN mvn -ntp dependency:go-offline

# Copy the source code
COPY src ./src

# Build the application
RUN mvn -ntp clean package -DskipTests

# ===============================
# Stage 2: Runtime
# ===============================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Non-root user (best practice)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy built JAR
COPY --from=build /app/target/*.jar app.jar

# Expose correct port
EXPOSE 8082

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

# JVM optimization
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Run app
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
