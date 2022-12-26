package net.payrdr.mobile.payment.sample.kotlin.threeds

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_three_d_s.amount
import kotlinx.android.synthetic.main.activity_three_d_s.baseUrl
import kotlinx.android.synthetic.main.activity_three_d_s.directoryServerId
import kotlinx.android.synthetic.main.activity_three_d_s.dsRootBase64
import kotlinx.android.synthetic.main.activity_three_d_s.email
import kotlinx.android.synthetic.main.activity_three_d_s.failUrl
import kotlinx.android.synthetic.main.activity_three_d_s.password
import kotlinx.android.synthetic.main.activity_three_d_s.returnUrl
import kotlinx.android.synthetic.main.activity_three_d_s.text
import kotlinx.android.synthetic.main.activity_three_d_s.threeDSCheckout
import kotlinx.android.synthetic.main.activity_three_d_s.userName
import net.payrdr.mobile.payment.sample.kotlin.MarketApplication
import net.payrdr.mobile.payment.sample.kotlin.R
import net.payrdr.mobile.payment.sample.kotlin.helpers.launchGlobalScope
import net.payrdr.mobile.payment.sample.kotlin.helpers.log
import net.payrdr.mobile.payment.sample.kotlin.threeds.ThreeDSGatewayApi.PaymentCheckOrderStatusRequest
import net.payrdr.mobile.payment.sample.kotlin.threeds.ThreeDSGatewayApi.PaymentFinishOrderRequest
import net.payrdr.mobile.payment.sample.kotlin.threeds.ThreeDSGatewayApi.PaymentOrderRequest
import net.payrdr.mobile.payment.sdk.SDKPayment
import net.payrdr.mobile.payment.sdk.form.PaymentConfigBuilder
import net.payrdr.mobile.payment.sdk.form.ResultCryptogramCallback
import net.payrdr.mobile.payment.sdk.form.SDKConfigBuilder
import net.payrdr.mobile.payment.sdk.form.SDKException
import net.payrdr.mobile.payment.sdk.form.SDKForms
import net.payrdr.mobile.payment.sdk.form.model.CryptogramData
import net.payrdr.mobile.payment.sdk.form.model.PaymentInfoNewCard
import net.payrdr.mobile.payment.sdk.threeds.impl.Factory
import net.payrdr.mobile.payment.sdk.threeds.spec.ChallengeStatusReceiver
import net.payrdr.mobile.payment.sdk.threeds.spec.CompletionEvent
import net.payrdr.mobile.payment.sdk.threeds.spec.ProtocolErrorEvent
import net.payrdr.mobile.payment.sdk.threeds.spec.RuntimeErrorEvent
import net.payrdr.mobile.payment.sdk.threeds.spec.ThreeDS2Service
import net.payrdr.mobile.payment.sdk.threeds.spec.Transaction

class ThreeDSActivity : AppCompatActivity() {

    // Api for test payment.
    private val api = ThreeDSGatewayApi()

    // The fields are required to create and run the 3DS Challenge Flow.
    private val factory = Factory()
    private lateinit var threeDS2Service: ThreeDS2Service
    private var transaction: Transaction? = null

