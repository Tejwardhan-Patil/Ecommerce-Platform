package infrastructure.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.services.StockUpdateService;
import application.dtos.StockLevelDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class StockUpdateEventSubscriber {

    private static final Logger logger = LoggerFactory.getLogger(StockUpdateEventSubscriber.class);
    private final StockUpdateService stockUpdateService;
    private final ObjectMapper objectMapper;

    public StockUpdateEventSubscriber(StockUpdateService stockUpdateService, ObjectMapper objectMapper) {
        this.stockUpdateService = stockUpdateService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "${rabbitmq.stock.update.queue}")
    public void onMessage(String message) {
        try {
            logger.info("Received stock update event: {}", message);
            StockLevelDTO stockLevelDTO = objectMapper.readValue(message, StockLevelDTO.class);
            stockUpdateService.updateStockLevel(stockLevelDTO);
            logger.info("Processed stock update event for product: {}", stockLevelDTO.getProductId());
        } catch (Exception e) {
            logger.error("Failed to process stock update event", e);
        }
    }

    @RabbitListener(queues = "${rabbitmq.stock.low.queue}")
    public void onLowStockMessage(String message) {
        try {
            logger.info("Received low stock event: {}", message);
            StockLevelDTO stockLevelDTO = objectMapper.readValue(message, StockLevelDTO.class);
            stockUpdateService.notifyLowStock(stockLevelDTO);
            logger.info("Processed low stock event for product: {}", stockLevelDTO.getProductId());
        } catch (Exception e) {
            logger.error("Failed to process low stock event", e);
        }
    }

    @RabbitListener(queues = "${rabbitmq.stock.out.queue}")
    public void onOutOfStockMessage(String message) {
        try {
            logger.info("Received out-of-stock event: {}", message);
            StockLevelDTO stockLevelDTO = objectMapper.readValue(message, StockLevelDTO.class);
            stockUpdateService.handleOutOfStock(stockLevelDTO);
            logger.info("Processed out-of-stock event for product: {}", stockLevelDTO.getProductId());
        } catch (Exception e) {
            logger.error("Failed to process out-of-stock event", e);
        }
    }

    @RabbitListener(queues = "${rabbitmq.stock.replenish.queue}")
    public void onStockReplenishmentMessage(String message) {
        try {
            logger.info("Received stock replenishment event: {}", message);
            StockLevelDTO stockLevelDTO = objectMapper.readValue(message, StockLevelDTO.class);
            stockUpdateService.replenishStock(stockLevelDTO);
            logger.info("Processed stock replenishment event for product: {}", stockLevelDTO.getProductId());
        } catch (Exception e) {
            logger.error("Failed to process stock replenishment event", e);
        }
    }

    @RabbitListener(queues = "${rabbitmq.stock.reserved.queue}")
    public void onStockReservedMessage(String message) {
        try {
            logger.info("Received stock reserved event: {}", message);
            StockLevelDTO stockLevelDTO = objectMapper.readValue(message, StockLevelDTO.class);
            stockUpdateService.reserveStock(stockLevelDTO);
            logger.info("Processed stock reserved event for product: {}", stockLevelDTO.getProductId());
        } catch (Exception e) {
            logger.error("Failed to process stock reserved event", e);
        }
    }

    @RabbitListener(queues = "${rabbitmq.stock.released.queue}")
    public void onStockReleasedMessage(String message) {
        try {
            logger.info("Received stock released event: {}", message);
            StockLevelDTO stockLevelDTO = objectMapper.readValue(message, StockLevelDTO.class);
            stockUpdateService.releaseStock(stockLevelDTO);
            logger.info("Processed stock released event for product: {}", stockLevelDTO.getProductId());
        } catch (Exception e) {
            logger.error("Failed to process stock released event", e);
        }
    }

    @RabbitListener(queues = "${rabbitmq.stock.damaged.queue}")
    public void onStockDamagedMessage(String message) {
        try {
            logger.info("Received stock damaged event: {}", message);
            StockLevelDTO stockLevelDTO = objectMapper.readValue(message, StockLevelDTO.class);
            stockUpdateService.handleDamagedStock(stockLevelDTO);
            logger.info("Processed stock damaged event for product: {}", stockLevelDTO.getProductId());
        } catch (Exception e) {
            logger.error("Failed to process stock damaged event", e);
        }
    }
}