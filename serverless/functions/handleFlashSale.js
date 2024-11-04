const AWS = require('aws-sdk');
const dynamoDb = new AWS.DynamoDB.DocumentClient();
const sns = new AWS.SNS();
const lambda = new AWS.Lambda();

const flashSaleTable = process.env.FLASH_SALE_TABLE;
const productTable = process.env.PRODUCT_TABLE;
const userSessionTable = process.env.USER_SESSION_TABLE;
const snsTopicArn = process.env.SNS_TOPIC_ARN;
const maxPurchaseLimit = parseInt(process.env.MAX_PURCHASE_LIMIT, 10) || 5;

exports.handler = async (event) => {
    try {
        const { productId, userId, quantity } = JSON.parse(event.body);

        if (!productId || !userId || !quantity) {
            return response(400, { message: 'Invalid request: missing parameters' });
        }

        if (quantity <= 0 || quantity > maxPurchaseLimit) {
            return response(400, { message: `Purchase limit exceeded. Max allowed: ${maxPurchaseLimit}` });
        }

        // Fetch flash sale product info
        const product = await getProduct(productId);
        if (!product) {
            return response(404, { message: 'Product not found' });
        }

        if (!product.onFlashSale) {
            return response(400, { message: 'Product is not on flash sale' });
        }

        // Validate stock level
        if (product.stock < quantity) {
            return response(400, { message: 'Insufficient stock for the flash sale' });
        }

        // Check if user has already purchased the item
        const userPurchase = await getUserPurchase(userId, productId);
        if (userPurchase && (userPurchase.totalQuantity + quantity > maxPurchaseLimit)) {
            return response(400, { message: 'Purchase limit reached for this user' });
        }

        // Reserve stock for the user
        const updatedStock = product.stock - quantity;
        await reserveProductStock(productId, updatedStock);

        // Create flash sale order and update user's purchase record
        await createFlashSaleOrder(userId, productId, quantity);

        // Send notification to the user
        await notifyUser(userId, productId, quantity);

        return response(200, { message: 'Flash sale purchase successful', productId, quantity });
    } catch (error) {
        console.error('Error handling flash sale:', error);
        return response(500, { message: 'Internal server error' });
    }
};

const getProduct = async (productId) => {
    const params = {
        TableName: productTable,
        Key: { productId }
    };
    const result = await dynamoDb.get(params).promise();
    return result.Item;
};

const getUserPurchase = async (userId, productId) => {
    const params = {
        TableName: userSessionTable,
        Key: { userId, productId }
    };
    const result = await dynamoDb.get(params).promise();
    return result.Item;
};

const reserveProductStock = async (productId, updatedStock) => {
    const params = {
        TableName: productTable,
        Key: { productId },
        UpdateExpression: 'set stock = :stock',
        ConditionExpression: 'stock >= :stock',
        ExpressionAttributeValues: {
            ':stock': updatedStock
        }
    };
    await dynamoDb.update(params).promise();
};

const createFlashSaleOrder = async (userId, productId, quantity) => {
    const order = {
        userId,
        productId,
        quantity,
        createdAt: new Date().toISOString(),
        status: 'PENDING'
    };

    const params = {
        TableName: flashSaleTable,
        Item: order
    };

    await dynamoDb.put(params).promise();

    // Add to user purchase history
    const userPurchaseParams = {
        TableName: userSessionTable,
        Key: { userId, productId },
        UpdateExpression: 'set totalQuantity = if_not_exists(totalQuantity, :start) + :quantity',
        ExpressionAttributeValues: {
            ':start': 0,
            ':quantity': quantity
        }
    };

    await dynamoDb.update(userPurchaseParams).promise();
};

const notifyUser = async (userId, productId, quantity) => {
    const message = `Your purchase of ${quantity} items of Product ID: ${productId} has been successful!`;
    const params = {
        Message: message,
        TopicArn: snsTopicArn,
        Subject: `Flash Sale Purchase Confirmation`,
        MessageAttributes: {
            userId: {
                DataType: 'String',
                StringValue: userId
            }
        }
    };
    await sns.publish(params).promise();
};

const response = (statusCode, body) => {
    return {
        statusCode,
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(body)
    };
};