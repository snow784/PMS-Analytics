# ===============================
# Stage 1: Build
# ===============================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Install protoc (Protocol Buffers Compiler)
# RUN apt-get update && apt-get install -y wget unzip \
#     && wget https://github.com/protocolbuffers/protobuf/releases/download/v21.0/protoc-21.0-linux-x86_64.zip \
#     && unzip protoc-21.0-linux-x86_64.zip -d /usr/local/bin/ \
#     && rm protoc-21.0-linux-x86_64.zip

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn -ntp dependency:go-offline

# Copy source code
COPY src ./src

# Run Maven build
RUN mvn -ntp clean package -DskipTests

# ===============================
# Stage 2: Runtime
# ===============================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy built JAR
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8082

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
