# =============================================================================
# Reactive Finance Ratpack — Native Docker Image
#
# Stage 1 (builder): Compiles the application to a native binary using GraalVM.
# Stage 2 (runtime): Runs the native binary in a minimal Debian slim image.
# =============================================================================

# ---- Build Stage ---------------------------------------------------------- #
FROM ghcr.io/graalvm/native-image-community:17-ol9 AS builder

WORKDIR /build

# Copy Gradle wrapper first (layer-cached until wrapper changes)
COPY gradle gradle
COPY gradlew .
COPY gradle.properties .
COPY settings.gradle .
COPY build.gradle .
COPY lombok.config .
COPY src src

RUN chmod +x gradlew && \
    ./gradlew nativeCompile --no-daemon -x test

# ---- Runtime Stage -------------------------------------------------------- #
FROM debian:bookworm-slim AS runtime

WORKDIR /app

# Install minimal runtime dependencies
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
      ca-certificates \
      tzdata \
      curl \
    && rm -rf /var/lib/apt/lists/*

# Copy the native binary from the build stage
COPY --from=builder /build/build/native/nativeCompile/reactive-finance-ratpack ./reactive-finance-ratpack

# Copy all environment configuration files into /app/config/
# The active profile is selected at runtime via RATPACK_ENVIRONMENT.
COPY src/main/resources/application-*.yml /app/config/

# --------------------------------------------------------------------------- #
# Environment Variables
#
# RATPACK_ENVIRONMENT   (required) Selects the active config profile.
#                       Corresponds to application-{value}.yml.
#                       Default: docker  (uses application-docker.yml)
#
# CONFIGURATION_PATH    (required internally) Directory containing the active
#                       config file, relative to the working directory.
#                       Default: /config/ → resolves to /app/config/
#
# JDBC__URL             (optional) Overrides jdbc.url from the config file.
#                       Example: jdbc:mariadb://my-host:3306/mydb
#
# JDBC__USERNAME        (optional) Overrides jdbc.username from the config file.
#
# JDBC__PASSWORD        (optional) Overrides jdbc.password from the config file.
#
# SERVER__DEFAULTPORT   (optional) Overrides server.defaultPort (default: 5051).
# --------------------------------------------------------------------------- #
ENV RATPACK_ENVIRONMENT=docker \
    CONFIGURATION_PATH=/config/

# Expose the default server port
EXPOSE 5051

# Health-check using the Ratpack built-in health endpoint
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:5051/_health || exit 1

ENTRYPOINT ["/app/reactive-finance-ratpack"]
