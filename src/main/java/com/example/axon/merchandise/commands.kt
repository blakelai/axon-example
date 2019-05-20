package com.example.axon.merchandise

import org.axonframework.modelling.command.TargetAggregateIdentifier

abstract class MerchandiseCommand(@TargetAggregateIdentifier open val merchandiseId: String)

data class CreateMerchandiseCommand(
        override val merchandiseId: String,
        open val stock: Integer
) : MerchandiseCommand(merchandiseId)

data class UpdateMerchandiseCommand(
        override val merchandiseId: String,
        open val stock: Integer
) : MerchandiseCommand(merchandiseId)

data class ReserveMerchandiseCommand(
        override val merchandiseId: String,
        open val quantity: Integer,
        open val orderId: String
) : MerchandiseCommand(merchandiseId)

data class RollbackMerchandiseReserveCommand(
        override val merchandiseId: String,
        open val quantity: Integer,
        open val orderId: String
) : MerchandiseCommand(merchandiseId)