    // Customizable parameters of the example from the UI.
    private val argBaseUrl: String
        get() = baseUrl.text.toString()
    private val argUserName: String
        get() = userName.text.toString()
    private val argPassword: String
        get() = password.text.toString()
    private val argText: String
        get() = text.text.toString()
    private val argAmount: String
        get() = amount.text.toString()
    private val argEmail: String
        get() = email.text.toString()
    private val argReturnUrl: String
        get() = returnUrl.text.toString()
    private val argFailUrl: String
        get() = failUrl.text.toString()
    private val argDsRoot: String
        get() = dsRootBase64.text.toString().replace("\n", "").replace(" ", "")
    private val argDirectoryServerId: String
        get() = directoryServerId.text.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_three_d_s)
        SDKForms.init(
            SDKConfigBuilder()
                .keyProviderUrl("$argBaseUrl/se/keys.do")
                .build()
        )

        threeDSCheckout.setOnClickListener {
            registerOrder()
        }
    }

    /**
     * Registering an order to start the payment process.
     */
    private fun registerOrder() = launchGlobalScope {
        // Get the order ID. .
        val registerResponse = api.executeRegisterOrder(
            url = "$argBaseUrl/rest/register.do",
            request = ThreeDSGatewayApi.RegisterRequest(
                amount = argAmount,
                userName = argUserName,
                password = argPassword,
                returnUrl = argReturnUrl,
                failUrl = argFailUrl,
                email = argEmail
            )
        )
        // Form parameters for launching the screen for entering card data and generating seToken by
        // complete filling of data.
        val paymentConfig = PaymentConfigBuilder(registerResponse.orderId)
            .build()

        // Calling the payment screen (filling in the card data).
        SDKForms.cryptogram(this@ThreeDSActivity, paymentConfig)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Processing the result of the formation of seToken.
        SDKForms.handleCryptogramResult(requestCode, data, object :
            ResultCryptogramCallback<CryptogramData> {

            override fun onSuccess(result: CryptogramData) {
                // The result of the formation of seToken (cryptogram).
                when {
                    result.status.isSucceeded() -> {
                        val info = result.info
                        if (info is PaymentInfoNewCard) {
                            executeThreeDSChallengeFlow(
                                seToken = result.seToken,
                                mdOrder = info.order
                            )
                        }
                        log("$result")
                    }
                    result.status.isCanceled() -> {
                        log("canceled")
                    }
                }
            }

            override fun onFail(e: SDKException) {
                // An error has occurred.
                log("${e.message} ${e.cause}")
            }
        })
    }

    /**
     * Launching the Challenge Flow process.
     *
     * @param seToken token (cryptogram) with order ID and payment card information.
     * @param mdOrder order id.
     */
    private fun executeThreeDSChallengeFlow(
        seToken: String,
        mdOrder: String
    ) = launchGlobalScope {
        threeDS2Service = factory.newThreeDS2Service()
        val configParams = factory.newConfigParameters()
        val uiCustomization = factory.newUiCustomization(baseContext)
        threeDS2Service.initialize(
            this@ThreeDSActivity,
            configParams,
            "en-US",
            uiCustomization
        )

        // Start of payment for the order. Getting the transaction ID of the 3DS server.
        val paymentOrderResponse = api.executePaymentOrder(
            url = "$argBaseUrl/rest/paymentorder.do",
            request = PaymentOrderRequest(
                seToken = seToken,
                mdOrder = mdOrder,
                userName = argUserName,
                password = argPassword,
                text = argText,
                threeDSSDK = true
            )
        )

        transaction?.close() // Close the previous transaction if there was one.

        // Creation of a transaction with encryption deviceInfo with the transmitted EC key.
        val ecPem: String = paymentOrderResponse.threeDSSDKKey

        transaction = threeDS2Service.createTransaction(
            argDirectoryServerId,
            ecPem,
            "2.1.0",
            argDsRoot
        )

        // Available data, to be sent to the payment gateway.
        val authRequestParams = transaction!!.authenticationRequestParameters!!
        val encryptedDeviceInfo: String = authRequestParams.deviceData
        val sdkTransactionID: String = authRequestParams.sdkTransactionID
        val sdkAppId: String = authRequestParams.sdkAppID
        val sdkEphmeralPublicKey: String = authRequestParams.sdkEphemeralPublicKey
        val sdkReferenceNumber: String = authRequestParams.sdkReferenceNumber


        // Get the necessary information to launch Challenge Flow (acsSignedContent,
        // acsTransactionId, acsRefNumber).
        val paymentOrderSecondStepResponse = api.executePaymentOrderSecondStep(
            url = "$argBaseUrl/rest/paymentorder.do",
            request = ThreeDSGatewayApi.PaymentOrderSecondStepRequest(
                seToken = seToken,
                mdOrder = mdOrder,
                userName = argUserName,
                password = argPassword,
                text = argText,
                threeDSSDK = true,
                threeDSServerTransId = paymentOrderResponse.threeDSServerTransId,
                threeDSSDKEncData = encryptedDeviceInfo,
                threeDSSDKEphemPubKey = sdkEphmeralPublicKey,
                threeDSSDKAppId = sdkAppId,
                threeDSSDKTransId = sdkTransactionID,
                threeDSSDKReferenceNumber = sdkReferenceNumber
            )
        )

        val challengeParameters = factory.newChallengeParameters()

        // Parameters for starting Challenge Flow.
        challengeParameters.acsTransactionID =
            paymentOrderSecondStepResponse.threeDSAcsTransactionId
        challengeParameters.acsRefNumber = paymentOrderSecondStepResponse.threeDSAcsRefNumber
        challengeParameters.acsSignedContent =
            paymentOrderSecondStepResponse.threeDSAcsSignedContent
        challengeParameters.set3DSServerTransactionID(paymentOrderResponse.threeDSServerTransId)

        // Listener to handle the Challenge Flow execution process.
        val challengeStatusReceiver: ChallengeStatusReceiver = object : ChallengeStatusReceiver {
            override fun cancelled() {
                log("cancelled")
                cleanup()
            }

            override fun protocolError(protocolErrorEvent: ProtocolErrorEvent) {
                log("protocolError $protocolErrorEvent")
                cleanup()
            }

            override fun runtimeError(runtimeErrorEvent: RuntimeErrorEvent) {
                log("runtimeError $runtimeErrorEvent")
                cleanup()
            }

            override fun completed(completionEvent: CompletionEvent) {
                log("completed $completionEvent")
                cleanup()
                if (completionEvent.transactionStatus == "Y") {
                    launchGlobalScope {
                        finishOrder(tDsTransId = paymentOrderResponse.threeDSServerTransId).join()
                        checkOrderStatus(orderId = mdOrder)
                    }
                }
            }

            override fun timedout() {
                log("timedout")
                cleanup()
            }
        }
        val timeOut = 5

        // Starting Challenge Flow.
        transaction!!.doChallenge(
            this@ThreeDSActivity,
            challengeParameters,
            challengeStatusReceiver,
            timeOut,
        )
    }

    /**
     * Completion of the payment process.
     *
     * @param tDsTransId ID of the transaction on the 3DS server.
     */
    private fun finishOrder(tDsTransId: String) = launchGlobalScope {
        api.executeFinishOrder(
            url = "$argBaseUrl/rest/finish3dsVer2Payment.do",
            request = PaymentFinishOrderRequest(
                tDsTransId = tDsTransId,
                userName = argUserName,
                password = argPassword
            )
        )
    }

    /**
     * Checking the status of the order.
     *
     * @param orderId order ID.
     */
    private fun checkOrderStatus(
        orderId: String
    ) = launchGlobalScope {
        val status = api.executeCheckOrderStatus(
            url = "$argBaseUrl/rest/getOrderStatusExtended.do",
            request = PaymentCheckOrderStatusRequest(
                orderId = orderId,
                userName = argUserName,
                password = argPassword
            )
        )
        log(status)
    }

    private fun cleanup() {
        transaction?.close()
        threeDS2Service.cleanup(this@ThreeDSActivity)
    }
}
