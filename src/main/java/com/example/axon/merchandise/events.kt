package com.example.axon.merchandise

abstract class MerchandiseEvent(open val merchandiseId: String)

data class MerchandiseCreatedEvent(
        override val merchandiseId: String,
        open val stock: Integer
) : MerchandiseEvent(merchandiseId)

data class MerchandiseUpdatedEvent(
        override val merchandiseId: String,
        open val stock: Integer
) : MerchandiseEvent(merchandiseId)

data class MerchandiseReservedEvent(
        override val merchandiseId: String,
        open val quantity: Integer,
        open val orderId: String
) : MerchandiseEvent(merchandiseId)

data class MerchandiseNotEnoughEvent(
        override val merchandiseId: String,
        open val quantity: Integer,
        open val orderId: String
) : MerchandiseEvent(merchandiseId)

data class MerchandiseReserveCanceledEvent(
        override val merchandiseId: String,
        open val quantity: Integer,
        open val orderId: String
) : MerchandiseEvent(merchandiseId)