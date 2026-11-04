package com.shopsphere.order.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.shopsphere.order.entity.Order;
import com.shopsphere.order.entity.OrderStatus;
import com.shopsphere.order.event.OrderCreatedEvent;
import com.shopsphere.order.event.OrderEventPublisher;
import com.shopsphere.order.exception.InventoryServiceException;
import com.shopsphere.order.exception.ProductServiceException;
import com.shopsphere.order.repository.OrderRepository;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import com.shopsphere.order.dto.InventoryResponseDto;
import com.shopsphere.order.dto.ProductResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;


@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestClient productRestClient;
    private final RestClient inventoryRestClient;
    private final OrderEventPublisher orderEventPublisher;

    public OrderService(OrderRepository orderRepository, RestClient productRestClient,
    		RestClient inventoryRestClient,OrderEventPublisher orderEventPublisher) {
        this.orderRepository = orderRepository;
        this.productRestClient = productRestClient;
        this.inventoryRestClient= inventoryRestClient;
        this.orderEventPublisher = orderEventPublisher;
        
    }

    public Order createOrder(Order order) {

        ProductResponse product = getProductById(order.getProductId());

        // Reserve stock first
        reserveStock(order.getProductId(), order.getQuantity());

        try {
            order.setUnitPrice(product.getPrice());

            BigDecimal totalAmount = product.getPrice()
                    .multiply(BigDecimal.valueOf(order.getQuantity()));

            order.setTotalAmount(totalAmount);
            order.setStatus(OrderStatus.PENDING);

            Order savedOrder= orderRepository.save(order);
            
            OrderCreatedEvent event = new OrderCreatedEvent(
                    savedOrder.getId(),
                    savedOrder.getUserId(),
                    savedOrder.getProductId(),
                    savedOrder.getQuantity(),
                    savedOrder.getTotalAmount(),
                    savedOrder.getStatus().name()
            ); 
             
            orderEventPublisher.publishOrderCreatedEvent(event);
            return savedOrder;
            
            

        } catch (RuntimeException ex) {
            // If saving the order fails, release the reserved stock
            try {
                releaseStock(order.getProductId(), order.getQuantity());
            } catch (RuntimeException compensationException) {
                ex.addSuppressed(compensationException);
            }

            throw ex;
        }
    }

    public Order getOrderById(Long orderId) {

        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException(
                        "Order not found with ID: " + orderId));
    }

    public List<Order> getAllOrders() {

        return orderRepository.findAll();
    }
    
    private ProductResponse getProductById(Long productId) {

        try {
            return productRestClient.get()
                    .uri("/api/products/{id}", productId)
                    .retrieve()
                    .body(ProductResponse.class);

        } catch (HttpClientErrorException ex) {
            throw new ProductServiceException(
                    ex.getResponseBodyAsString(),
                    ex.getStatusCode()
            );

        } catch (RestClientException ex) {
            throw new ProductServiceException(
                    "Product Service is unavailable. Please try again later.",
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        }
    }
    
    private InventoryResponseDto reserveStock(Long productId, Integer quantity) {

        try {
            return inventoryRestClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/inventory/{productId}/reserve")
                            .queryParam("quantity", quantity)
                            .build(productId))
                    .retrieve()
                    .body(InventoryResponseDto.class);

        } catch (HttpClientErrorException ex) {

            String errorMessage = ex.getResponseBodyAsString();

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(errorMessage);

                errorMessage = jsonNode.has("error")
                        ? jsonNode.get("error").asText()
                        : errorMessage;

            } catch (Exception ignored) {
                // Keep the original response if parsing fails
            }

            throw new InventoryServiceException(
                    errorMessage,
                    ex.getStatusCode()
            );
        

        } catch (RestClientException ex) {
            throw new InventoryServiceException(
                    "Inventory Service is unavailable. Please try again later.",
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        }
    }
    
    public InventoryResponseDto releaseStock(Long productId, Integer quantity) {
        return inventoryRestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/inventory/{productId}/release")
                        .queryParam("quantity", quantity)
                        .build(productId))
                .retrieve()
                .body(InventoryResponseDto.class);
    }
}