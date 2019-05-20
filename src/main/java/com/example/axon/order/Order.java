package com.example.axon.order;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
public class Order {
    @Id
    @Getter
    private String orderId;

    @Getter
    @Setter
    private OrderStatus status;
    @Getter
    private List<OrderItem> items;
    @Getter
    private String amount;

    //@Version
    //private int version;

    public Order(String orderId, List<OrderItem> items, String amount) {
        this.orderId = orderId;
        this.status = OrderStatus.INITIAL;
        this.items = items;
        this.amount = amount;
    }
}
