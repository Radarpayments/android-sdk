package net.payrdr.mobile.payment.sdk.utils

/**
 * Listing of all available order statuses.
 *
 * @param statusName text representation of status.
 */
enum class OrderStatus(val statusName: String) {

    /**
     * Order was created.
     */
    CREATED("CREATED"),

    /**
     * Order was payed.
     */
    DEPOSITED("DEPOSITED"),

    /**
     * Order was payed.
     */
    APPROVED("APPROVED"),

    /**
     * Order was declined.
     */
    DECLINED("DECLINED"),

}
