# Use an official OpenJDK runtime as a parent image
FROM openjdk:11-jre-slim

# Set the working directory in the container
WORKDIR /app

# Copy the application's JAR file into the container
COPY target/EventBus-1.0.0.jar EventBus.jar

# Expose the port that the application will run on
EXPOSE 8080

# Environment variables (RabbitMQ, Kafka)
ENV RABBITMQ_HOST=rabbitmq
ENV KAFKA_BROKER=kafka:9092

# Run the JAR file
ENTRYPOINT ["java", "-jar", "EventBus.jar"]