# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/copro-connect-1.0.0.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs

# Expose port
EXPOSE 4000

# Environment variables (can be overridden)
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=4000

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
