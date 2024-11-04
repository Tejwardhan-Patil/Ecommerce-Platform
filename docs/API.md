# API Documentation

## Inventory Service

### Get Product Information

**Endpoint**: `GET /api/inventory/{productId}`

**Description**: Retrieves details of a product, including stock levels.

**Response**:

```json
{
  "productId": "12345",
  "name": "Product Name",
  "stockLevel": 100
}
```

### Update Stock Levels

**Endpoint**: `POST /api/inventory/update`

**Description**: Updates the stock level of a product.

**Request**:

```json
{
  "productId": "12345",
  "newStockLevel": 50
}
```

**Response**:

```json
{
  "status": "success",
  "message": "Stock level updated."
}
```

## Order Service

### Place an Order

**Endpoint**: `POST /api/orders/place`

**Description**: Places a new order for a product.

**Request**:

```json
{
  "productId": "12345",
  "quantity": 2,
  "userId": "7890"
}
```

**Response**:

```json
{
  "orderId": "order_98765",
  "status": "placed"
}
```

### Track an Order

**Endpoint**: `GET /api/orders/{orderId}`

**Description**: Fetches the status of an order.

**Response**:

```json
{
  "orderId": "order_98765",
  "status": "shipped",
  "expectedDelivery": "2024-10-25"
}
```

## Payment Service

### Process a Payment

**Endpoint**: `POST /api/payments/process`

**Description**: Processes a payment for an order.

**Request**:

```json
{
  "orderId": "order_98765",
  "paymentMethod": "credit_card",
  "amount": 100.50
}
```

**Response**:

```json
{
  "paymentId": "pay_12345",
  "status": "completed"
}
```
