package com.example.axon.order;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.common.IdentifierFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private CommandGateway commandGateway;

    @Autowired
    private OrderService orderService;

    @GetMapping
    public List<Order> list() {
        return orderService.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order create(@RequestBody Order order) {
        String orderId = IdentifierFactory.getInstance().generateIdentifier();
        CreateOrderCommand cmd = new CreateOrderCommand(
                orderId, order.getItems(), order.getAmount()
        );
        commandGateway.sendAndWait(cmd);
        return new Order(orderId, order.getItems(), order.getAmount());
    }

    @GetMapping("/{orderId}")
    public Order get(@PathVariable String orderId) {
        return orderService.get(orderId);
    }

    @PostMapping("/{orderId}/cancel")
    public void cancel(@PathVariable String orderId) {
        CancelOrderCommand cmd = new CancelOrderCommand(orderId);
        commandGateway.sendAndWait(cmd);
    }
}
