package presentation.controllers;

import application.dtos.ProductDTO;
import application.dtos.StockLevelDTO;
import core.entities.ProductEntity;
import core.entities.StockLevelEntity;
import core.services.InventoryService;
import core.services.StockUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    private final StockUpdateService stockUpdateService;

    @Autowired
    public InventoryController(InventoryService inventoryService, StockUpdateService stockUpdateService) {
        this.inventoryService = inventoryService;
        this.stockUpdateService = stockUpdateService;
    }

    @PostMapping("/product")
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO productDTO) {
        try {
            ProductEntity createdProduct = inventoryService.createProduct(productDTO);
            return new ResponseEntity<>(new ProductDTO(createdProduct), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/product/{productId}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long productId, @RequestBody ProductDTO productDTO) {
        try {
            ProductEntity updatedProduct = inventoryService.updateProduct(productId, productDTO);
            if (updatedProduct != null) {
                return new ResponseEntity<>(new ProductDTO(updatedProduct), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long productId) {
        try {
            ProductEntity product = inventoryService.getProductById(productId);
            if (product != null) {
                return new ResponseEntity<>(new ProductDTO(product), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        try {
            List<ProductEntity> products = inventoryService.getAllProducts();
            List<ProductDTO> productDTOs = products.stream().map(ProductDTO::new).toList();
            return new ResponseEntity<>(productDTOs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/product/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {
        try {
            boolean isDeleted = inventoryService.deleteProduct(productId);
            if (isDeleted) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/stock")
    public ResponseEntity<StockLevelDTO> updateStock(@RequestBody StockLevelDTO stockLevelDTO) {
        try {
            StockLevelEntity updatedStock = stockUpdateService.updateStock(stockLevelDTO);
            return new ResponseEntity<>(new StockLevelDTO(updatedStock), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/stock/{productId}")
    public ResponseEntity<StockLevelDTO> getStockByProductId(@PathVariable Long productId) {
        try {
            StockLevelEntity stockLevel = inventoryService.getStockByProductId(productId);
            if (stockLevel != null) {
                return new ResponseEntity<>(new StockLevelDTO(stockLevel), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/stocks")
    public ResponseEntity<List<StockLevelDTO>> getAllStocks() {
        try {
            List<StockLevelEntity> stocks = inventoryService.getAllStocks();
            List<StockLevelDTO> stockDTOs = stocks.stream().map(StockLevelDTO::new).toList();
            return new ResponseEntity<>(stockDTOs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/stock/adjust")
    public ResponseEntity<StockLevelDTO> adjustStockLevel(@RequestParam Long productId, @RequestParam int quantity) {
        try {
            StockLevelEntity adjustedStock = stockUpdateService.adjustStockLevel(productId, quantity);
            if (adjustedStock != null) {
                return new ResponseEntity<>(new StockLevelDTO(adjustedStock), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}