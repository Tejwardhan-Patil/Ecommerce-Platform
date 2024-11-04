# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml and download the dependencies
COPY pom.xml ./
RUN ./mvnw dependency:go-offline

# Copy the source code into the container
COPY src ./src

# Package the application
RUN ./mvnw package -DskipTests

# Expose the port the service will run on
EXPOSE 8080

# Define the entry point for the container
ENTRYPOINT ["java", "-jar", "target/OrderService.jar"]