package com.example.axon.order;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ProcessingGroup("projections")
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    public List<Order> list() {
        return orderRepository.findAll();
    }

    public Order get(String orderId) {
        return orderRepository.findByOrderId(orderId).get();
    }

    @EventHandler
    public void on(OrderCreatedEvent event) {
        Order order = new Order(event.getOrderId(), event.getItems(), event.getAmount());
        orderRepository.save(order);
    }

    @EventHandler
    public void on(OrderConfirmedEvent event) {
        orderRepository.findByOrderId(event.getOrderId()).ifPresent(order -> {
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
        });
    }

    @EventHandler
    public void on(OrderDeletedEvent event) {
        orderRepository.findByOrderId(event.getOrderId()).ifPresent(order -> orderRepository.delete(order));
    }

    @EventHandler
    public void on(OrderCanceledEvent event) {
        orderRepository.findByOrderId(event.getOrderId()).ifPresent(order -> {
            order.setStatus(OrderStatus.CANCELED);
            orderRepository.save(order);
        });
    }
}
