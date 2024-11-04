package subscribers;

import com.website.inventoryservice.core.entities.StockLevelEntity;
import com.website.inventoryservice.infrastructure.repositories.StockLevelRepository;
import com.website.inventoryservice.services.StockUpdateService;
import com.website.eventbus.events.StockUpdateEvent;
import com.website.eventbus.EventSubscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * StockUpdateEventSubscriber is responsible for subscribing to StockUpdateEvents
 * and performing the appropriate actions to update the stock levels in the system.
 */
@Component
public class StockUpdateEventSubscriber implements EventSubscriber<StockUpdateEvent> {

    private static final Logger logger = Logger.getLogger(StockUpdateEventSubscriber.class.getName());

    @Autowired
    private StockUpdateService stockUpdateService;

    @Autowired
    private StockLevelRepository stockLevelRepository;

    public StockUpdateEventSubscriber() {
        logger.log(Level.INFO, "Initializing StockUpdateEventSubscriber...");
    }

    /**
     * Handles the stock update event by updating stock levels in the inventory.
     * This method is invoked whenever a StockUpdateEvent is published.
     * 
     * @param event the StockUpdateEvent containing stock update details
     */
    @Override
    @Transactional
    public void handleEvent(StockUpdateEvent event) {
        logger.log(Level.INFO, "Handling StockUpdateEvent for product ID: {0}", event.getProductId());
        
        try {
            // Fetch the current stock level for the product
            StockLevelEntity stockLevelEntity = stockLevelRepository.findByProductId(event.getProductId());
            if (stockLevelEntity == null) {
                logger.log(Level.WARNING, "Stock level for product ID {0} not found.", event.getProductId());
                return;
            }

            // Update stock level based on the event data
            stockLevelEntity.setStockQuantity(stockLevelEntity.getStockQuantity() + event.getQuantityChange());

            // Save the updated stock level to the repository
            stockLevelRepository.save(stockLevelEntity);

            logger.log(Level.INFO, "Updated stock level for product ID {0} to {1}", 
                       new Object[]{event.getProductId(), stockLevelEntity.getStockQuantity()});

            // Trigger any additional services like notifications or downstream events
            stockUpdateService.triggerAdditionalProcesses(event.getProductId());

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error occurred while handling StockUpdateEvent", e);
            throw new RuntimeException("Failed to handle stock update", e);
        }
    }

    /**
     * Logs when the subscription to stock update events starts.
     */
    @Override
    public void subscribe() {
        logger.log(Level.INFO, "Subscribing to StockUpdateEvent...");
    }

    /**
     * Logs when the subscription to stock update events stops.
     */
    @Override
    public void unsubscribe() {
        logger.log(Level.INFO, "Unsubscribing from StockUpdateEvent...");
    }

    /**
     * Handles any cleanup required when the subscriber is destroyed.
     */
    public void onDestroy() {
        logger.log(Level.INFO, "StockUpdateEventSubscriber is being destroyed...");
        unsubscribe();
    }

    /**
     * Initializes the subscriber upon startup.
     */
    public void onInit() {
        logger.log(Level.INFO, "StockUpdateEventSubscriber has been initialized...");
        subscribe();
    }
}