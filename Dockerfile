
# --------------------------------------------------
# Stage 1: Build React + Vite frontend
# --------------------------------------------------
FROM node:24-alpine AS frontend-build
WORKDIR /dashboard

COPY examples/web-dashboard/package*.json ./
RUN npm ci

COPY examples/web-dashboard/ ./
RUN npm run build


# --------------------------------------------------
# Stage 2: Build Spring Boot backend
# --------------------------------------------------
FROM eclipse-temurin:21.0.13_9-jdk AS build

WORKDIR /app

COPY . .

# Copy the production React build into Spring Boot static resources
COPY --from=frontend-build /dashboard/dist/ /app/src/main/resources/static/

# Build the executable Spring Boot JAR
RUN chmod +x mvnw && ./mvnw clean package -DskipTests -B

# Verify that the dashboard was packaged
RUN jar tf target/*.jar | grep -q 'BOOT-INF/classes/static/index.html'


# --------------------------------------------------
# Stage 3: Production runtime
# --------------------------------------------------
FROM eclipse-temurin:21.0.13_9-jre-alpine-3.23 AS runtime

# Apply Alpine security fixes and create non-root user
RUN apk upgrade --no-cache && \
  apk add --no-cache \
  'brotli-libs>=1.2.1-r0' \
  'libexpat>=2.10.0-r0' \
  'zlib>=1.4-r0' \
  'musl>=1.2.5-r24' \
  'musl-utils>=1.2.5-r24' \
  'libpng>=1.6.59-r0' && \
  addgroup -S -g 10001 appuser && \
  adduser -S -D -H -u 10001 -G appuser appuser

WORKDIR /app

COPY --from=build --chown=appuser:appuser /app/target/*.jar app.jar

USER appuser

EXPOSE 8080

ENV JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom -Xmx512m -Xms256m"

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
