package net.payrdr.mobile.payment.sdk.payment

import com.google.android.gms.wallet.PaymentDataRequest
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.payrdr.mobile.payment.sdk.LogDebug
import net.payrdr.mobile.payment.sdk.api.PaymentApi
import net.payrdr.mobile.payment.sdk.api.PaymentApiImpl
import net.payrdr.mobile.payment.sdk.api.entity.BindingItem
import net.payrdr.mobile.payment.sdk.api.entity.FinishedPaymentInfoResponse
import net.payrdr.mobile.payment.sdk.api.entity.ProcessFormResponse
import net.payrdr.mobile.payment.sdk.api.entity.ProcessFormSecondResponse
import net.payrdr.mobile.payment.sdk.api.entity.SessionStatusResponse
import net.payrdr.mobile.payment.sdk.exceptions.SDKAlreadyPaymentException
import net.payrdr.mobile.payment.sdk.exceptions.SDKDeclinedException
import net.payrdr.mobile.payment.sdk.exceptions.SDKNotConfigureException
import net.payrdr.mobile.payment.sdk.exceptions.SDKOrderNotExistException
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
import net.payrdr.mobile.payment.sdk.payment.model.CryptogramApiData
import net.payrdr.mobile.payment.sdk.payment.model.CryptogramGPayApiData
import net.payrdr.mobile.payment.sdk.payment.model.GPayDelegate
import net.payrdr.mobile.payment.sdk.payment.model.PaymentData
import net.payrdr.mobile.payment.sdk.payment.model.WebChallengeParam
import net.payrdr.mobile.payment.sdk.threeds.impl.Factory
import net.payrdr.mobile.payment.sdk.threeds.spec.ChallengeParameters
import net.payrdr.mobile.payment.sdk.threeds.spec.ChallengeStatusReceiver
import net.payrdr.mobile.payment.sdk.threeds.spec.CompletionEvent
import net.payrdr.mobile.payment.sdk.threeds.spec.ProtocolErrorEvent
import net.payrdr.mobile.payment.sdk.threeds.spec.RuntimeErrorEvent
import net.payrdr.mobile.payment.sdk.threeds.spec.ThreeDS2Service
import net.payrdr.mobile.payment.sdk.threeds.spec.Transaction
import net.payrdr.mobile.payment.sdk.utils.OrderStatus
import net.payrdr.mobile.payment.sdk.utils.containsAnyOfKeywordIgnoreCase
import java.math.BigDecimal

/**
 * A set of methods for carrying out a full payment cycle.
 *
 * @param cardFormDelegate interface describing the operation of the card data entry form.
 * @param threeDSFormDelegate interface describing the work of the 3DS form.
 * @param activityDelegate interface describing activity methods.
 */
