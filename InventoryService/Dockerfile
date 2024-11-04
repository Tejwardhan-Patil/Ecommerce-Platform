# Use an official OpenJDK image as the base image
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven build file to the container
COPY pom.xml .

# Download the project dependencies
RUN apt-get update && \
    apt-get install -y maven && \
    mvn dependency:resolve

# Copy the entire project source to the working directory
COPY src /app/src

# Build the application using Maven
RUN mvn clean install

# Expose the port that the InventoryService will run on
EXPOSE 8080

# Run the InventoryService application
ENTRYPOINT ["java", "-jar", "target/inventory-service.jar"]