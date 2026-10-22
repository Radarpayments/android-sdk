package net.payrdr.mobile.payment.sdk.payment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_payment.paymentPreloader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.payrdr.mobile.payment.sdk.Constants.INTENT_EXTRA_RESULT
import net.payrdr.mobile.payment.sdk.Constants.IS_GOOGLE_PAY
import net.payrdr.mobile.payment.sdk.Constants.MDORDER
import net.payrdr.mobile.payment.sdk.Constants.PAYMENT_API_VERSION
import net.payrdr.mobile.payment.sdk.Constants.REQUEST_CODE_3DS2_WEB
import net.payrdr.mobile.payment.sdk.Constants.REQUEST_CODE_CRYPTOGRAM
import net.payrdr.mobile.payment.sdk.LogDebug
import net.payrdr.mobile.payment.sdk.PaymentConfigBuilder
import net.payrdr.mobile.payment.sdk.R
import net.payrdr.mobile.payment.sdk.ResultCryptogramCallback
import net.payrdr.mobile.payment.sdk.SDKException
import net.payrdr.mobile.payment.sdk.SDKForms
import net.payrdr.mobile.payment.sdk.SDKPayment
import net.payrdr.mobile.payment.sdk.api.entity.BindingItem
import net.payrdr.mobile.payment.sdk.component.KeyProviderException
import net.payrdr.mobile.payment.sdk.component.impl.RemoteKeyProvider
import net.payrdr.mobile.payment.sdk.core.SDKCore
import net.payrdr.mobile.payment.sdk.core.model.BindingParams
import net.payrdr.mobile.payment.sdk.core.model.CardParams
import net.payrdr.mobile.payment.sdk.core.model.MSDKRegisteredFrom
import net.payrdr.mobile.payment.sdk.core.model.SDKCoreConfig
import net.payrdr.mobile.payment.sdk.core.utils.toExpDate
import net.payrdr.mobile.payment.sdk.exceptions.SDKCryptogramException
import net.payrdr.mobile.payment.sdk.model.AdditionalFieldsForPaymentSystem
import net.payrdr.mobile.payment.sdk.model.Card
import net.payrdr.mobile.payment.sdk.model.CardDeleteOptions
import net.payrdr.mobile.payment.sdk.model.CardSaveOptions
import net.payrdr.mobile.payment.sdk.model.CryptogramData
import net.payrdr.mobile.payment.sdk.model.GooglePayPaymentConfig
import net.payrdr.mobile.payment.sdk.model.PaymentInfo
import net.payrdr.mobile.payment.sdk.model.PaymentInfoBindCard
import net.payrdr.mobile.payment.sdk.model.PaymentInfoGooglePay
import net.payrdr.mobile.payment.sdk.model.PaymentInfoNewCard
import net.payrdr.mobile.payment.sdk.payment.model.GooglePayProcessFormRequest
import net.payrdr.mobile.payment.sdk.payment.model.PaymentApiVersion
import net.payrdr.mobile.payment.sdk.payment.model.PaymentResult
import net.payrdr.mobile.payment.sdk.payment.model.SDKPaymentConfig
import net.payrdr.mobile.payment.sdk.payment.model.WebChallengeParam
import net.payrdr.mobile.payment.sdk.ui.GooglePayActivity
import net.payrdr.mobile.payment.sdk.ui.helper.LocalizationSetting
import net.payrdr.mobile.payment.sdk.ui.helper.ThemeSetting

/**
 *  Activity for payment cycle.
 */
