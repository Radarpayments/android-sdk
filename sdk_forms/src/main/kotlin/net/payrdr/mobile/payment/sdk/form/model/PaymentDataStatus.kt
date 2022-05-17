package net.payrdr.mobile.payment.sdk.form.model

/**
 * Possible states of data generation for making a payment.
 */
enum class PaymentDataStatus {

    /**
     * Payment canceled.
     */
    CANCELED,

    /**
     * Data successfully generated.
     */
    SUCCEEDED;

    /**
     * Checking for compliance with the [SUCCEEDED] status.
     *
     * @return returns true if the status is [SUCCEEDED], otherwise false.
     */
    fun isSucceeded() = this == SUCCEEDED

    /**
     * Checking for compliance with the [CANCELED] status.
     *
     * @return returns true if the status is [CANCELED], otherwise false.
     */
    fun isCanceled() = this == CANCELED
}
