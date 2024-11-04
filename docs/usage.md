# Platform Usage Guide

This document provides step-by-step instructions on how to set up and use the e-commerce platform.

## Prerequisites

Before running the platform, ensure the following are installed:

- Docker
- Kubernetes (if deploying to a cluster)
- Node.js (for frontend development)
- Python 3.x (for the RecommendationService)
- Java 11+ (for microservices)

## Setup

### Step 1: Clone the Repository

```bash
git clone https://github.com/repo.git
cd ecommerce-platform
```

### Step 2: Start the Microservices

Using Docker Compose, all services can be started in one command:

```bash
docker-compose up --build
```

Alternatively, you can build and start each microservice manually using the provided Dockerfiles in their respective directories (e.g., `InventoryService`, `OrderService`).

### Step 3: Access the Frontend

Once the services are running, the frontend can be accessed via:

```bash
cd frontend
npm install
npm start
```

Navigate to `http://localhost:3000` in your browser.

### Step 4: Running the RecommendationService

For the **RecommendationService**, additional setup is required for Python:

```bash
cd RecommendationService
pip install -r requirements.txt
python src/infrastructure/http/api.py
```

The API will be available at `http://localhost:8000`.

### Step 5: Accessing the Admin Dashboard

For inventory and order management, navigate to the admin dashboard via:

```bash
http://localhost:8080/admin
```

## Interacting with APIs

You can interact with the platform APIs using Postman or cURL. Below are some endpoints:

- **Inventory Service**:  
  `GET /api/inventory/{productId}`  
  `POST /api/inventory/update`

- **Order Service**:  
  `POST /api/orders/place`  
  `GET /api/orders/{orderId}`

## Tests

To run unit tests:

- **Java Microservices**:

  ```bash
  mvn test
  ```

- **RecommendationService**:
  
  ```bash
  python -m unittest discover tests
  ```
