package com.example.axon.order;

import lombok.Getter;

public class OrderItem {

    @Getter
    private String merchandiseId;

    @Getter
    private int quantity;

    public OrderItem(String merchandiseId, int quantity) {
        this.merchandiseId = merchandiseId;
        this.quantity = quantity;
    }
}