class PaymentActivity : AppCompatActivity() {
    private var resultForHandle: ResultForHandle? = null
    private var isRestored = false
    private val sdkPaymentConfig: SDKPaymentConfig
        get() = SDKPayment.sdkPaymentConfig
    private val workScope: CoroutineScope = CoroutineScope(Dispatchers.IO + Job())

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
            googlePayConfig: GooglePayPaymentConfig?,
            additionalCardParamForPayments: AdditionalFieldsForPaymentSystem
        ) {
            SDKForms.cryptogram(
                manager = supportFragmentManager,
                tag = null,
                config = PaymentConfigBuilder(mdOrder)
                    .cards(bindingCards.toCards())
                    .storedPaymentMethodCVCRequired(!cvcNotRequired)
                    .cardSaveOptions(savedFunctionByConfig(bindingEnabled))
                    .cardDeleteOptions(
                        if (bindingDeactivationEnabled) CardDeleteOptions.YES_DELETE
                        else CardDeleteOptions.NO_DELETE
                    ).registeredFrom(MSDKRegisteredFrom.MSDK_PAYMENT)
                    .paramsNeedToBeFilledForVisa(additionalCardParamForPayments.visaFields)
                    .paramsNeedToBeFilledForMastercard(additionalCardParamForPayments.mastercardFields)
                    .build(),
                googlePayConfig = googlePayConfig
            )
        }

        private fun savedFunctionByConfig(isEnabled: Boolean): CardSaveOptions = if (isEnabled) {
            CardSaveOptions.YES_BY_DEFAULT
        } else {
            CardSaveOptions.HIDE
        }

        // Example of the label field in [BindingItem]: "654654***3843 12/24".
        private fun List<BindingItem>.toCards(): Set<Card> = map { it.toCard() }.toSet()

        @Suppress("TooGenericExceptionCaught")
        private fun BindingItem.toCard(): Card = try {
            val parts = label.split(" ")
            Card(
                pan = parts[0],
                bindingId = id,
                // The label carries the expiry as MM/YY.
                expiryDate = parts[1].toExpDate()
            )
        } catch (e: Exception) {
            Card(pan = label, bindingId = id, expiryDate = null)
        }
    }

    /**
     *  Property for initializing the 3DS2 web form, calling the Challenge Flow window, and completing the transaction.
     */

    private val threeDS1FormDelegate = object : ThreeDS2WebFormDelegate {

        override fun openWebChallenge(webChallengeParam: WebChallengeParam) {
            hidePaymentPreloader()
            this@PaymentActivity.startActivityForResult(
                Activity3DS2WebChallenge.prepareIntent(
                    this@PaymentActivity,
                    webChallengeParam,
                ),
                REQUEST_CODE_3DS2_WEB
            )
        }
    }

    /**
     *  A property to call the completion methods of the activity, passing information.
     */
    private val activityDelegate = object : ActivityDelegate {
        override fun finishActivityWithResult(paymentData: PaymentResult) {
            finishWithResult(paymentData)
        }

        override fun getPaymentConfig() = sdkPaymentConfig
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
    private val paymentManagerLazy = lazy {
        PaymentManagerImpl(
            cardFormDelegate,
            threeDS1FormDelegate,
            activityDelegate,
            gPayDelegate,
        )
    }
    private val paymentManager: PaymentManagerImpl by paymentManagerLazy

    override fun onCreate(savedInstanceState: Bundle?) {
        val isSdkInitialized = SDKPayment.restoreConfigIfNeeded(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)
        val mdOrder = intent.getStringExtra(MDORDER)
        if (!isSdkInitialized) {
            finishWithResult(
                PaymentResult(
                    sessionId = mdOrder.orEmpty(),
                    isSuccess = false,
                    exception = SDKException(
                        "SDKPayment is not initialized. Please call SDKPayment.init() in Application.onCreate()."
                    ),
                )
            )
            return
        }
        val versionApi = intent.getSerializableExtra(PAYMENT_API_VERSION) as PaymentApiVersion
        if (savedInstanceState == null) {
            paymentManager.checkout(mdOrder!!, intent.getBooleanExtra(IS_GOOGLE_PAY, false), versionApi)
        } else {
            // Recreated after a configuration change or process death: Android restores the bottom sheet
            // and the screens above it, starting the checkout again would open a second bottom sheet.
            paymentManager.restore(mdOrder!!, versionApi)
            isRestored = true
        }
    }

    override fun onNewIntent(intent: Intent?) {
        this.resultForHandle = ResultForHandle(
            REQUEST_CODE_CRYPTOGRAM,
            0,
            intent
        )
        super.onNewIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        // The payment was being processed when the activity was destroyed (e.g. the process died during
        // the network requests): nothing on screen can continue it, so report the order status instead of
        // leaving an empty transparent screen over the host app.
        val restoredWithNothingToContinue = isRestored && resultForHandle == null &&
            supportFragmentManager.fragments.isEmpty()
        isRestored = false
        if (restoredWithNothingToContinue) {
            showPaymentPreloader()
            paymentManager.finishWithCheckOrderStatus()
        }
        resultForHandle?.let {
            val (requestCode, resultCode, data) = it
            resultForHandle = null
            if (requestCode == REQUEST_CODE_3DS2_WEB) {
                // Processing the result of the 3DS2 web flow challenge.
                val paymentData = data?.getParcelableExtra(INTENT_EXTRA_RESULT) as PaymentResult?
                if (paymentData?.exception != null) {
                    paymentManager.finishWithError(paymentData.exception)
                } else {
                    paymentManager.finishWithCheckOrderStatus()
                }
            } else {

                // Processing the result of the formation of seToken.
                SDKForms.handleCryptogramResult(
                    requestCode,
                    data,
                    object : ResultCryptogramCallback<CryptogramData> {
                        override fun onSuccess(result: CryptogramData) {
                            // The result of the formation of the cryptogram.
                            when {
                                result.status.isSucceeded() -> {
                                    showPaymentPreloader()
                                    workScope.launch(Dispatchers.Main) {
                                        val keyProvider = RemoteKeyProvider(
                                            "${sdkPaymentConfig.baseURL}/se/keys.do",
                                            sdkPaymentConfig.sslContextConfig?.sslContext,
                                        )
                                        val pubKey = try {
                                            keyProvider.provideKey().value
                                        } catch (e: KeyProviderException) {
                                            LogDebug.logIfDebug("Key loading error: ${e.message}")
                                            paymentManager.finishWithError(e)
                                            return@launch
                                        }
                                        payWithPubKey(result.info, pubKey)
                                        LogDebug.logIfDebug("seToken created: $result")
                                    }
                                }

                                result.status.isCanceled() -> {
                                    LogDebug.logIfDebug("Cryptogram canceled")
                                    paymentManager.finishWithError(SDKCryptogramException(cause = null))
                                }
                            }
                        }

                        override fun onFail(e: SDKException) {
                            // An error occurred.
                            LogDebug.logIfDebug("Cryptogram error: ${e.message} ${e.cause}")
                            paymentManager.finishWithError(e)
                        }
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (paymentManagerLazy.isInitialized()) {
            paymentManager.onDestroy()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        this.resultForHandle = ResultForHandle(
            requestCode,
            resultCode,
            data
        )
    }

    private fun payWithPubKey(info: PaymentInfo?, pubKey: String) {
        when (info) {
            is PaymentInfoNewCard -> {
                val config = SDKCoreConfig(
                    registeredFrom = MSDKRegisteredFrom.MSDK_PAYMENT,
                    paymentCardParams = CardParams(
                        pan = info.pan,
                        cvc = info.cvc,
                        expiryMMYY = info.expiryDate,
                        cardHolder = info.holder,
                        mdOrder = info.order,
                        pubKey = pubKey
                    )
                )
                val paymentToken = SDKCore(this).generateWithConfig(config)
                paymentNewCard(seToken = paymentToken.token ?: "", info)
            }

            is PaymentInfoBindCard -> {
                val config = SDKCoreConfig(
                    registeredFrom = MSDKRegisteredFrom.MSDK_PAYMENT,
                    paymentCardParams = BindingParams(
                        cvc = info.cvc,
                        bindingID = info.bindingId,
                        mdOrder = info.order,
                        pubKey = pubKey
                    )
                )
                val paymentToken = SDKCore(this).generateWithConfig(config)
                paymentBindingCard(seToken = paymentToken.token ?: "", info)
            }

            is PaymentInfoGooglePay -> {
                gPayPayment(info)
                Log.d("PAYRDRSDK", "GPay seToken: ${info.paymentToken}")
            }
        }
    }

    private fun paymentNewCard(seToken: String, paymentInfo: PaymentInfoNewCard) {
        paymentManager.processNewCard(
            seToken = seToken,
            mdOrder = paymentInfo.order,
            holder = paymentInfo.holder,
            saveCard = paymentInfo.saveCard,
            filledAdditionalPayerParams = paymentInfo.filledAdditionalPayerParams
        )
    }

    private fun paymentBindingCard(seToken: String, paymentInfo: PaymentInfoBindCard) {
        paymentManager.processBindingCard(
            seToken = seToken,
            mdOrder = paymentInfo.order,
            filledAdditionalPayerParams = paymentInfo.filledAdditionalPayerParams
        )
    }

    private fun gPayPayment(paymentInfoGooglePay: PaymentInfoGooglePay) {
        paymentManager.gPayProcessForm(
            cryptogramGPayApiData = GooglePayProcessFormRequest(
                paymentToken = paymentInfoGooglePay.paymentToken,
                mdOrder = paymentInfoGooglePay.order
            )
        )
    }

    /**
     * Terminates the [Activity] on which this method was called with passing as a result
     * [Activity] work with value [paymentData].
     *
     * @param paymentData - result of [Activity] work.
     */
    private fun finishWithResult(paymentData: PaymentResult) {
        hidePaymentPreloader()
        SDKPayment.clearSavedConfig(this)
        val resultIntent = Intent().apply {
            putExtra(INTENT_EXTRA_RESULT, paymentData)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    /**
     * Shows a full-screen dimmed overlay with a progress indicator while the payment is processed.
     *
     * The activity window is floating and transparent, so it is stretched to the full screen
     * and dims the content behind it for as long as the preloader is visible. The window is not
     * shrunk back on hide: resizing it makes the old frame with the indicator flash in the corner.
     */
    private fun showPaymentPreloader() {
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.setDimAmount(PRELOADER_DIM_AMOUNT)
        paymentPreloader.visibility = View.VISIBLE
    }

    /**
     * Hides the preloader shown by [showPaymentPreloader]. Safe to call from any thread.
     */
    private fun hidePaymentPreloader() = runOnUiThread {
        paymentPreloader.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }

    private data class ResultForHandle(
        val requestCode: Int,
        val resultCode: Int,
        val data: Intent?
    )

    companion object {

        private const val PRELOADER_DIM_AMOUNT = 0.4f

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
            gPayClicked: Boolean,
            paymentApiVersion: PaymentApiVersion
        ): Intent = Intent(context, PaymentActivity::class.java).apply {
            putExtra(MDORDER, mdOrder)
            putExtra(IS_GOOGLE_PAY, gPayClicked)
            putExtra(PAYMENT_API_VERSION, paymentApiVersion)
        }
    }
}
