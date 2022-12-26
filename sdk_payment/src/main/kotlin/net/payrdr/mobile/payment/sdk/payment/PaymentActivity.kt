package net.payrdr.mobile.payment.sdk.payment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import net.payrdr.mobile.payment.sdk.Constants.INTENT_EXTRA_ERROR
import net.payrdr.mobile.payment.sdk.Constants.INTENT_EXTRA_RESULT
import net.payrdr.mobile.payment.sdk.Constants.IS_GOOGLE_PAY
import net.payrdr.mobile.payment.sdk.Constants.MDORDER
import net.payrdr.mobile.payment.sdk.Constants.REQUEST_CODE_3DS
import net.payrdr.mobile.payment.sdk.Constants.REQUEST_CODE_CRYPTOGRAM
import net.payrdr.mobile.payment.sdk.Constants.TIMEOUT_THREE_DS
import net.payrdr.mobile.payment.sdk.LogDebug
import net.payrdr.mobile.payment.sdk.R
import net.payrdr.mobile.payment.sdk.SDKPayment
import net.payrdr.mobile.payment.sdk.api.entity.BindingItem
import net.payrdr.mobile.payment.sdk.core.model.ExpiryDate
import net.payrdr.mobile.payment.sdk.exceptions.SDKCryptogramException
import net.payrdr.mobile.payment.sdk.form.PaymentConfigBuilder
import net.payrdr.mobile.payment.sdk.form.ResultCryptogramCallback
import net.payrdr.mobile.payment.sdk.form.SDKException
import net.payrdr.mobile.payment.sdk.form.SDKForms
import net.payrdr.mobile.payment.sdk.form.model.Card
import net.payrdr.mobile.payment.sdk.form.model.CardDeleteOptions
import net.payrdr.mobile.payment.sdk.form.model.CardSaveOptions
import net.payrdr.mobile.payment.sdk.form.model.CryptogramData
import net.payrdr.mobile.payment.sdk.form.model.GooglePayPaymentConfig
import net.payrdr.mobile.payment.sdk.form.model.PaymentInfoBindCard
import net.payrdr.mobile.payment.sdk.form.model.PaymentInfoGooglePay
import net.payrdr.mobile.payment.sdk.form.model.PaymentInfoNewCard
import net.payrdr.mobile.payment.sdk.form.ui.GooglePayActivity
import net.payrdr.mobile.payment.sdk.form.ui.helper.LocalizationSetting
import net.payrdr.mobile.payment.sdk.form.ui.helper.ThemeSetting
import net.payrdr.mobile.payment.sdk.form.utils.finishWithError
import net.payrdr.mobile.payment.sdk.payment.model.CryptogramApiData
import net.payrdr.mobile.payment.sdk.payment.model.CryptogramGPayApiData
import net.payrdr.mobile.payment.sdk.payment.model.GPayDelegate
import net.payrdr.mobile.payment.sdk.payment.model.PaymentData
import net.payrdr.mobile.payment.sdk.payment.model.SDKPaymentConfig
import net.payrdr.mobile.payment.sdk.payment.model.WebChallengeParam
import net.payrdr.mobile.payment.sdk.threeds.spec.ChallengeParameters
import net.payrdr.mobile.payment.sdk.threeds.spec.ChallengeStatusReceiver
import net.payrdr.mobile.payment.sdk.threeds.spec.Factory
import net.payrdr.mobile.payment.sdk.threeds.spec.ThreeDS2Service
import net.payrdr.mobile.payment.sdk.threeds.spec.Transaction
import net.payrdr.mobile.payment.sdk.utils.finishWithResult

/**
 *  Activity for payment cycle.
 */
class PaymentActivity : AppCompatActivity() {
    private val sdkPaymentConfig: SDKPaymentConfig = SDKPayment.sdkPaymentConfig
    private val paymentScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val errorHandler = CoroutineExceptionHandler { _, exception ->
        LogDebug.logIfDebug("CoroutineExceptionHandler got $exception")
        finishWithError(exception as Exception)
    }

