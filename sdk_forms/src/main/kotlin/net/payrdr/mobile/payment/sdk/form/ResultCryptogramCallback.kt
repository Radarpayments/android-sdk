package net.payrdr.mobile.payment.sdk.form

/**
 * An interface for handling the result of an operation that returns [CryptogramData] or [Exception].
 */
interface ResultCryptogramCallback<CryptogramData> {

    /**
     * Called when the operation is successful, the result of which is [result].
     */
    fun onSuccess(result: CryptogramData)

    /**
     * Called when an error occurs during the execution of an operation. [e] contains a description of the error.
     */
    fun onFail(e: SDKException)
}
