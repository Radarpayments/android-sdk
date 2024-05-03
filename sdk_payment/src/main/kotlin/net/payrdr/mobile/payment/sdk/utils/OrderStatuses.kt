package net.payrdr.mobile.payment.sdk.utils

internal object OrderStatuses {

    val payedStatues = listOf(OrderStatus.DEPOSITED, OrderStatus.APPROVED)
    val payedCouldNotBeCompleted = listOf(OrderStatus.DECLINED)
}
