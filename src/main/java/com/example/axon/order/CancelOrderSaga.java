package com.example.axon.order;

import com.example.axon.merchandise.MerchandiseReserveCanceledEvent;
import com.example.axon.merchandise.RollbackMerchandiseReserveCommand;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Saga
public class CancelOrderSaga {

    private static final Logger logger = LoggerFactory.getLogger(CancelOrderSaga.class);

    @Autowired
    private transient CommandGateway commandGateway;

    @Autowired
    private transient EventBus eventBus;

    private String orderId;

    private List<OrderItem> items;

    private int toCancelReserveNumber;

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void on(OrderCancelTriggeredEvent event) {
        logger.info("start order cancel saga");
        orderId = event.getOrderId();
        SagaLifecycle.associateWith("orderId", orderId);

        items = event.getItems();
        toCancelReserveNumber = items.size();
        if (event.getStatus() == OrderStatus.CONFIRMED) {
            cancelReserve();
        }
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(OrderConfirmedEvent event) {
        cancelReserve();
    }

    private void cancelReserve() {
        items.forEach(item -> commandGateway.send(
                new RollbackMerchandiseReserveCommand(item.getMerchandiseId(), item.getQuantity(), orderId)
        ));
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(MerchandiseReserveCanceledEvent event) {
        if (--toCancelReserveNumber == 0) {
            eventBus.publish(GenericEventMessage.asEventMessage(new OrderCanceledEvent(orderId)));
        }
    }

    @SagaEventHandler(associationProperty = "orderId")
    @EndSaga
    public void on(OrderCanceledEvent event) {
    }

    @SagaEventHandler(associationProperty = "orderId")
    @EndSaga
    public void on(OrderDeletedEvent event) {
    }
}