    /**
     *  Property for calling the window of creating a cryptogram.
     */
    private val cardFormDelegate = object : CardFormDelegate {

        override fun openBottomSheet(
            mdOrder: String,
            bindingEnabled: Boolean,
            bindingCards: List<BindingItem>,
            cvcNotRequired: Boolean,
            bindingDeactivationEnabled: Boolean,
            googlePayConfig: GooglePayPaymentConfig?
        ) {
            SDKForms.cryptogram(
                manager = supportFragmentManager,
                tag = null,
                config = PaymentConfigBuilder(mdOrder)
                    .cards(bindingCards.toCards())
                    .bindingCVCRequired(!cvcNotRequired)
                    .cardSaveOptions(savedFunctionByConfig(bindingEnabled))
                    .cardDeleteOptions(
                        if (bindingDeactivationEnabled) CardDeleteOptions.YES_DELETE
                        else CardDeleteOptions.NO_DELETE
                    )
                    .build(),
                googlePayConfig = googlePayConfig
            )
        }

        private fun savedFunctionByConfig(isEnabled: Boolean): CardSaveOptions = if (isEnabled) {
            CardSaveOptions.YES_BY_DEFAULT
        } else {
            CardSaveOptions.HIDE
        }

        private fun List<BindingItem>.toCards(): Set<Card> = map { it.toCard() }.toSet()

        /*
            Example of the label filed in [BindingItem]: "654654***3843 12/24".
            TODO replace with two separated fields month and year.
        */
        @Suppress("TooGenericExceptionCaught")
        private fun BindingItem.toCard(): Card = try {
            val label = label.split(" ")
            val expiryDate = label[1].split("/")
            Card(
                pan = label[0],
                bindingId = id,
                expiryDate = ExpiryDate(
                    expiryDate[0].toInt(),
                    expiryDate[1].toInt()
                )
            )
        } catch (e: Exception) {
            Card(
                pan = label,
                bindingId = id,
                expiryDate = null
            )
        }
    }

    /**
     *  Property for initializing the 3DS service, calling the Challenge Flow window, and completing the transaction.
     */
    private val threeDSFormDelegate = object : ThreeDSFormDelegate {
        override fun initThreeDS2Service(
            threeDS2Service: ThreeDS2Service,
            factory: Factory
        ) {
            val configParams = factory.newConfigParameters()
            val uiCustomization = factory.newUiCustomization(baseContext)
            threeDS2Service.initialize(
                this@PaymentActivity,
                configParams,
                "ru-RU",
                uiCustomization,
                SDKPayment.sdkPaymentConfig.sslContextConfig?.sslContext,
                SDKPayment.sdkPaymentConfig.sslContextConfig?.trustManager,
            )
        }

        override fun openChallengeFlow(
            transaction: Transaction?,
            challengeParameters: ChallengeParameters,
            challengeStatusReceiver: ChallengeStatusReceiver
        ) {
            transaction!!.doChallenge(
                this@PaymentActivity,
                challengeParameters,
                challengeStatusReceiver,
                TIMEOUT_THREE_DS,
            )
        }

        override fun openWebChallenge(webChallengeParam: WebChallengeParam) {
            this@PaymentActivity.startActivityForResult(
                ActivityWebChallenge.prepareIntent(
                    this@PaymentActivity,
                    webChallengeParam,
                ),
                REQUEST_CODE_3DS
            )
        }

        override fun cleanup(transaction: Transaction?, threeDS2Service: ThreeDS2Service) {
            transaction?.close()
            threeDS2Service.cleanup(this@PaymentActivity)
        }
    }

    /**
     *  A property to call the completion methods of the activity, passing information.
     */
    private val activityDelegate = object : ActivityDelegate {
        override fun finishActivityWithResult(paymentData: PaymentData) {
            finishWithResult(paymentData)
        }

        override fun finishActivityWithError(e: SDKException) {
            finishWithError(e)
        }

        override fun getPaymentConfig() = sdkPaymentConfig

        override fun get3DSOption(): Boolean = SDKPayment.use3ds2sdk
    }

    private val gPayDelegate = object : GPayDelegate {
        override fun openGPayForm(config: GooglePayPaymentConfig?) {
            config?.let {
                ThemeSetting.setTheme(config.theme)
                LocalizationSetting.setLanguage(config.locale)
                this@PaymentActivity.startActivityForResult(
                    GooglePayActivity.prepareIntent(this@PaymentActivity, config),
                    REQUEST_CODE_CRYPTOGRAM
                )
            } ?: run {
                Log.d("PAYRDRSDK", "GPay not supported by server")
                this@PaymentActivity.finish()
            }
        }
    }

    /**
     *  Initialization of the class that implements the payment process.
     */
    private val paymentManager: PaymentManagerImpl = PaymentManagerImpl(
        cardFormDelegate,
        threeDSFormDelegate,
        activityDelegate,
        gPayDelegate,
        paymentScope
    )

