package net.payrdr.mobile.payment.sdk.api

import net.payrdr.mobile.payment.sdk.LogDebug
import net.payrdr.mobile.payment.sdk.SDKPayment
import net.payrdr.mobile.payment.sdk.api.entity.FinishedPaymentInfoResponse
import net.payrdr.mobile.payment.sdk.api.entity.GPaySettings
import net.payrdr.mobile.payment.sdk.api.entity.ProcessFormGPayResponse
import net.payrdr.mobile.payment.sdk.api.entity.ProcessFormResponse
import net.payrdr.mobile.payment.sdk.api.entity.SessionStatusResponse
import net.payrdr.mobile.payment.sdk.api.entity.UnbindCardResponse
import net.payrdr.mobile.payment.sdk.exceptions.SDKPaymentApiException
import net.payrdr.mobile.payment.sdk.form.utils.executeGet
import net.payrdr.mobile.payment.sdk.form.utils.executePostJson
import net.payrdr.mobile.payment.sdk.form.utils.executePostParams
import net.payrdr.mobile.payment.sdk.form.utils.responseBodyToJsonObject
import net.payrdr.mobile.payment.sdk.payment.model.GooglePayProcessFormRequest
import net.payrdr.mobile.payment.sdk.payment.model.ProcessFormRequest
import net.payrdr.mobile.payment.sdk.utils.mapToJsonString
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
            val connection = URL("$baseUrl/rest/getSessionStatus.do").executePostParams(
                paramBody = body,
                sslContext = SDKPayment.sdkPaymentConfig.sslContextConfig?.sslContext,
            )
            SessionStatusResponse.fromJson(connection.responseBodyToJsonObject())
        }

    override suspend fun processForm(
        cryptogramApiData: ProcessFormRequest,
    ): ProcessFormResponse = startRunCatching {
        val body = mutableMapOf(
            "seToken" to cryptogramApiData.paymentToken,
            "MDORDER" to cryptogramApiData.mdOrder,
            "TEXT" to cryptogramApiData.holder,
            "bindingNotNeeded" to "${(!cryptogramApiData.saveCard)}",
            "threeDSSDK" to "false",
        )
        if (cryptogramApiData.additionalPayerData.isEmpty().not()) {
            body["billingPayerData"] = cryptogramApiData.additionalPayerData.mapToJsonString()
        }
        if (cryptogramApiData.mobilePhone != null) {
            val orderPayerData = mapOf("mobilePhone" to cryptogramApiData.mobilePhone)
            body["orderPayerData"] = orderPayerData.mapToJsonString()
        }
        if (cryptogramApiData.email != null) {
            body["email"] = cryptogramApiData.email
        }
        val connection = URL("$baseUrl/rest/processform.do").executePostParams(
            paramBody = body,
            sslContext = SDKPayment.sdkPaymentConfig.sslContextConfig?.sslContext
        )
        val res = connection.responseBodyToJsonObject()
        LogDebug.logIfDebug(res.toString())
        ProcessFormResponse.fromJson(res)
    }

    override suspend fun processBindingForm(
        cryptogramApiData: ProcessFormRequest,
    ): ProcessFormResponse = startRunCatching {
        val body = mutableMapOf(
            "seToken" to cryptogramApiData.paymentToken,
            "MDORDER" to cryptogramApiData.mdOrder,
            "TEXT" to cryptogramApiData.holder,
            "threeDSSDK" to "false"
        )
        if (cryptogramApiData.additionalPayerData.isEmpty().not()) {
            body["billingPayerData"] = cryptogramApiData.additionalPayerData.mapToJsonString()
        }
        if (cryptogramApiData.mobilePhone != null) {
            val orderPayerData = mapOf("mobilePhone" to cryptogramApiData.mobilePhone)
            body["orderPayerData"] = orderPayerData.mapToJsonString()
        }
        if (cryptogramApiData.email != null) {
            body["email"] = cryptogramApiData.email
        }
        val connection = URL("$baseUrl/rest/processBindingForm.do").executePostParams(
            paramBody = body,
            sslContext = SDKPayment.sdkPaymentConfig.sslContextConfig?.sslContext
        )
        val res = connection.responseBodyToJsonObject()
        LogDebug.logIfDebug(res.toString())
        ProcessFormResponse.fromJson(res)
    }

    override suspend fun gPayProcessForm(
        cryptogramGPayApiData: GooglePayProcessFormRequest
    ): ProcessFormGPayResponse = startRunCatching {
        val jsonBody =
            "{\"paymentToken\":\"${cryptogramGPayApiData.paymentToken}\"," +
                "\"mdOrder\":\"${cryptogramGPayApiData.mdOrder}\"}"
        LogDebug.logIfDebug(jsonBody)
        val connection = URL("$baseUrl/google/paymentOrder.do").executePostJson(
            jsonBody = jsonBody,
            sslContext = SDKPayment.sdkPaymentConfig.sslContextConfig?.sslContext
        )
        val res = connection.responseBodyToJsonObject()
        LogDebug.logIfDebug(res.toString())
        ProcessFormGPayResponse.fromJson(res)
    }

    override suspend fun finish3dsVer2PaymentAnonymous(threeDSServerTransId: String) {
        startRunCatching {
            val body = mapOf("threeDSServerTransId" to threeDSServerTransId)
            val connection = URL("$baseUrl/rest/finish3dsVer2PaymentAnonymous.do")
                .executePostParams(
                    paramBody = body,
                    sslContext = SDKPayment.sdkPaymentConfig.sslContextConfig?.sslContext
                )
            val res = connection.responseBodyToJsonObject()
            LogDebug.logIfDebug(res.toString())
        }
    }

    override suspend fun getFinishedPaymentInfo(orderId: String): FinishedPaymentInfoResponse =
        startRunCatching {
            val body = mapOf("orderId" to orderId)
            val connection = URL("$baseUrl/rest/getFinishedPaymentInfo.do")
                .executePostParams(
                    paramBody = body,
                    sslContext = SDKPayment.sdkPaymentConfig.sslContextConfig?.sslContext
                )
            val res = connection.responseBodyToJsonObject()
            LogDebug.logIfDebug(res.toString())
            FinishedPaymentInfoResponse.fromJson(res)
        }

    override suspend fun getPaymentSettings(login: String): GPaySettings =
        startRunCatching {
            val connection = URL("$baseUrl/rest/getPaymentSettings.do?login=$login").executeGet(
                sslContext = SDKPayment.sdkPaymentConfig.sslContextConfig?.sslContext
            )
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
            .executePostParams(
                paramBody = body,
                sslContext = SDKPayment.sdkPaymentConfig.sslContextConfig?.sslContext
            )
        val res = connection.responseBodyToJsonObject()
        LogDebug.logIfDebug(res.toString())
        UnbindCardResponse.fromJson(res)
    }

    private fun <T> startRunCatching(block: () -> T): T {
        return try {
            block()
        } catch (e: Exception) {
            throw SDKPaymentApiException(cause = e.cause, message = e.message.toString())
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
