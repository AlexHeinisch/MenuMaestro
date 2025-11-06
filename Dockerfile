# Stage 1: Build the application
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy everything
COPY . .

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /build/application/target/application*.jar application.jar

EXPOSE 8080

# Command to run the application
CMD ["java", "-jar", "./application.jar"]
