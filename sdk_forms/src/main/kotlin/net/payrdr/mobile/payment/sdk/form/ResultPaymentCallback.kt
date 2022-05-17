package net.payrdr.mobile.payment.sdk.form

/**
 * An interface for handling the result of an operation that returns [PaymentData] or [Exception].
 */
interface ResultPaymentCallback<PaymentData> {

    /**
     * Called when the operation is successful, the result of which is [result].
     */
    fun onSuccess(result: PaymentData)

    /**
     * Called when an error occurs during the execution of an operation. [e] contains a description of the error.
     */
    fun onFail(e: SDKException)
}
