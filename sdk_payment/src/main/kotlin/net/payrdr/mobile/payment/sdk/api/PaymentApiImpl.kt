package net.payrdr.mobile.payment.sdk.api

import net.payrdr.mobile.payment.sdk.LogDebug
import net.payrdr.mobile.payment.sdk.api.entity.FinishedPaymentInfoResponse
import net.payrdr.mobile.payment.sdk.api.entity.GPaySettings
import net.payrdr.mobile.payment.sdk.api.entity.ProcessFormGPayResponse
import net.payrdr.mobile.payment.sdk.api.entity.ProcessFormResponse
import net.payrdr.mobile.payment.sdk.api.entity.ProcessFormSecondResponse
import net.payrdr.mobile.payment.sdk.api.entity.SessionStatusResponse
import net.payrdr.mobile.payment.sdk.api.entity.UnbindCardResponse
import net.payrdr.mobile.payment.sdk.exceptions.SDKPaymentApiException
import net.payrdr.mobile.payment.sdk.form.utils.executeGet
import net.payrdr.mobile.payment.sdk.form.utils.executePostJson
import net.payrdr.mobile.payment.sdk.form.utils.executePostParams
import net.payrdr.mobile.payment.sdk.form.utils.responseBodyToJsonObject
import net.payrdr.mobile.payment.sdk.payment.model.CryptogramApiData
import net.payrdr.mobile.payment.sdk.payment.model.CryptogramGPayApiData
import java.net.URL

/**
 * Implementation of API methods for a full payment cycle.
 *
 * @param baseUrl the main part of the server address .
 */
