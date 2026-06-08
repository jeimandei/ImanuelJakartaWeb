# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM docker.io/library/maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /workspace

# Copy parent POM and all module POMs first — enables layer caching for deps
COPY pom.xml .
COPY church-common/pom.xml                church-common/pom.xml
COPY church-config-server/pom.xml         church-config-server/pom.xml
COPY church-auth-service/pom.xml          church-auth-service/pom.xml
COPY church-user-service/pom.xml          church-user-service/pom.xml
COPY church-cms-service/pom.xml           church-cms-service/pom.xml
COPY church-event-service/pom.xml         church-event-service/pom.xml
COPY church-media-service/pom.xml         church-media-service/pom.xml
COPY church-interaction-service/pom.xml   church-interaction-service/pom.xml
COPY church-audit-service/pom.xml         church-audit-service/pom.xml
COPY church-gateway-service/pom.xml       church-gateway-service/pom.xml
COPY church-api-gateway/pom.xml           church-api-gateway/pom.xml

COPY maven-settings.xml .

RUN --mount=type=cache,target=/root/.m2,id=maven-m2 \
    mvn dependency:go-offline -B -s maven-settings.xml

# Copy full source and build only the requested service (and its dependencies)
COPY . .
ARG SERVICE_NAME
RUN --mount=type=cache,target=/root/.m2,id=maven-m2 \
    mvn package -pl ${SERVICE_NAME} -am -DskipTests -B -s maven-settings.xml

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM docker.io/library/eclipse-temurin:21-jre-alpine

ARG SERVICE_NAME
ARG SERVICE_PORT=8080

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --from=build /workspace/${SERVICE_NAME}/target/*.jar app.jar

EXPOSE ${SERVICE_PORT}

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
