package com.example.axon.order;

import com.example.axon.merchandise.MerchandiseNotEnoughEvent;
import com.example.axon.merchandise.MerchandiseReserveCanceledEvent;
import com.example.axon.merchandise.MerchandiseReservedEvent;
import com.example.axon.merchandise.ReserveMerchandiseCommand;
import com.example.axon.merchandise.RollbackMerchandiseReserveCommand;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

@Saga
public class CreateOrderSaga {

    private static final Logger logger = LoggerFactory.getLogger(CreateOrderSaga.class);

    @Autowired
    private transient CommandGateway commandGateway;

    private String orderId;
    private Map<String, OrderItem> toReserve;
    private Map<String, OrderItem> toRollback;
    private int toReserveNumber;
    private boolean needRollback = false;

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void on(OrderCreatedEvent event) {
        logger.info("start order create saga");
        orderId = event.getOrderId();
        SagaLifecycle.associateWith("orderId", orderId);

        toReserve = new HashMap<>();
        toRollback = new HashMap<>();
        toReserveNumber = event.getItems().size();
        for (OrderItem item : event.getItems()) {
            toReserve.put(item.getMerchandiseId(), item);
        }

        event.getItems().forEach(item -> {
                commandGateway.send(
                        new ReserveMerchandiseCommand(item.getMerchandiseId(), item.getQuantity(), orderId)
                );
        });
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(MerchandiseReservedEvent event) {
        OrderItem item = toReserve.remove(event.getMerchandiseId());
        toRollback.put(event.getMerchandiseId(), item);
        if (--toReserveNumber == 0) {
            finish();
        }
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(MerchandiseNotEnoughEvent event) {
        toReserve.remove(event.getMerchandiseId());
        needRollback = true;
        if (--toReserveNumber == 0) {
            finish();
        }
    }

    private void finish() {
        if (needRollback) {
            for (OrderItem item : toRollback.values()) {
                commandGateway.send(new RollbackMerchandiseReserveCommand(
                        item.getMerchandiseId(), item.getQuantity(), orderId
                ));
            }
            if (toRollback.isEmpty()) {
                commandGateway.send(new RollbackOrderCommand(orderId));
            }
            return;
        }
        commandGateway.send(new ConfirmOrderCommand(orderId));
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(MerchandiseReserveCanceledEvent event) {
        toRollback.remove(event.getMerchandiseId());
        if (toRollback.isEmpty()) {
            commandGateway.send(new RollbackOrderCommand(orderId));
        }
    }

    @SagaEventHandler(associationProperty = "orderId")
    @EndSaga
    public void on(OrderConfirmedEvent event) {
        logger.info("Order {} is confirmed", event.getOrderId());
    }

    @SagaEventHandler(associationProperty = "orderId")
    @EndSaga
    public void on(OrderDeletedEvent event) {
        logger.info("Order {} is deleted", event.getOrderId());
    }
}
