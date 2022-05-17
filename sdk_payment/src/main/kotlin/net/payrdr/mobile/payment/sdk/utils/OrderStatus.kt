package net.payrdr.mobile.payment.sdk.utils

/**
 * Listing of all available order statuses.
 */
enum class OrderStatus {
    /**
     * Order was payed.
     */
    DEPOSITED,

    /**
     * Order was payed.
     */
    APPROVED,

    /**
     * Order was declined.
     */
    DECLINED
}
