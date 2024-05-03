package net.payrdr.mobile.payment.sdk.payment

import com.google.android.gms.wallet.PaymentDataRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import net.payrdr.mobile.payment.sdk.LogDebug
import net.payrdr.mobile.payment.sdk.api.PaymentApi
import net.payrdr.mobile.payment.sdk.api.PaymentApiImpl
import net.payrdr.mobile.payment.sdk.api.entity.BindingItem
import net.payrdr.mobile.payment.sdk.api.entity.FinishedPaymentInfoResponse
import net.payrdr.mobile.payment.sdk.api.entity.ProcessFormResponse
import net.payrdr.mobile.payment.sdk.api.entity.SessionStatusResponse
import net.payrdr.mobile.payment.sdk.exceptions.SDKAlreadyPaymentException
import net.payrdr.mobile.payment.sdk.exceptions.SDKDeclinedException
import net.payrdr.mobile.payment.sdk.exceptions.SDKNotConfigureException
import net.payrdr.mobile.payment.sdk.exceptions.SDKOrderNotExistException
import net.payrdr.mobile.payment.sdk.exceptions.SDKPaymentApiException
import net.payrdr.mobile.payment.sdk.exceptions.SDKTransactionException
import net.payrdr.mobile.payment.sdk.form.GooglePayConfigBuilder
import net.payrdr.mobile.payment.sdk.form.SDKException
import net.payrdr.mobile.payment.sdk.form.gpay.AllowedPaymentMethods
import net.payrdr.mobile.payment.sdk.form.gpay.GooglePayAuthMethod
import net.payrdr.mobile.payment.sdk.form.gpay.GooglePayCardNetwork
import net.payrdr.mobile.payment.sdk.form.gpay.GooglePayCheckoutOption
import net.payrdr.mobile.payment.sdk.form.gpay.GooglePayPaymentDataRequest
import net.payrdr.mobile.payment.sdk.form.gpay.GooglePayPaymentMethod
import net.payrdr.mobile.payment.sdk.form.gpay.GooglePayTotalPriceStatus
import net.payrdr.mobile.payment.sdk.form.gpay.GoogleTokenizationSpecificationType
import net.payrdr.mobile.payment.sdk.form.gpay.MerchantInfo
import net.payrdr.mobile.payment.sdk.form.gpay.PaymentMethodParameters
import net.payrdr.mobile.payment.sdk.form.gpay.TokenizationSpecification
import net.payrdr.mobile.payment.sdk.form.gpay.TokenizationSpecificationParameters
import net.payrdr.mobile.payment.sdk.form.gpay.TransactionInfo
import net.payrdr.mobile.payment.sdk.form.model.GooglePayPaymentConfig
import net.payrdr.mobile.payment.sdk.form.utils.requiredField
import net.payrdr.mobile.payment.sdk.payment.model.GooglePayProcessFormRequest
import net.payrdr.mobile.payment.sdk.payment.model.PaymentResult
import net.payrdr.mobile.payment.sdk.payment.model.ProcessFormRequest
import net.payrdr.mobile.payment.sdk.payment.model.SDKPaymentConfig
import net.payrdr.mobile.payment.sdk.payment.model.Use3DSConfig
import net.payrdr.mobile.payment.sdk.payment.model.WebChallengeParam
import net.payrdr.mobile.payment.sdk.threeds.impl.Factory
import net.payrdr.mobile.payment.sdk.threeds.spec.ChallengeParameters
import net.payrdr.mobile.payment.sdk.threeds.spec.ChallengeStatusReceiver
import net.payrdr.mobile.payment.sdk.threeds.spec.CompletionEvent
import net.payrdr.mobile.payment.sdk.threeds.spec.ProtocolErrorEvent
import net.payrdr.mobile.payment.sdk.threeds.spec.RuntimeErrorEvent
import net.payrdr.mobile.payment.sdk.threeds.spec.ThreeDS2Service
import net.payrdr.mobile.payment.sdk.threeds.spec.Transaction
import net.payrdr.mobile.payment.sdk.utils.OrderStatuses
import net.payrdr.mobile.payment.sdk.utils.containsAnyOfKeywordIgnoreCase
import java.math.BigDecimal

/**
 * A set of methods for carrying out a full payment cycle.
 *
 * @param cardFormDelegate interface describing the operation of the card data entry form.
 * @param threeDS2FormDelegate interface describing the work of the 3DS form.
 * @param activityDelegate interface describing activity methods.
 */
