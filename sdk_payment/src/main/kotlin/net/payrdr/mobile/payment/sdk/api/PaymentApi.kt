package net.payrdr.mobile.payment.sdk.api

import net.payrdr.mobile.payment.sdk.api.entity.FinishedPaymentInfoResponse
import net.payrdr.mobile.payment.sdk.api.entity.GPaySettings
import net.payrdr.mobile.payment.sdk.api.entity.ProcessFormGPayResponse
import net.payrdr.mobile.payment.sdk.api.entity.ProcessFormResponse
import net.payrdr.mobile.payment.sdk.api.entity.SessionStatusResponse
import net.payrdr.mobile.payment.sdk.api.entity.UnbindCardResponse
import net.payrdr.mobile.payment.sdk.payment.model.ProcessFormRequest
import net.payrdr.mobile.payment.sdk.payment.model.GooglePayProcessFormRequest

/**
 * Interface for carrying out a full payment cycle.
 */
@Suppress("TooManyFunctions")
interface PaymentApi {

    /**
     * API method for getting information about order status .
     *
     * @param mdOrder order number.
     * @return [SessionStatusResponse] order status data.
     */
    suspend fun getSessionStatus(mdOrder: String): SessionStatusResponse

    /**
     * API method for making payments with a new card without 3DS.
     *
     * @param cryptogramApiData cryptogram and data for its creation.
     * @return [ProcessFormResponse] payment status data for first payment try.
     */
    suspend fun processForm(
        cryptogramApiData: ProcessFormRequest
    ): ProcessFormResponse

    /**
     * API method for making payments with a binding card.
     *
     * @param cryptogramApiData cryptogram and data for its creation.
     * @return [ProcessFormResponse] payment status data for first payment try.
     */
    suspend fun processBindingForm(
        cryptogramApiData: ProcessFormRequest
    ): ProcessFormResponse

    /**
     * API method for making payments with a GPay payment.
     *
     * @param cryptogramGPayApiData cryptogram created by GPay lib.
     * @return [ProcessFormGPayResponse] payment status data.
     */
    suspend fun gPayProcessForm(cryptogramGPayApiData: GooglePayProcessFormRequest): ProcessFormGPayResponse

    /**
     * API method for completing payment.
     *
     * @param threeDSServerTransId server transaction id for challenge flow.
     */
    suspend fun finish3dsVer2PaymentAnonymous(threeDSServerTransId: String)

    /**
     * API method for getting payment information.
     *
     * @param orderId order number.
     * @return [FinishedPaymentInfoResponse] payment finish status data.
     */
    suspend fun getFinishedPaymentInfo(orderId: String): FinishedPaymentInfoResponse

    /**
     * API method for getting payment settings.
     *
     * @return [GPaySettings] settings for GPay payment.
     */
    suspend fun getPaymentSettings(login: String): GPaySettings

    /**
     * API method for unbinding card by id.
     *
     * @param bindingId card identifier.
     * @param mdOrder order identifier.
     * @return [UnbindCardResponse] card unbind result.
     */
    suspend fun unbindCardAnonymous(bindingId: String, mdOrder: String): UnbindCardResponse
}
