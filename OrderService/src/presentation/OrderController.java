package presentation;

import com.website.order.application.PlaceOrderCommandHandler;
import com.website.order.application.TrackOrderQueryHandler;
import com.website.order.application.dtos.OrderDTO;
import com.website.order.application.commands.PlaceOrderCommand;
import com.website.order.application.queries.TrackOrderQuery;
import com.website.order.core.entities.OrderEntity;
import com.website.order.infrastructure.OrderEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final PlaceOrderCommandHandler placeOrderCommandHandler;
    private final TrackOrderQueryHandler trackOrderQueryHandler;
    private final OrderEventPublisher orderEventPublisher;

    @Autowired
    public OrderController(PlaceOrderCommandHandler placeOrderCommandHandler, 
                           TrackOrderQueryHandler trackOrderQueryHandler,
                           OrderEventPublisher orderEventPublisher) {
        this.placeOrderCommandHandler = placeOrderCommandHandler;
        this.trackOrderQueryHandler = trackOrderQueryHandler;
        this.orderEventPublisher = orderEventPublisher;
    }

    @PostMapping
    public ResponseEntity<OrderDTO> placeOrder(@RequestBody PlaceOrderCommand placeOrderCommand) {
        OrderDTO orderDTO = placeOrderCommandHandler.handle(placeOrderCommand);
        orderEventPublisher.publishOrderPlacedEvent(orderDTO.getOrderId());
        return new ResponseEntity<>(orderDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable UUID orderId) {
        Optional<OrderDTO> orderDTO = trackOrderQueryHandler.handle(new TrackOrderQuery(orderId));
        return orderDTO.map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                       .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        List<OrderDTO> orderList = trackOrderQueryHandler.getAllOrders();
        return new ResponseEntity<>(orderList, HttpStatus.OK);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancelOrder(@PathVariable UUID orderId) {
        boolean success = placeOrderCommandHandler.cancelOrder(orderId);
        if (success) {
            orderEventPublisher.publishOrderCancelledEvent(orderId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<OrderDTO> updateOrder(@PathVariable UUID orderId, @RequestBody PlaceOrderCommand updateOrderCommand) {
        Optional<OrderDTO> updatedOrder = placeOrderCommandHandler.updateOrder(orderId, updateOrderCommand);
        if (updatedOrder.isPresent()) {
            orderEventPublisher.publishOrderUpdatedEvent(orderId);
            return new ResponseEntity<>(updatedOrder.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Internal Helper Methods (for tracking and placing orders)

    private void validateOrder(UUID orderId) {
        if (orderId == null || orderId.toString().isEmpty()) {
            throw new IllegalArgumentException("Invalid orderId");
        }
    }
    
    private ResponseEntity<OrderDTO> responseWithOrder(Optional<OrderDTO> orderDTO) {
        return orderDTO.map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                       .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}