    private fun paymentNewCard(seToken: String, paymentInfo: PaymentInfoNewCard) {
        paymentScope.launch(errorHandler) {
            paymentManager.processFormData(
                cryptogramApiData = CryptogramApiData(
                    seToken = seToken,
                    mdOrder = paymentInfo.order,
                    holder = paymentInfo.holder.ifBlank { DEFAULT_VALUE_CARDHOLDER },
                    saveCard = paymentInfo.saveCard
                ),
                isBinding = false
            )
        }
    }

    private fun paymentBindingCard(seToken: String, paymentInfo: PaymentInfoBindCard) {
        paymentScope.launch(errorHandler) {
            paymentManager.processFormData(
                cryptogramApiData = CryptogramApiData(
                    seToken = seToken,
                    mdOrder = paymentInfo.order,
                    holder = DEFAULT_VALUE_CARDHOLDER
                ),
                isBinding = true
            )
        }
    }

    private fun gPayPayment(cryptogram: String, paymentInfoGooglePay: PaymentInfoGooglePay) {
        paymentScope.launch(errorHandler) {
            paymentManager.gPayProcessForm(
                cryptogramGPayApiData = CryptogramGPayApiData(
                    paymentToken = cryptogram,
                    mdOrder = paymentInfoGooglePay.order
                )
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)
        val mdOrder = intent.getStringExtra(MDORDER)
        paymentScope.launch(errorHandler) {
            paymentManager.checkout(mdOrder!!, intent.getBooleanExtra(IS_GOOGLE_PAY, false))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        paymentScope.cancel()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_3DS) {
            val paymentData = data?.getParcelableExtra(INTENT_EXTRA_RESULT) as PaymentData?
            if (paymentData != null) {
                finishWithResult(paymentData)
            } else {
                val exception = data?.getSerializableExtra(INTENT_EXTRA_ERROR) as SDKException?
                finishWithError(exception ?: SDKException("Unknown error"))
            }
        }

        // Processing the result of the formation of seToken.
        SDKForms.handleCryptogramResult(
            requestCode,
            data,
            object : ResultCryptogramCallback<CryptogramData> {
                override fun onSuccess(result: CryptogramData) {
                    // The result of the formation of the cryptogram.
                    when {
                        result.status.isSucceeded() -> {
                            deleteBindingCards(result.deletedCardsList)
                            val info = result.info
                            if (info is PaymentInfoNewCard) {
                                paymentNewCard(result.seToken, info)
                            } else if (info is PaymentInfoBindCard) {
                                paymentBindingCard(result.seToken, info)
                            } else if (info is PaymentInfoGooglePay) {
                                gPayPayment(result.seToken, info)
                                Log.d("PAYRDRSDK", "GPay seToken: ${result.seToken}")
                            }
                            LogDebug.logIfDebug("seToken created: $result")
                        }
                        result.status.isCanceled() -> {
                            LogDebug.logIfDebug("Cryptogram canceled")
                            deleteBindingCards(result.deletedCardsList)
                            paymentManager.finishWithError(SDKCryptogramException(cause = null))
                        }
                    }
                }

                override fun onFail(e: SDKException) {
                    // An error occurred.
                    LogDebug.logIfDebug("Cryptogram error: ${e.message} ${e.cause}")
                    paymentManager.finishWithError(SDKCryptogramException(cause = null))
                }
            }
        )
    }

    @Suppress("TooGenericExceptionCaught")
    private fun deleteBindingCards(cardsSet: Set<Card>) {
        paymentScope.launch {
            cardsSet.forEach { card ->
                try {
                    paymentManager.unbindCard(card.bindingId)
                } catch (e: Throwable) {
                    LogDebug.logIfDebug("Error unbind card.")
                }
            }
        }
    }

    companion object {
        private const val DEFAULT_VALUE_CARDHOLDER: String = "CARDHOLDER"

        /**
         * Prepares the [Intent] to start the checkout screen.
         *
         * @param context to prepare intent.
         * @param mdOrder order number.
         * @param gPayClicked user clicked gPay button.
         * @return [Intent] for sending to activity.
         */
        fun prepareIntent(
            context: Context,
            mdOrder: String,
            gPayClicked: Boolean
        ): Intent = Intent(context, PaymentActivity::class.java).apply {
            putExtra(MDORDER, mdOrder)
            putExtra(IS_GOOGLE_PAY, gPayClicked)
        }
    }
}
