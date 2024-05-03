package net.payrdr.mobile.payment.sdk

/**
 * An interface for handling the result of an operation that returns [PaymentResult].
 */
interface ResultPaymentCallback<PaymentResult> {

    /**
     * Called when the operation is successful, the result of which is [result].
     */
    fun onResult(result: PaymentResult)

}
