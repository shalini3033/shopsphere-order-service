package com.shopsphere.order.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.shopsphere.order.dto.OrderRequestDto;
import com.shopsphere.order.entity.Order;
import com.shopsphere.order.service.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    // Create a new order
    @PostMapping
    public Order createOrder(@RequestBody OrderRequestDto request) {

    	Order order = new Order();
    	
    	order.setUserId(request.getUserId());
    	order.setProductId(request.getProductId());
    	order.setQuantity(request.getQuantity());
    	
//        Order createdOrder = orderService.createOrder(order);
//
//        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
        
        return orderService.createOrder(order);
    }

    // Get order by ID
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long orderId) {

        Order order = orderService.getOrderById(orderId);

        return ResponseEntity.ok(order);
    }

    // Get all orders
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {

        List<Order> orders = orderService.getAllOrders();

        return ResponseEntity.ok(orders);
    }
}