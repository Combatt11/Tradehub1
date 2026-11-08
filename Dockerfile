# ============================================================
# Stage 1: build the application with Maven + JDK 17
# ============================================================
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /workspace

# Cache dependencies in their own layer: only re-downloaded when pom.xml changes.
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline

# Copy sources and build the executable jar (tests run in CI, not during image build).
COPY src ./src
RUN mvn -B -ntp clean package -DskipTests

# ============================================================
# Stage 2: minimal JRE 17 runtime
# ============================================================
FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

# Run as an unprivileged user.
RUN addgroup -S tradehub && adduser -S tradehub -G tradehub

COPY --from=build /workspace/target/*.jar app.jar

USER tradehub

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
