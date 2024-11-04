# Use the official Python image from the Docker Hub
FROM python:3.9-slim

# Set environment variables for the Python environment
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

# Set the working directory in the container
WORKDIR /app

# Copy the requirements.txt file to the working directory
COPY requirements.txt /app/

# Install Python dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Copy the current directory contents into the container at /app
COPY . /app/

# Expose the port that the service will run on
EXPOSE 8000

# Run the application
CMD ["python", "src/infrastructure/http/api.py"]