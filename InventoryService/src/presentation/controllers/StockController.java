package presentation.controllers;

import com.inventoryservice.core.entities.StockLevelEntity;
import com.inventoryservice.application.commands.UpdateStockCommandHandler;
import com.inventoryservice.application.queries.CheckStockQueryHandler;
import com.inventoryservice.application.dtos.StockLevelDTO;
import com.inventoryservice.core.services.InventoryService;
import com.inventoryservice.infrastructure.messaging.InventoryEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private UpdateStockCommandHandler updateStockCommandHandler;

    @Autowired
    private CheckStockQueryHandler checkStockQueryHandler;

    @Autowired
    private InventoryEventPublisher inventoryEventPublisher;

    // Fetch all stock levels for products
    @GetMapping
    public ResponseEntity<List<StockLevelDTO>> getAllStockLevels() {
        List<StockLevelDTO> stockLevels = inventoryService.getAllStockLevels();
        return new ResponseEntity<>(stockLevels, HttpStatus.OK);
    }

    // Fetch stock level for a specific product by product ID
    @GetMapping("/{productId}")
    public ResponseEntity<StockLevelDTO> getStockLevelByProductId(@PathVariable("productId") String productId) {
        Optional<StockLevelDTO> stockLevel = inventoryService.getStockLevelByProductId(productId);
        if (stockLevel.isPresent()) {
            return new ResponseEntity<>(stockLevel.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Update stock level for a specific product
    @PostMapping("/{productId}/update")
    public ResponseEntity<String> updateStockLevel(@PathVariable("productId") String productId,
                                                   @RequestParam("quantity") int quantity) {
        boolean success = updateStockCommandHandler.handleUpdateStock(productId, quantity);
        if (success) {
            // Publish stock update event
            inventoryEventPublisher.publishStockUpdateEvent(productId, quantity);
            return new ResponseEntity<>("Stock updated successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Failed to update stock", HttpStatus.BAD_REQUEST);
        }
    }

    // Check if stock is available for a specific product
    @GetMapping("/{productId}/availability")
    public ResponseEntity<Boolean> checkStockAvailability(@PathVariable("productId") String productId) {
        boolean isAvailable = checkStockQueryHandler.checkStockAvailability(productId);
        return new ResponseEntity<>(isAvailable, HttpStatus.OK);
    }

    // Decrease stock level for a product (after an order)
    @PostMapping("/{productId}/decrease")
    public ResponseEntity<String> decreaseStock(@PathVariable("productId") String productId,
                                                @RequestParam("quantity") int quantity) {
        boolean success = updateStockCommandHandler.handleDecreaseStock(productId, quantity);
        if (success) {
            // Publish stock update event
            inventoryEventPublisher.publishStockUpdateEvent(productId, -quantity);
            return new ResponseEntity<>("Stock decreased successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Failed to decrease stock", HttpStatus.BAD_REQUEST);
        }
    }

    // Increase stock level for a product (after a return)
    @PostMapping("/{productId}/increase")
    public ResponseEntity<String> increaseStock(@PathVariable("productId") String productId,
                                                @RequestParam("quantity") int quantity) {
        boolean success = updateStockCommandHandler.handleIncreaseStock(productId, quantity);
        if (success) {
            // Publish stock update event
            inventoryEventPublisher.publishStockUpdateEvent(productId, quantity);
            return new ResponseEntity<>("Stock increased successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Failed to increase stock", HttpStatus.BAD_REQUEST);
        }
    }

    // Reset stock level for a product
    @PostMapping("/{productId}/reset")
    public ResponseEntity<String> resetStockLevel(@PathVariable("productId") String productId) {
        boolean success = inventoryService.resetStockLevel(productId);
        if (success) {
            // Publish stock reset event
            inventoryEventPublisher.publishStockResetEvent(productId);
            return new ResponseEntity<>("Stock reset successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Failed to reset stock", HttpStatus.BAD_REQUEST);
        }
    }

    // Batch update stock levels for multiple products
    @PostMapping("/batch/update")
    public ResponseEntity<String> batchUpdateStockLevels(@RequestBody List<StockLevelDTO> stockLevelUpdates) {
        boolean success = updateStockCommandHandler.handleBatchUpdateStock(stockLevelUpdates);
        if (success) {
            // Publish batch stock update event
            inventoryEventPublisher.publishBatchStockUpdateEvent(stockLevelUpdates);
            return new ResponseEntity<>("Batch stock update successful", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Batch stock update failed", HttpStatus.BAD_REQUEST);
        }
    }

    // Delete stock level for a product
    @DeleteMapping("/{productId}/delete")
    public ResponseEntity<String> deleteStockLevel(@PathVariable("productId") String productId) {
        boolean success = inventoryService.deleteStockLevel(productId);
        if (success) {
            // Publish stock delete event
            inventoryEventPublisher.publishStockDeleteEvent(productId);
            return new ResponseEntity<>("Stock level deleted successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Failed to delete stock level", HttpStatus.BAD_REQUEST);
        }
    }
}