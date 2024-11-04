package infrastructure.http;

import com.website.inventory.application.commands.CreateProductCommandHandler;
import com.website.inventory.application.commands.UpdateStockCommandHandler;
import com.website.inventory.application.queries.CheckStockQueryHandler;
import com.website.inventory.application.queries.GetProductQueryHandler;
import com.website.inventory.application.dtos.ProductDTO;
import com.website.inventory.application.dtos.StockLevelDTO;
import com.website.inventory.core.valueobjects.ProductID;
import com.website.inventory.core.entities.ProductEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final GetProductQueryHandler getProductQueryHandler;
    private final CheckStockQueryHandler checkStockQueryHandler;
    private final CreateProductCommandHandler createProductCommandHandler;
    private final UpdateStockCommandHandler updateStockCommandHandler;

    public InventoryController(GetProductQueryHandler getProductQueryHandler,
                               CheckStockQueryHandler checkStockQueryHandler,
                               CreateProductCommandHandler createProductCommandHandler,
                               UpdateStockCommandHandler updateStockCommandHandler) {
        this.getProductQueryHandler = getProductQueryHandler;
        this.checkStockQueryHandler = checkStockQueryHandler;
        this.createProductCommandHandler = createProductCommandHandler;
        this.updateStockCommandHandler = updateStockCommandHandler;
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> products = getProductQueryHandler.getAllProducts();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable String id) {
        ProductID productID = new ProductID(id);
        ProductDTO product = getProductQueryHandler.getProductById(productID);
        if (product != null) {
            return new ResponseEntity<>(product, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/product")
    public ResponseEntity<String> createProduct(@RequestBody ProductDTO productDTO) {
        try {
            createProductCommandHandler.createProduct(productDTO);
            return new ResponseEntity<>("Product created successfully", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/product/{id}/stock")
    public ResponseEntity<String> updateStock(@PathVariable String id, @RequestBody StockLevelDTO stockLevelDTO) {
        ProductID productID = new ProductID(id);
        try {
            updateStockCommandHandler.updateStock(productID, stockLevelDTO);
            return new ResponseEntity<>("Stock updated successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/product/{id}/stock")
    public ResponseEntity<StockLevelDTO> checkStock(@PathVariable String id) {
        ProductID productID = new ProductID(id);
        StockLevelDTO stockLevelDTO = checkStockQueryHandler.checkStock(productID);
        if (stockLevelDTO != null) {
            return new ResponseEntity<>(stockLevelDTO, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/product/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable String id) {
        ProductID productID = new ProductID(id);
        try {
            getProductQueryHandler.deleteProduct(productID);
            return new ResponseEntity<>("Product deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return new ResponseEntity<>("Inventory service is up and running", HttpStatus.OK);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}