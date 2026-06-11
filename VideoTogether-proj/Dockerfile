# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
# Download dependencies first to utilize Docker layer caching
RUN mvn dependency:go-offline -B
COPY src ./src
# Build the application skipping tests to speed up the process
RUN mvn clean package -DskipTests -B

# Stage 2: Create the final lightweight image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Copy the built jar file from the previous stage
COPY --from=build /app/target/*.jar app.jar
# Expose the default port
EXPOSE 8080
# Run the application with the prod profile
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
