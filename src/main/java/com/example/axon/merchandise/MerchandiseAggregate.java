package com.example.axon.merchandise;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate(snapshotTriggerDefinition = "snapshotTriggerDefinition")
public class MerchandiseAggregate {

    private static final Logger logger = LoggerFactory.getLogger(MerchandiseAggregate.class);

    @AggregateIdentifier
    private String merchandiseId;

    private int stock;

    @CommandHandler
    public MerchandiseAggregate(CreateMerchandiseCommand cmd) {
        apply(new MerchandiseCreatedEvent(cmd.getMerchandiseId(), cmd.getStock()));
    }

    @CommandHandler
    public void handle(UpdateMerchandiseCommand cmd) {
        apply(new MerchandiseUpdatedEvent(cmd.getMerchandiseId(), cmd.getStock()));
    }

    @CommandHandler
    public void handle(ReserveMerchandiseCommand cmd) {
        if (cmd.getQuantity() > stock) {
            logger.info("out of stock: only {} left in stock but need {}", stock, cmd.getQuantity());
            apply(new MerchandiseNotEnoughEvent(cmd.getMerchandiseId(), cmd.getQuantity(), cmd.getOrderId()));
        } else {
            apply(new MerchandiseReservedEvent(cmd.getMerchandiseId(), cmd.getQuantity(), cmd.getOrderId()));
        }
    }

    @CommandHandler
    public void handle(RollbackMerchandiseReserveCommand cmd) {
        apply(new MerchandiseReserveCanceledEvent(cmd.getMerchandiseId(), cmd.getQuantity(), cmd.getOrderId()));
    }

    @EventSourcingHandler
    public void on(MerchandiseCreatedEvent event) {
        this.merchandiseId = event.getMerchandiseId();
        this.stock = event.getStock();
    }

    @EventSourcingHandler
    public void on(MerchandiseUpdatedEvent event) {
        this.stock = event.getStock();
    }

    @EventSourcingHandler
    public void on(MerchandiseReservedEvent event) {
        logger.info("handle merchandise reserve event");
        this.merchandiseId = event.getMerchandiseId();
        this.stock -= event.getQuantity();
    }

    @EventSourcingHandler
    public void on(MerchandiseReserveCanceledEvent event) {
        logger.info("handle merchandise reserve cancel event");
        this.merchandiseId = event.getMerchandiseId();
        this.stock += event.getQuantity();
    }

    protected MerchandiseAggregate() { }

}
