package infrastructure.http;

import com.website.inventoryservice.application.dtos.StockLevelDTO;
import com.website.inventoryservice.application.commands.UpdateStockCommandHandler;
import com.website.inventoryservice.application.queries.CheckStockQueryHandler;
import com.website.inventoryservice.core.valueobjects.ProductID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    private final UpdateStockCommandHandler updateStockCommandHandler;
    private final CheckStockQueryHandler checkStockQueryHandler;

    @Autowired
    public StockController(UpdateStockCommandHandler updateStockCommandHandler,
                           CheckStockQueryHandler checkStockQueryHandler) {
        this.updateStockCommandHandler = updateStockCommandHandler;
        this.checkStockQueryHandler = checkStockQueryHandler;
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateStock(@RequestBody StockLevelDTO stockLevelDTO) {
        try {
            updateStockCommandHandler.handle(stockLevelDTO);
            return new ResponseEntity<>("Stock updated successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update stock", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/check/{productId}")
    public ResponseEntity<StockLevelDTO> checkStock(@PathVariable("productId") String productId) {
        try {
            Optional<StockLevelDTO> stockLevelDTO = checkStockQueryHandler.handle(new ProductID(productId));
            return stockLevelDTO.map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/bulk-update")
    public ResponseEntity<String> bulkUpdateStock(@RequestBody List<StockLevelDTO> stockLevelDTOList) {
        try {
            for (StockLevelDTO stockLevelDTO : stockLevelDTOList) {
                updateStockCommandHandler.handle(stockLevelDTO);
            }
            return new ResponseEntity<>("Bulk stock update successful", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Bulk stock update failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<StockLevelDTO>> getAllStockLevels() {
        try {
            List<StockLevelDTO> allStockLevels = checkStockQueryHandler.getAllStockLevels();
            return new ResponseEntity<>(allStockLevels, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{productId}")
    public ResponseEntity<String> deleteStockForProduct(@PathVariable("productId") String productId) {
        try {
            boolean deleted = updateStockCommandHandler.deleteStock(new ProductID(productId));
            if (deleted) {
                return new ResponseEntity<>("Stock deleted successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Stock not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to delete stock", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<StockLevelDTO>> getLowStockProducts() {
        try {
            List<StockLevelDTO> lowStockProducts = checkStockQueryHandler.getLowStockProducts();
            return new ResponseEntity<>(lowStockProducts, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/replenish/{productId}")
    public ResponseEntity<String> replenishStock(@PathVariable("productId") String productId,
                                                 @RequestParam("quantity") int quantity) {
        try {
            StockLevelDTO stockLevelDTO = new StockLevelDTO();
            stockLevelDTO.setProductId(productId);
            stockLevelDTO.setStockLevel(quantity);
            updateStockCommandHandler.replenishStock(stockLevelDTO);
            return new ResponseEntity<>("Stock replenished successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to replenish stock", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> transferStock(@RequestParam("fromProductId") String fromProductId,
                                                @RequestParam("toProductId") String toProductId,
                                                @RequestParam("quantity") int quantity) {
        try {
            boolean success = updateStockCommandHandler.transferStock(new ProductID(fromProductId),
                                                                       new ProductID(toProductId),
                                                                       quantity);
            if (success) {
                return new ResponseEntity<>("Stock transferred successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Stock transfer failed", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to transfer stock", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/adjust/{productId}")
    public ResponseEntity<String> adjustStock(@PathVariable("productId") String productId,
                                              @RequestParam("adjustment") int adjustment) {
        try {
            boolean success = updateStockCommandHandler.adjustStock(new ProductID(productId), adjustment);
            if (success) {
                return new ResponseEntity<>("Stock adjusted successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Stock adjustment failed", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to adjust stock", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}