@Suppress("TooGenericExceptionCaught", "TooManyFunctions")
class PaymentManagerImpl(
    private val cardFormDelegate: CardFormDelegate,
    private val threeDS1FormDelegate: ThreeDS1FormDelegate,
    private val threeDS2FormDelegate: ThreeDS2FormDelegate,
    private val activityDelegate: ActivityDelegate,
    private val gPayDelegate: GPayDelegate,
) : PaymentManager {

    private val paymentScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private lateinit var mdOrder: String

    private val paymentConfig: SDKPaymentConfig by lazy {
        activityDelegate.getPaymentConfig()
    }
    private val paymentApi: PaymentApi = PaymentApiImpl(
        baseUrl = paymentConfig.baseURL
    )

    // The fields are required to create and run the 3DS Challenge Flow.
    private val factory = Factory()
    private val threeDS2Service: ThreeDS2Service by lazy {
        val service = factory.newThreeDS2Service()
        val configParams = factory.newConfigParameters()
        val uiCustomization = factory.newUiCustomization(threeDS2FormDelegate.getBaseContext())
        if (!service.isInitialized) {
            service.initialize(
                threeDS2FormDelegate.getApplicationContext(),
                configParams,
                "ru-RU",
                uiCustomization,
                paymentConfig.sslContextConfig?.sslContext,
                paymentConfig.sslContextConfig?.trustManager,
            )
        }
        service
    }

    private var transaction: Transaction? = null

    @Suppress("LongMethod")
    override fun checkout(order: String, gPayClicked: Boolean) {
        paymentScope.launchSafe {
            // start PaymentActivity
            // The first step is to call the getSessionStatus method
            // Payment configuration ->
            // 1 - Run SDK Form
            // a - New map screen
            // b - Linked cards
            // 2 - Call the payment method for a new card or linked
            // a - Method of payment with a new card
            // b - Payment method with linked card
            // 3 Call the method of processing the result
            // a - payment was successful
            // finish
            // b - Need 3DS
            // 3.b.1 - launch SDK 3DS (challenge flow)
            // c - Get an error
            // 4 - Completion with the result of the payment (return to the payment start screen).

            mdOrder = order
            val sessionStatusResponse = getSessionStatus()
            val isGPayAccepted = sessionStatusResponse.merchantOptions.contains("GOOGLEPAY")
            var gPayConfig: GooglePayPaymentConfig? = null
            if (isGPayAccepted) {
                val gPaySettings =
                    paymentApi.getPaymentSettings(login = sessionStatusResponse.merchantInfo.merchantLogin)
                gPayConfig = createGooglePayConfig(
                    mdOrder,
                    gatewayGPay = gPaySettings.gateway,
                    merchantIdGPay = gPaySettings.merchantId,
                    amount = sessionStatusResponse.amount!!,
                    currencyCodeInput = sessionStatusResponse.currencyAlphaCode!!,
                    getawayMerchantIdConfig = sessionStatusResponse.merchantInfo.merchantLogin,
                    merchantFullName = sessionStatusResponse.merchantInfo.merchantFullName,
                    isTestEnvironment = gPaySettings.environment == "TEST",
                )
            }
            if (gPayClicked) {
                gPayDelegate.openGPayForm(gPayConfig)
            } else {
                when {
                    sessionStatusResponse.remainingSecs == null -> {
                        val response = getFinishedPaymentInfo(mdOrder)
                        when {
                            response.status == null -> {
                                finishWithError(SDKOrderNotExistException(cause = null))
                            }

                            response.status.containsAnyOfKeywordIgnoreCase(OrderStatuses.payedStatues) -> {
                                finishWithError(SDKAlreadyPaymentException(cause = null))
                            }

                            response.status.containsAnyOfKeywordIgnoreCase(OrderStatuses.payedCouldNotBeCompleted) -> {
                                finishWithError(SDKDeclinedException(cause = null))
                            }

                            else -> {
                                TODO()
                            }
                        }
                    }

                    sessionStatusResponse.bindingItems.isNullOrEmpty() -> {
                        cardFormDelegate.openBottomSheet(
                            mdOrder = order,
                            bindingEnabled = sessionStatusResponse.bindingEnabled,
                            bindingCards = emptyList<BindingItem>(),
                            cvcNotRequired = sessionStatusResponse.cvcNotRequired,
                            bindingDeactivationEnabled = sessionStatusResponse.bindingDeactivationEnabled,
                            googlePayConfig = gPayConfig
                        )
                        LogDebug.logIfDebug("Creating cryptogram with New Card")
                    }

                    else -> {
                        cardFormDelegate.openBottomSheet(
                            mdOrder = order,
                            bindingEnabled = sessionStatusResponse.bindingEnabled,
                            bindingCards = sessionStatusResponse.bindingItems,
                            cvcNotRequired = sessionStatusResponse.cvcNotRequired,
                            bindingDeactivationEnabled = sessionStatusResponse.bindingDeactivationEnabled,
                            googlePayConfig = gPayConfig
                        )
                        LogDebug.logIfDebug("Creating cryptogram with Binding Card")
                    }
                }
            }
        }
    }

    internal fun processNewCard(seToken: String, mdOrder: String, holder: String, saveCard: Boolean) {
        processFormData(
            processFormRequest = ProcessFormRequest(
                seToken = seToken,
                mdOrder = mdOrder,
                holder = holder.ifBlank { DEFAULT_VALUE_CARDHOLDER },
                saveCard = saveCard,
            ),
            isBinding = false,
        )
    }

    internal fun processBindingCard(seToken: String, mdOrder: String) {
        processFormData(
            processFormRequest = ProcessFormRequest(
                seToken = seToken,
                mdOrder = mdOrder,
                holder = DEFAULT_VALUE_CARDHOLDER,
                saveCard = false,
            ),
            isBinding = true,
        )
    }

    /**
     * Start the payment process by calling the API methods.
     *
     * @param processFormRequest result of the creating a cryptogram.
     */
    private fun processFormData(processFormRequest: ProcessFormRequest, isBinding: Boolean) {
        paymentScope.launchSafe {
            val threeDSSDK = paymentConfig.use3DSConfig is Use3DSConfig.Use3DS2
            val paymentResult: ProcessFormResponse = if (isBinding) {
                paymentApi.processBindingForm(
                    cryptogramApiData = processFormRequest,
                    threeDSSDK = threeDSSDK
                )
            } else {
                paymentApi.processForm(
                    cryptogramApiData = processFormRequest,
                    threeDSSDK = threeDSSDK
                )
            }

            when {
                paymentResult.threeDSMethodURL != null -> {
                    LogDebug.logIfDebug("Merchant is not configured to be used without 3DS2SDK: $paymentResult")
                    throw SDKNotConfigureException(
                        message = "Merchant is not configured to be used without 3DS2SDK",
                        cause = null
                    )
                }

                paymentResult.is3DSVer2 -> {
                    LogDebug.logIfDebug("processForm - Payment need 3DSVer2: $paymentResult")

                    processThreeDS2Data(
                        cryptogramApiData = processFormRequest,
                        threeDSSDKKey = paymentResult.threeDSSDKKey.requiredField("threeDSSDKKey"),
                        threeDSServerTransId = paymentResult.threeDSServerTransId.requiredField("threeDSServerTransId"),
                        isBinding = isBinding
                    )
                }

                paymentResult.acsUrl != null -> {
                    LogDebug.logIfDebug("processForm - Payment need 3DSVer1: $paymentResult")
                    threeDS1FormDelegate.openWebChallenge(
                        WebChallengeParam(
                            processFormRequest.mdOrder,
                            paymentResult.acsUrl,
                            paymentResult.paReq.requiredField("paReq"),
                            paymentResult.termUrl.requiredField("termUrl"),
                        )
                    )
                }

                else -> {
                    LogDebug.logIfDebug("processForm - Payment without 3DS: $paymentResult")
                    finishWithCheckOrderStatus()
                }
            }
        }
    }

    /**
     * Start the payment process by calling the API methods for Google Pay.
     *
     * @param cryptogramGPayApiData result of the creating a cryptogram by GPay lib.
     */

    @Suppress("ComplexCondition")
    fun gPayProcessForm(cryptogramGPayApiData: GooglePayProcessFormRequest) {
        paymentScope.launchSafe {
            val paymentResult = paymentApi.gPayProcessForm(
                cryptogramGPayApiData = cryptogramGPayApiData
            )
            LogDebug.logIfDebug("GPay Payment First Response $paymentResult")

            if (paymentResult.data != null && !paymentResult.data.errorTypeName.isNullOrBlank()) {
                finishWithError(
                    SDKException(
                        message = paymentResult.data.errorTypeName,
                        cause = null
                    )
                )
            } else if (paymentResult.data != null && !paymentResult.data.acsUrl.isNullOrBlank()
                && !paymentResult.data.paReq.isNullOrBlank()
                && !paymentResult.data.termUrl.isNullOrBlank()
            ) {
                val webChallengeParam = WebChallengeParam(
                    mdOrder = paymentResult.data.orderId,
                    acsUrl = paymentResult.data.acsUrl,
                    paReq = paymentResult.data.paReq,
                    termUrl = paymentResult.data.termUrl,
                )
                threeDS1FormDelegate.openWebChallenge(
                    webChallengeParam = webChallengeParam,
                )
            } else if (paymentResult.data != null && paymentResult.data.threeDSAcsRefNumber != null
                && paymentResult.data.threeDSAcsTransactionId != null
                && paymentResult.data.threeDSAcsSignedContent != null
                && paymentResult.data.threeDSServerTransId != null
            ) {
                threeDS2FormDelegate.openChallengeFlow(
                    transaction,
                    createChallengeParameters(
                        threeDSAcsTransactionId = paymentResult.data.threeDSAcsTransactionId,
                        threeDSAcsRefNumber = paymentResult.data.threeDSAcsRefNumber,
                        threeDSAcsSignedContent = paymentResult.data.threeDSAcsSignedContent,
                        threeDSServerTransId = paymentResult.data.threeDSServerTransId,
                    ),
                    createChallengeStatusReceiver(
                        paymentResult.data.threeDSServerTransId,
                        threeDS2Service,
                    )
                )
            } else {
                finishWithCheckOrderStatus()
            }
        }
    }

    /**
     * Unbind card on server.
     *
     * @param bindingId identifier of binding card.
     * @return true if success, otherwise false
     */
    internal fun unbindCard(bindingId: String) {
        paymentScope.launchSafe {
            paymentApi.unbindCardAnonymous(bindingId, mdOrder)
        }
    }

    internal fun onDestroy() {
        paymentScope.cancel()
    }

    internal fun finishWithCheckOrderStatus() {
        threeDS2FormDelegate.cleanup(transaction, threeDS2Service)
        paymentScope.launchSafe {
            val orderStatus = getSessionStatus()
            val paymentFinishedInfo = getFinishedPaymentInfo(mdOrder)
            LogDebug.logIfDebug("getSessionStatus - Remaining sec ${orderStatus.remainingSecs}")
            val paymentDataResponse = PaymentResult(
                mdOrder = mdOrder,
                isSuccess = paymentFinishedInfo.status.containsAnyOfKeywordIgnoreCase(OrderStatuses.payedStatues),
                exception = null,
            )
            activityDelegate.finishActivityWithResult(paymentDataResponse)
        }
    }

    /**
     * Canceling the process of creating a cryptogram.
     */
    internal fun finishWithError(ex: Throwable) {
        threeDS2FormDelegate.cleanup(transaction, threeDS2Service)
        paymentScope.launch {
            try {
                val orderStatus = getSessionStatus()
                val paymentFinishedInfo = getFinishedPaymentInfo(mdOrder)
                LogDebug.logIfDebug("getSessionStatus - Remaining sec ${orderStatus.remainingSecs}")
                val paymentDataResponse = PaymentResult(
                    mdOrder = mdOrder,
                    isSuccess = paymentFinishedInfo.status.containsAnyOfKeywordIgnoreCase(OrderStatuses.payedStatues),
                    exception = if (ex is SDKException) {
                        ex
                    } else {
                        SDKException(message = "", cause = ex)
                    }
                )
                activityDelegate.finishActivityWithResult(paymentDataResponse)
            } catch (ex: Exception) {
                activityDelegate.finishActivityWithResult(
                    PaymentResult(
                        mdOrder = mdOrder,
                        isSuccess = false,
                        exception = if (ex is SDKException) {
                            ex
                        } else {
                            SDKException(message = "", cause = ex)
                        }
                    )
                )
            }
        }
    }

    private suspend fun getFinishedPaymentInfo(mdOrder: String): FinishedPaymentInfoResponse =
        paymentApi.getFinishedPaymentInfo(mdOrder)

    @Suppress("ComplexCondition", "LongMethod")
    private suspend fun processThreeDS2Data(
        cryptogramApiData: ProcessFormRequest,
        threeDSSDKKey: String,
        threeDSServerTransId: String,
        isBinding: Boolean
    ) {
        val dsRoot = when (val use3DSConfig = paymentConfig.use3DSConfig) {
            Use3DSConfig.Use3DS1 -> throw SDKException(
                "Please configure Use3DSConfig.Use3DS2 with SDKPaymentConfig to use 3DS2."
            )

            is Use3DSConfig.Use3DS2 -> use3DSConfig.dsRoot
        }

        transaction?.close() // Close the previous transaction if there was one.
        transaction = threeDS2Service.createTransaction(
            "A000000658",
            threeDSSDKKey,
            "2.2.0",
            dsRoot
        )

        // Available data, to be sent to the payment gateway.
        val authRequestParams = transaction!!.authenticationRequestParameters!!
        val encryptedDeviceInfo: String = authRequestParams.deviceData
        val sdkTransactionID: String = authRequestParams.sdkTransactionID
        val sdkAppId: String = authRequestParams.sdkAppID
        val sdkEphmeralPublicKey: String = authRequestParams.sdkEphemeralPublicKey
        val sdkReferenceNumber: String = authRequestParams.sdkReferenceNumber

        val paymentThreeDsInfo = PaymentApiImpl.PaymentThreeDSInfo(
            threeDSSDK = true,
            threeDSServerTransId = threeDSServerTransId,
            threeDSSDKEncData = encryptedDeviceInfo,
            threeDSSDKEphemPubKey = sdkEphmeralPublicKey,
            threeDSSDKAppId = sdkAppId,
            threeDSSDKTransId = sdkTransactionID,
            threeDSSDKReferenceNumber = sdkReferenceNumber
        )
        val paymentResult = if (isBinding) {
            paymentApi.processBindingFormSecond(
                cryptogramApiData = cryptogramApiData,
                threeDSParams = paymentThreeDsInfo
            )
        } else {
            paymentApi.processFormSecond(
                cryptogramApiData = cryptogramApiData,
                threeDSParams = paymentThreeDsInfo
            )
        }

        if (paymentResult.redirect?.startsWith("sdk://done") == true) {
            finishWithCheckOrderStatus()
        } else if (!paymentResult.errorTypeName.isNullOrBlank()) {
            finishWithError(
                SDKException(
                    message = paymentResult.errorTypeName,
                    cause = null
                )
            )
        } else if (paymentResult.threeDSAcsRefNumber != null
            && paymentResult.threeDSAcsTransactionId != null
            && paymentResult.threeDSAcsSignedContent != null
            && paymentResult.threeDSServerTransId != null
        ) {
            //
            threeDS2FormDelegate.openChallengeFlow(
                transaction,
                createChallengeParameters(
                    threeDSAcsTransactionId = paymentResult.threeDSAcsTransactionId,
                    threeDSAcsRefNumber = paymentResult.threeDSAcsRefNumber,
                    threeDSAcsSignedContent = paymentResult.threeDSAcsSignedContent,
                    threeDSServerTransId = threeDSServerTransId,
                ),
                createChallengeStatusReceiver(threeDSServerTransId, threeDS2Service)
            )
        } else {
            finishWithError(
                SDKPaymentApiException(
                    message = "Wrong api response from second form $paymentResult",
                    cause = null
                )
            )
        }
    }

    private suspend fun getSessionStatus(): SessionStatusResponse = paymentApi.getSessionStatus(
        mdOrder = mdOrder
    )

    private fun createChallengeParameters(
        threeDSAcsTransactionId: String,
        threeDSAcsRefNumber: String,
        threeDSAcsSignedContent: String,
        threeDSServerTransId: String,
    ): ChallengeParameters {
        val challengeParameters = factory.newChallengeParameters()

        // Parameters for starting Challenge Flow.
        challengeParameters.acsTransactionID = threeDSAcsTransactionId
        challengeParameters.acsRefNumber = threeDSAcsRefNumber
        challengeParameters.acsSignedContent = threeDSAcsSignedContent
        challengeParameters.set3DSServerTransactionID(threeDSServerTransId)

        return challengeParameters
    }

    // Listener to handle the Challenge Flow execution process.
    private suspend fun createChallengeStatusReceiver(
        threeDSServerTransId: String,
        threeDS2Service: ThreeDS2Service,
    ): ChallengeStatusReceiver = object : ChallengeStatusReceiver {
        override fun cancelled() {
            LogDebug.logIfDebug("cancelled")
            threeDS2FormDelegate.cleanup(transaction, threeDS2Service)
        }

        override fun protocolError(protocolErrorEvent: ProtocolErrorEvent) {
            LogDebug.logIfDebug("protocolError $protocolErrorEvent")
            threeDS2FormDelegate.cleanup(transaction, threeDS2Service)
        }

        override fun runtimeError(runtimeErrorEvent: RuntimeErrorEvent) {
            LogDebug.logIfDebug("runtimeError $runtimeErrorEvent")
            threeDS2FormDelegate.cleanup(transaction, threeDS2Service)
        }

        override fun completed(completionEvent: CompletionEvent) {
            LogDebug.logIfDebug("completed $completionEvent")
            threeDS2FormDelegate.cleanup(transaction, threeDS2Service)
            when (completionEvent.transactionStatus) {
                "Y" -> {
                    paymentScope.launchSafe {
                        paymentApi.finish3dsVer2PaymentAnonymous(threeDSServerTransId)
                        finishWithCheckOrderStatus()
                    }
                }

                "N" -> {
                    paymentScope.launchSafe {
                        finishWithError(SDKTransactionException("Transaction not confirmed", cause = null))
                    }
                }
            }
        }

        override fun timedout() {
            LogDebug.logIfDebug("timedout")
            threeDS2FormDelegate.cleanup(transaction, threeDS2Service)
        }
    }

    @Suppress("LongParameterList")
    private fun createGooglePayConfig(
        mdOrder: String,
        gatewayGPay: String,
        merchantIdGPay: String,
        amount: String,
        currencyCodeInput: String,
        getawayMerchantIdConfig: String,
        merchantFullName: String,
        isTestEnvironment: Boolean,
    ): GooglePayPaymentConfig {
        val paymentData = GooglePayPaymentDataRequest.paymentDataRequestCreate {
            allowedPaymentMethods = AllowedPaymentMethods.allowedPaymentMethodsCreate {
                method {
                    type = GooglePayPaymentMethod.CARD
                    parameters = PaymentMethodParameters.paymentMethodParametersCreate {
                        allowedAuthMethods = mutableSetOf(
                            GooglePayAuthMethod.PAN_ONLY,
                            GooglePayAuthMethod.CRYPTOGRAM_3DS
                        )
                        allowedCardNetworks =
                            mutableSetOf(
                                GooglePayCardNetwork.AMEX,
                                GooglePayCardNetwork.DISCOVER,
                                GooglePayCardNetwork.INTERAC,
                                GooglePayCardNetwork.JCB,
                                GooglePayCardNetwork.MASTERCARD,
                                GooglePayCardNetwork.VISA
                            )
                    }
                    tokenizationSpecification =
                        TokenizationSpecification.tokenizationSpecificationCreate {
                            type = GoogleTokenizationSpecificationType.PAYMENT_GATEWAY
                            parameters =
                                TokenizationSpecificationParameters.tokenizationSpecificationParametersCreate {
                                    gateway = gatewayGPay
                                    gatewayMerchantId = getawayMerchantIdConfig
                                }
                        }
                }
            }
            transactionInfo = TransactionInfo.transactionInfoCreate {
                totalPrice = BigDecimal.valueOf(amount.trim().split(" ")[0].toDoubleOrNull() ?: 0.0)
                totalPriceStatus = GooglePayTotalPriceStatus.FINAL
                countryCode = "RU"
                currencyCode = currencyCodeInput
                checkoutOption = GooglePayCheckoutOption.COMPLETE_IMMEDIATE_PURCHASE
            }
            merchantInfo = MerchantInfo.merchantInfoCreate {
                merchantName = merchantFullName
                merchantId = merchantIdGPay
            }
        }.toJson().toString()

        return GooglePayConfigBuilder(
            order = mdOrder,
            paymentData = PaymentDataRequest.fromJson(paymentData),
        ).testEnvironment(isTestEnvironment)
            .build()
    }

    private inline fun CoroutineScope.launchSafe(
        crossinline block: suspend CoroutineScope.() -> Unit,
    ): Job {
        return launch {
            try {
                block()
            } catch (exception: Exception) {
                finishWithError(exception)
            }
        }
    }

    companion object {
        private const val DEFAULT_VALUE_CARDHOLDER: String = "CARDHOLDER"
    }
}
