package com.example.axon.order

import org.axonframework.modelling.command.TargetAggregateIdentifier

abstract class OrderCommand(@TargetAggregateIdentifier open val orderId: String)

data class CreateOrderCommand(
        override val orderId: String,
        open val items: List<OrderItem>,
        open val amount: String
) : OrderCommand(orderId)

data class ConfirmOrderCommand(
        override val orderId: String
) : OrderCommand(orderId)

data class RollbackOrderCommand(
        override val orderId: String
) : OrderCommand(orderId)

data class CancelOrderCommand(
        override val orderId: String
) : OrderCommand(orderId)

data class DoneCancelOrderCommand(
        override val orderId: String
) : OrderCommand(orderId)