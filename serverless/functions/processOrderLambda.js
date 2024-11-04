const AWS = require('aws-sdk');
const axios = require('axios');

// Initialize AWS SDK
const sns = new AWS.SNS();
const sqs = new AWS.SQS();
const dynamoDB = new AWS.DynamoDB.DocumentClient();

// Service endpoints
const ORDER_SERVICE_URL = 'https://api.website.com/orders';
const PAYMENT_SERVICE_URL = 'https://api.website.com/payments';
const INVENTORY_SERVICE_URL = 'https://api.website.com/inventory';
const NOTIFICATION_SERVICE_URL = 'https://api.website.com/notifications';

exports.handler = async (event) => {
  try {
    const orderDetails = JSON.parse(event.body);
    console.log('Processing order:', orderDetails);

    // Validate order
    await validateOrder(orderDetails);

    // Reserve Inventory
    await reserveInventory(orderDetails);

    // Process Payment
    const paymentStatus = await processPayment(orderDetails);
    if (!paymentStatus.success) {
      throw new Error('Payment failed');
    }

    // Save Order to Database
    await saveOrderToDB(orderDetails);

    // Publish Order Event
    await publishOrderEvent(orderDetails);

    // Send Confirmation Notification
    await sendNotification(orderDetails);

    return {
      statusCode: 200,
      body: JSON.stringify({
        message: 'Order processed successfully',
      }),
    };

  } catch (error) {
    console.error('Error processing order:', error);

    // Handle errors and send failure notifications
    await handleError(event, error);

    return {
      statusCode: 500,
      body: JSON.stringify({
        message: 'Order processing failed',
      }),
    };
  }
};

async function validateOrder(order) {
  if (!order || !order.items || !order.userId) {
    throw new Error('Invalid order details');
  }
  console.log('Order validated:', order.orderId);
}

async function reserveInventory(order) {
  try {
    const inventoryResponse = await axios.post(`${INVENTORY_SERVICE_URL}/reserve`, {
      orderId: order.orderId,
      items: order.items,
    });
    console.log('Inventory reserved:', inventoryResponse.data);
  } catch (error) {
    throw new Error('Failed to reserve inventory');
  }
}

async function processPayment(order) {
  try {
    const paymentResponse = await axios.post(`${PAYMENT_SERVICE_URL}/process`, {
      orderId: order.orderId,
      amount: order.totalAmount,
      paymentMethod: order.paymentMethod,
    });
    console.log('Payment processed:', paymentResponse.data);
    return paymentResponse.data;
  } catch (error) {
    throw new Error('Payment processing failed');
  }
}

async function saveOrderToDB(order) {
  const params = {
    TableName: 'Orders',
    Item: {
      orderId: order.orderId,
      userId: order.userId,
      items: order.items,
      totalAmount: order.totalAmount,
      paymentStatus: 'Paid',
      orderStatus: 'Processed',
      createdAt: new Date().toISOString(),
    },
  };
  await dynamoDB.put(params).promise();
  console.log('Order saved to database:', order.orderId);
}

async function publishOrderEvent(order) {
  const params = {
    Message: JSON.stringify({
      orderId: order.orderId,
      userId: order.userId,
      orderStatus: 'Processed',
    }),
    TopicArn: process.env.ORDER_TOPIC_ARN,
  };
  await sns.publish(params).promise();
  console.log('Order event published to SNS:', order.orderId);
}

async function sendNotification(order) {
  try {
    await axios.post(`${NOTIFICATION_SERVICE_URL}/send`, {
      userId: order.userId,
      message: `Your order ${order.orderId} has been successfully processed.`,
    });
    console.log('Notification sent for order:', order.orderId);
  } catch (error) {
    console.error('Failed to send notification:', error);
  }
}

async function handleError(event, error) {
  const order = JSON.parse(event.body);

  // Update the order status in DynamoDB
  const params = {
    TableName: 'Orders',
    Key: { orderId: order.orderId },
    UpdateExpression: 'set orderStatus = :status',
    ExpressionAttributeValues: {
      ':status': 'Failed',
    },
  };
  await dynamoDB.update(params).promise();
  console.log('Order status updated to "Failed":', order.orderId);

  // Send failure notification
  try {
    await axios.post(`${NOTIFICATION_SERVICE_URL}/send`, {
      userId: order.userId,
      message: `Your order ${order.orderId} failed to process.`,
    });
    console.log('Failure notification sent for order:', order.orderId);
  } catch (notificationError) {
    console.error('Failed to send failure notification:', notificationError);
  }
}