@Suppress("TooGenericExceptionCaught", "TooManyFunctions")
class PaymentManagerImpl(
    private val cardFormDelegate: CardFormDelegate,
    private val threeDSFormDelegate: ThreeDSFormDelegate,
    private val activityDelegate: ActivityDelegate,
    private val gPayDelegate: GPayDelegate,
    private val paymentScope: CoroutineScope
) : PaymentManager {
    private lateinit var mdOrder: String
    private val dsRoot: String = activityDelegate.getPaymentConfig().dsRoot
    private val paymentApi: PaymentApi = PaymentApiImpl(
        baseUrl = activityDelegate.getPaymentConfig().baseURL
    )
    private val errorHandler = CoroutineExceptionHandler { _, exception ->
        LogDebug.logIfDebug("CoroutineExceptionHandler got $exception")
        finishWithError(exception)
    }

    // The fields are required to create and run the 3DS Challenge Flow.
    private val factory = Factory()
    private lateinit var threeDS2Service: ThreeDS2Service
    private var transaction: Transaction? = null

    @Suppress("LongMethod")
    override suspend fun checkout(order: String, gPayClicked: Boolean) {
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
            val gPaySettings = paymentApi.getPaymentSettings(login = sessionStatusResponse.merchantInfo.merchantLogin)
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
                    val successPaymentStatusList =
                        listOf(OrderStatus.DEPOSITED.name, OrderStatus.APPROVED.name)
                    when {
                        response.status == null -> {
                            finishWithError(SDKOrderNotExistException(cause = null))
                        }
                        response.status.containsAnyOfKeywordIgnoreCase(successPaymentStatusList) -> {
                            finishWithError(SDKAlreadyPaymentException(cause = null))
                        }
                        response.status.containsAnyOfKeywordIgnoreCase(listOf(OrderStatus.DECLINED.name)) -> {
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

    override suspend fun checkOrderStatus() {
        val orderStatus = getSessionStatus()

        val paymentFinishedInfo = getFinishedPaymentInfo(mdOrder)
        LogDebug.logIfDebug("getSessionStatus - Remaining sec ${orderStatus.remainingSecs}")
        val paymentDataResponse = PaymentData(
            mdOrder = mdOrder,
            status = paymentFinishedInfo.status
        )
        activityDelegate.finishActivityWithResult(paymentDataResponse)
    }

    /**
     * Start the payment process by calling the API methods.
     *
     * @param cryptogramApiData result of the creating a cryptogram.
     */
    suspend fun processFormData(cryptogramApiData: CryptogramApiData, isBinding: Boolean) {
        val paymentResult: ProcessFormResponse = if (isBinding) {
            paymentApi.processBindingForm(
                cryptogramApiData = cryptogramApiData,
                threeDSSDK = activityDelegate.get3DSOption()
            )
        } else {
            paymentApi.processForm(
                cryptogramApiData = cryptogramApiData,
                threeDSSDK = activityDelegate.get3DSOption()
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
                processThreeDSData(
                    cryptogramApiData = cryptogramApiData,
                    processFormResponse = paymentResult,
                    isBinding = isBinding
                )
            }
            paymentResult.acsUrl != null -> {
                LogDebug.logIfDebug("processForm - Payment need 3DSVer1: $paymentResult")
                threeDSFormDelegate.openWebChallenge(
                    WebChallengeParam(
                        cryptogramApiData.mdOrder,
                        paymentResult.acsUrl,
                        paymentResult.paReq!!,
                        paymentResult.termUrl!!,
                    )
                )
            }
            else -> {
                LogDebug.logIfDebug("processForm - Payment without 3DS: $paymentResult")
                checkOrderStatus()
            }
        }
    }

    /**
     * Start the payment process by calling the API methods for Google Pay.
     *
     * @param cryptogramGPayApiData result of the creating a cryptogram by GPay lib.
     */

    @Suppress("ComplexCondition")
    suspend fun gPayProcessForm(cryptogramGPayApiData: CryptogramGPayApiData) {
        val paymentResult = paymentApi.gPayProcessForm(
            cryptogramGPayApiData = cryptogramGPayApiData
        )
        LogDebug.logIfDebug("GPay Payment First Response $paymentResult")

        if (paymentResult.data != null && !paymentResult.data.acsUrl.isNullOrBlank()
            && !paymentResult.data.paReq.isNullOrBlank()
            && !paymentResult.data.termUrl.isNullOrBlank()
        ) {
            val webChallengeParam = WebChallengeParam(
                mdOrder = paymentResult.data.orderId,
                acsUrl = paymentResult.data.acsUrl,
                paReq = paymentResult.data.paReq,
                termUrl = paymentResult.data.termUrl,
            )
            threeDSFormDelegate.openWebChallenge(
                webChallengeParam = webChallengeParam,
            )
        } else {
            checkOrderStatus()
        }
    }

    /**
     * Unbind card on server.
     *
     * @param bindingId identifier of binding card.
     * @return true if success, otherwise false
     */
    suspend fun unbindCard(bindingId: String): Boolean =
        paymentApi.unbindCardAnonymous(bindingId, mdOrder).isSuccess()

    /**
     * Canceling the process of creating a cryptogram.
     */
    fun <T> finishWithError(exception: T) {
        activityDelegate.finishActivityWithError(
            exception as SDKException
        )
    }

    private suspend fun getFinishedPaymentInfo(mdOrder: String): FinishedPaymentInfoResponse =
        paymentApi.getFinishedPaymentInfo(mdOrder)

    private suspend fun processThreeDSData(
        cryptogramApiData: CryptogramApiData,
        processFormResponse: ProcessFormResponse,
        isBinding: Boolean
    ) {
        threeDS2Service = factory.newThreeDS2Service()
        threeDSFormDelegate.initThreeDS2Service(threeDS2Service, factory)

        transaction?.close() // Close the previous transaction if there was one.
        transaction = threeDS2Service.createTransaction(
            "A000000658",
            processFormResponse.threeDSSDKKey!!,
            "2.1.0",
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
            threeDSServerTransId = processFormResponse.threeDSServerTransId!!,
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

        threeDSFormDelegate.openChallengeFlow(
            transaction,
            createChallengeParameters(paymentResult, processFormResponse),
            createChallengeStatusReceiver(processFormResponse)
        )
    }

    private suspend fun getSessionStatus(): SessionStatusResponse = paymentApi.getSessionStatus(
        mdOrder = mdOrder
    )

    private fun createChallengeParameters(
        paymentResult: ProcessFormSecondResponse,
        processFormResponse: ProcessFormResponse
    ): ChallengeParameters {
        val challengeParameters = factory.newChallengeParameters()

        LogDebug.logIfDebug(paymentResult.toString())
        // Parameters for starting Challenge Flow.
        challengeParameters.acsTransactionID = paymentResult.threeDSAcsTransactionId
        challengeParameters.acsRefNumber = paymentResult.threeDSAcsRefNumber
        challengeParameters.acsSignedContent = paymentResult.threeDSAcsSignedContent
        challengeParameters.set3DSServerTransactionID(processFormResponse.threeDSServerTransId)

        return challengeParameters
    }

    // Listener to handle the Challenge Flow execution process.
    private suspend fun createChallengeStatusReceiver(
        processFormResponse: ProcessFormResponse
    ): ChallengeStatusReceiver = object : ChallengeStatusReceiver {
        override fun cancelled() {
            LogDebug.logIfDebug("cancelled")
            threeDSFormDelegate.cleanup(transaction, threeDS2Service)
        }

        override fun protocolError(protocolErrorEvent: ProtocolErrorEvent) {
            LogDebug.logIfDebug("protocolError $protocolErrorEvent")
            threeDSFormDelegate.cleanup(transaction, threeDS2Service)
        }

        override fun runtimeError(runtimeErrorEvent: RuntimeErrorEvent) {
            LogDebug.logIfDebug("runtimeError $runtimeErrorEvent")
            threeDSFormDelegate.cleanup(transaction, threeDS2Service)
        }

        override fun completed(completionEvent: CompletionEvent) {
            LogDebug.logIfDebug("completed $completionEvent")
            threeDSFormDelegate.cleanup(transaction, threeDS2Service)
            when (completionEvent.transactionStatus) {
                "Y" -> {
                    paymentScope.launch(errorHandler) {
                        paymentApi.finish3dsVer2PaymentAnonymous(processFormResponse.threeDSServerTransId!!)
                        checkOrderStatus()
                    }
                }
                "N" -> {
                    paymentScope.launch(errorHandler) {
                        checkOrderStatus()
                    }
                }
            }
        }

        override fun timedout() {
            LogDebug.logIfDebug("timedout")
            threeDSFormDelegate.cleanup(transaction, threeDS2Service)
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
}
