package com.example.axon.order;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;
import static org.axonframework.modelling.command.AggregateLifecycle.markDeleted;

@Aggregate(snapshotTriggerDefinition = "snapshotTriggerDefinition")
public class OrderAggregate {

    private Logger logger = LoggerFactory.getLogger(OrderAggregate.class);

    @AggregateIdentifier
    private String orderId;

    private OrderStatus status;

    private List<OrderItem> items;

    private String amount;

    private boolean isCancelTriggered;

    @CommandHandler
    public OrderAggregate(CreateOrderCommand cmd) {
        if (cmd.getItems() == null || cmd.getItems().isEmpty()) {
            throw new IllegalArgumentException("order items must not empty");
        }
        apply(new OrderCreatedEvent(cmd.getOrderId(), cmd.getItems(), cmd.getAmount()));
    }

    @CommandHandler
    public void handle(ConfirmOrderCommand cmd) {
        if (status != OrderStatus.INITIAL) {
            throw new IllegalStateException("order status must be initial");
        }
        apply(new OrderConfirmedEvent(cmd.getOrderId()));
    }

    @CommandHandler
    public void handle(RollbackOrderCommand cmd) {
        if (status != OrderStatus.INITIAL) {
            throw new IllegalStateException("order status must be initial");
        }
        apply(new OrderDeletedEvent(cmd.getOrderId()));
    }

    @CommandHandler
    public void handle(CancelOrderCommand cmd) {
        logger.info("orderId={}, status={}, amount={}", orderId, status.name(), amount);
        if (isCancelTriggered) {
            throw new IllegalStateException("order has triggered cancel process");
        }
        if (status != OrderStatus.INITIAL && status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("cannot cancel order with status " + status.name());
        }

        apply(new OrderCancelTriggeredEvent(cmd.getOrderId(), status, items));
    }

    @EventSourcingHandler
    public void on(OrderCreatedEvent event) {
        logger.info("handle order created event");
        this.orderId = event.getOrderId();
        this.status = OrderStatus.INITIAL;
        this.items = event.getItems();
        this.amount = event.getAmount();
        this.isCancelTriggered = false;
    }

    @EventSourcingHandler
    public void on(OrderConfirmedEvent event) {
        logger.info("handle order confirmed event");
        this.status = OrderStatus.CONFIRMED;
    }

    @EventSourcingHandler
    public void on(OrderDeletedEvent event) {
        logger.info("handle order deleted event");
        this.status = OrderStatus.DELETED;
        markDeleted();
    }

    @EventSourcingHandler
    public void on(OrderCancelTriggeredEvent event) {
        this.isCancelTriggered = true;
    }

    @EventSourcingHandler
    public void on(OrderCanceledEvent event) {
        logger.info("handle order canceled event");
        this.status = OrderStatus.CANCELED;
    }

    protected OrderAggregate() { }
}
