package com.example.axon.order

abstract class OrderEvent(open val orderId: String)

data class OrderCreatedEvent(
        override val orderId: String,
        open val items: List<OrderItem>,
        open val amount: String
) : OrderEvent(orderId)

data class OrderConfirmedEvent(
        override val orderId: String
) : OrderEvent(orderId)

data class OrderDeletedEvent(
        override val orderId: String
) : OrderEvent(orderId)

data class OrderCancelTriggeredEvent(
        override val orderId: String,
        open val status: OrderStatus,
        open val items: List<OrderItem>
) : OrderEvent(orderId)

data class OrderCanceledEvent(
        override val orderId: String
) : OrderEvent(orderId)