@Suppress("TooGenericExceptionCaught", "TooManyFunctions")
class PaymentApiImpl(
    private val baseUrl: String
) : PaymentApi {
    override suspend fun getSessionStatus(mdOrder: String): SessionStatusResponse =
        startRunCatching {
            val body = mapOf("MDORDER" to mdOrder)
            val connection = URL("$baseUrl/rest/getSessionStatus.do").executePostParams(body)
            SessionStatusResponse.fromJson(connection.responseBodyToJsonObject())
        }

    override suspend fun processForm(
        cryptogramApiData: CryptogramApiData,
        threeDSSDK: Boolean
    ): ProcessFormResponse = startRunCatching {
        val body = mapOf(
            "seToken" to cryptogramApiData.seToken,
            "MDORDER" to cryptogramApiData.mdOrder,
            "TEXT" to cryptogramApiData.holder,
            "bindingNotNeeded" to "${(!cryptogramApiData.saveCard)}",
            "threeDSSDK" to "$threeDSSDK"
        )
        val connection = URL("$baseUrl/rest/processform.do").executePostParams(body)
        val res = connection.responseBodyToJsonObject()
        LogDebug.logIfDebug(res.toString())
        ProcessFormResponse.fromJson(res)
    }

    override suspend fun processBindingForm(
        cryptogramApiData: CryptogramApiData,
        threeDSSDK: Boolean
    ): ProcessFormResponse = startRunCatching {
        val body = mapOf(
            "seToken" to cryptogramApiData.seToken,
            "MDORDER" to cryptogramApiData.mdOrder,
            "TEXT" to cryptogramApiData.holder,
            "threeDSSDK" to "$threeDSSDK"
        )
        val connection = URL("$baseUrl/rest/processBindingForm.do").executePostParams(body)
        val res = connection.responseBodyToJsonObject()
        LogDebug.logIfDebug(res.toString())
        ProcessFormResponse.fromJson(res)
    }

    override suspend fun processFormSecond(
        cryptogramApiData: CryptogramApiData,
        threeDSParams: PaymentThreeDSInfo
    ): ProcessFormSecondResponse = startRunCatching {
        val body = mapOf(
            "seToken" to cryptogramApiData.seToken,
            "MDORDER" to cryptogramApiData.mdOrder,
            "TEXT" to cryptogramApiData.holder,
            "bindingNotNeeded" to "${(!cryptogramApiData.saveCard)}",
            "threeDSSDK" to threeDSParams.threeDSSDK.toString(),
            "threeDSServerTransId" to threeDSParams.threeDSServerTransId,
            "threeDSSDKEncData" to threeDSParams.threeDSSDKEncData,
            "threeDSSDKEphemPubKey" to threeDSParams.threeDSSDKEphemPubKey,
            "threeDSSDKAppId" to threeDSParams.threeDSSDKAppId,
            "threeDSSDKTransId" to threeDSParams.threeDSSDKTransId,
            "threeDSSDKReferenceNumber" to threeDSParams.threeDSSDKReferenceNumber
        )
        val connection = URL("$baseUrl/rest/processform.do").executePostParams(body)
        val res = connection.responseBodyToJsonObject()
        LogDebug.logIfDebug(res.toString())
        ProcessFormSecondResponse.fromJson(res)
    }

    override suspend fun processBindingFormSecond(
        cryptogramApiData: CryptogramApiData,
        threeDSParams: PaymentThreeDSInfo
    ): ProcessFormSecondResponse = startRunCatching {
        val body = mapOf(
            "seToken" to cryptogramApiData.seToken,
            "MDORDER" to cryptogramApiData.mdOrder,
            "TEXT" to cryptogramApiData.holder,
            "threeDSSDK" to threeDSParams.threeDSSDK.toString(),
            "threeDSServerTransId" to threeDSParams.threeDSServerTransId,
            "threeDSSDKEncData" to threeDSParams.threeDSSDKEncData,
            "threeDSSDKEphemPubKey" to threeDSParams.threeDSSDKEphemPubKey,
            "threeDSSDKAppId" to threeDSParams.threeDSSDKAppId,
            "threeDSSDKTransId" to threeDSParams.threeDSSDKTransId,
            "threeDSSDKReferenceNumber" to threeDSParams.threeDSSDKReferenceNumber
        )
        val connection = URL("$baseUrl/rest/processBindingForm.do").executePostParams(body)
        val res = connection.responseBodyToJsonObject()
        LogDebug.logIfDebug(res.toString())
        ProcessFormSecondResponse.fromJson(res)
    }

    override suspend fun gPayProcessForm(
        cryptogramGPayApiData: CryptogramGPayApiData
    ): ProcessFormGPayResponse = startRunCatching {
        val jsonBody =
            "{\"paymentToken\":\"${cryptogramGPayApiData.paymentToken}\"," +
                "\"mdOrder\":\"${cryptogramGPayApiData.mdOrder}\"}"
        LogDebug.logIfDebug(jsonBody)
        val connection = URL("$baseUrl/google/paymentOrder.do").executePostJson(jsonBody)
        val res = connection.responseBodyToJsonObject()
        LogDebug.logIfDebug(res.toString())
        ProcessFormGPayResponse.fromJson(res)
    }

    override suspend fun finish3dsVer2PaymentAnonymous(threeDSServerTransId: String) {
        startRunCatching {
            val body = mapOf("threeDSServerTransId" to threeDSServerTransId)
            val connection = URL("$baseUrl/rest/finish3dsVer2PaymentAnonymous.do")
                .executePostParams(body)
            val res = connection.responseBodyToJsonObject()
            LogDebug.logIfDebug(res.toString())
        }
    }

    override suspend fun getFinishedPaymentInfo(orderId: String): FinishedPaymentInfoResponse =
        startRunCatching {
            val body = mapOf("orderId" to orderId)
            val connection = URL("$baseUrl/rest/getFinishedPaymentInfo.do")
                .executePostParams(body)
            val res = connection.responseBodyToJsonObject()
            LogDebug.logIfDebug(res.toString())
            FinishedPaymentInfoResponse.fromJson(res)
        }

    override suspend fun getPaymentSettings(): GPaySettings =
        startRunCatching {
            val connection = URL("$baseUrl/rest/getPaymentSettings.do?login=3ds2_dev")
                .executeGet()
            val res = connection.responseBodyToJsonObject()
            LogDebug.logIfDebug(res.toString())
            GPaySettings.fromJson(res)
        }

    override suspend fun unbindCardAnonymous(
        bindingId: String,
        mdOrder: String
    ): UnbindCardResponse = startRunCatching {
        val body = mapOf(
            "mdOrder" to mdOrder,
            "bindingId" to bindingId
        )
        val connection = URL("$baseUrl/rest/unbindcardanon.do")
            .executePostParams(body)
        val res = connection.responseBodyToJsonObject()
        LogDebug.logIfDebug(res.toString())
        UnbindCardResponse.fromJson(res)
    }

    private fun <T> startRunCatching(block: () -> T): T {
        return try {
            block()
        } catch (e: Exception) {
            throw SDKPaymentApiException(cause = e.cause)
        }
    }

    /**
     * Data class for storing information about 3SD payments.
     * @param threeDSSDK there is 3DS for payment.
     * @param threeDSServerTransId 3DS2 identifier in 3DS Server .
     * @param threeDSSDKEncData encrypted device data .
     * @param threeDSSDKEphemPubKey key for encryption during exchange with ACS.
     * @param threeDSSDKAppId identifier SDK.
     * @param threeDSSDKTransId transaction identifier SDK.
     * @param threeDSSDKReferenceNumber reference number.
     */
    data class PaymentThreeDSInfo(
        val threeDSSDK: Boolean,
        val threeDSServerTransId: String,
        val threeDSSDKEncData: String,
        val threeDSSDKEphemPubKey: String,
        val threeDSSDKAppId: String,
        val threeDSSDKTransId: String,
        val threeDSSDKReferenceNumber: String
    )
}
