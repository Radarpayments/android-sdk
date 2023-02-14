package net.payrdr.mobile.payment.sample.kotlin.threeds

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.timeout
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.payrdr.mobile.payment.sample.kotlin.MarketApplication

/**
 * The class provides the implementation of the necessary methods for making a payment.
 */
class ThreeDSGatewayApi {

    private val httpClient by lazy {
        HttpClient(Android) {
            MarketApplication.sslContextConfig?.let {
                engine {
                    sslManager = { httpsURLConnection ->
                        httpsURLConnection.sslSocketFactory = it.sslContext.socketFactory
                    }
                }
            }
            install(HttpTimeout) {
                connectTimeoutMillis = 8_000
                requestTimeoutMillis = 8_000
                socketTimeoutMillis = 8_000
            }
            install(HttpRedirect) {
                checkHttpMethod = false
                allowHttpsDowngrade = true
            }
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
    }

    /**
     * register.do method should call at the server.
     * Here are called from the client for the sake of simplicity of the example.
     */
    suspend fun executeRegisterOrder(
        url: String,
        request: RegisterRequest
    ): RegisterOrderResponse = withContext(Dispatchers.IO) {
        httpClient.post(url) {
            setBody(
                FormDataContent(
                    Parameters.build {
                        append("amount", request.amount)
                        append("userName", request.userName)
                        append("password", request.password)
                        append("returnUrl", request.returnUrl)
                        append("failUrl", request.failUrl)
                        append("email", request.email)
                    }
                )
            )
        }.body()
    }

    suspend fun executePaymentOrder(
        url: String,
        request: PaymentOrderRequest
    ): PaymentOrderResponse = withContext(Dispatchers.IO) {
        httpClient.post(url) {
            setBody(
                FormDataContent(
                    Parameters.build {
                        append("seToken", request.seToken)
                        append("MDORDER", request.mdOrder)
                        append("userName", request.userName)
                        append("password", request.password)
                        append("TEXT", request.text)
                        append("threeDSSDK", request.threeDSSDK.toString())
                    }
                )
            )
        }.body()
    }

    suspend fun executePaymentOrderSecondStep(
        url: String,
        request: PaymentOrderSecondStepRequest
    ): PaymentOrderSecondStepResponse = withContext(Dispatchers.IO) {
        httpClient.post(url) {
            setBody(
                FormDataContent(
                    Parameters.build {
                        append("seToken", request.seToken)
                        append("MDORDER", request.mdOrder)
                        append("userName", request.userName)
                        append("password", request.password)
                        append("TEXT", request.text)
                        append("threeDSSDK", request.threeDSSDK.toString())
                        append("threeDSSDKEncData", request.threeDSSDKEncData)
                        append("threeDSSDKEphemPubKey", request.threeDSSDKEphemPubKey)
                        append("threeDSSDKAppId", request.threeDSSDKAppId)
                        append("threeDSSDKTransId", request.threeDSSDKTransId)
                        append("threeDSServerTransId", request.threeDSServerTransId)
                        append("threeDSSDKReferenceNumber", request.threeDSSDKReferenceNumber)
                    }
                )
            )
        }.body()
    }

    /**
     * finish3dsVer2Payment.do method should call at the server.
     * Here are called from the client for the sake of simplicity of the example.
     */
    suspend fun executeFinishOrder(
        url: String,
        request: PaymentFinishOrderRequest
    ): FinishOrderResponse = withContext(Dispatchers.IO) {
            httpClient.post(url) {
                timeout {
                    connectTimeoutMillis = 4_000
                    requestTimeoutMillis = 4_000
                    socketTimeoutMillis = 4_000
                }
                setBody(FormDataContent(Parameters.build {
                        append("threeDSServerTransId", request.tDsTransId)
                        append("userName", request.userName)
                        append("password", request.password)
                    })
                )
            }.body()
        }

    suspend fun executeCheckOrderStatus(
        url: String,
        request: PaymentCheckOrderStatusRequest
    ): String = withContext(Dispatchers.IO) {
        httpClient.post(url) {
            setBody(
                FormDataContent(
                    Parameters.build {
                        append("orderId", request.orderId)
                        append("userName", request.userName)
                        append("password", request.password)
                    }
                )
            )
        }
    }.body()

    @Serializable
    data class RegisterRequest(
        val amount: String,
        val userName: String,
        val password: String,
        val returnUrl: String,
        val failUrl: String,
        val email: String
    )

    @Serializable
    data class PaymentOrderRequest(
        val seToken: String,
        val mdOrder: String,
        val userName: String,
        val password: String,
        val text: String,
        val threeDSSDK: Boolean
    )

    @Serializable
    data class PaymentOrderSecondStepRequest(
        val seToken: String,
        val mdOrder: String,
        val userName: String,
        val password: String,
        val text: String,
        val threeDSSDK: Boolean,
        val threeDSServerTransId: String,
        val threeDSSDKEncData: String,
        val threeDSSDKEphemPubKey: String,
        val threeDSSDKAppId: String,
        val threeDSSDKTransId: String,
        val threeDSSDKReferenceNumber: String
    )

    @Serializable
    data class PaymentFinishOrderRequest(
        val tDsTransId: String,
        val userName: String,
        val password: String
    )

    @Serializable
    data class PaymentCheckOrderStatusRequest(
        val orderId: String,
        val userName: String,
        val password: String
    )

    @Serializable
    data class FinishOrderResponse(
        @SerialName("redirect")
        val redirect: String,
        @SerialName("errorCode")
        val errorCode: Int,
        @SerialName("is3DSVer2")
        val is3DSVer2: Boolean
    )

    @Serializable
    data class RegisterOrderResponse(
        @SerialName("orderId")
        val orderId: String
    )

    @Serializable
    data class PaymentOrderResponse(
        @SerialName("errorCode")
        val errorCode: Int,
        @SerialName("is3DSVer2")
        val is3DSVer2: Boolean,
        @SerialName("threeDSServerTransId")
        val threeDSServerTransId: String,
        @SerialName("threeDSSDKKey")
        val threeDSSDKKey: String
    )

    @Serializable
    data class PaymentOrderSecondStepResponse(
        @SerialName("info")
        val info: String,
        @SerialName("errorCode")
        val errorCode: Int,
        @SerialName("is3DSVer2")
        val is3DSVer2: Boolean,
        @SerialName("threeDSAcsTransactionId")
        val threeDSAcsTransactionId: String,
        @SerialName("threeDSAcsRefNumber")
        val threeDSAcsRefNumber: String,
        @SerialName("threeDSAcsSignedContent")
        val threeDSAcsSignedContent: String
    )
}