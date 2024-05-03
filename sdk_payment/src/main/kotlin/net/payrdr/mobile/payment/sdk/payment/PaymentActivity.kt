package net.payrdr.mobile.payment.sdk.payment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import net.payrdr.mobile.payment.sdk.Constants.INTENT_EXTRA_RESULT
import net.payrdr.mobile.payment.sdk.Constants.IS_GOOGLE_PAY
import net.payrdr.mobile.payment.sdk.Constants.MDORDER
import net.payrdr.mobile.payment.sdk.Constants.REQUEST_CODE_3DS1
import net.payrdr.mobile.payment.sdk.Constants.REQUEST_CODE_CRYPTOGRAM
import net.payrdr.mobile.payment.sdk.Constants.TIMEOUT_THREE_DS
import net.payrdr.mobile.payment.sdk.LogDebug
import net.payrdr.mobile.payment.sdk.R
import net.payrdr.mobile.payment.sdk.SDKPayment
import net.payrdr.mobile.payment.sdk.api.entity.BindingItem
import net.payrdr.mobile.payment.sdk.core.model.ExpiryDate
import net.payrdr.mobile.payment.sdk.core.model.MSDKRegisteredFrom
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
import net.payrdr.mobile.payment.sdk.payment.model.GooglePayProcessFormRequest
import net.payrdr.mobile.payment.sdk.payment.model.PaymentResult
import net.payrdr.mobile.payment.sdk.payment.model.SDKPaymentConfig
import net.payrdr.mobile.payment.sdk.payment.model.WebChallengeParam
import net.payrdr.mobile.payment.sdk.threeds.spec.ChallengeParameters
import net.payrdr.mobile.payment.sdk.threeds.spec.ChallengeStatusReceiver
import net.payrdr.mobile.payment.sdk.threeds.spec.SDKNotInitializedException
import net.payrdr.mobile.payment.sdk.threeds.spec.ThreeDS2Service
import net.payrdr.mobile.payment.sdk.threeds.spec.Transaction

/**
 *  Activity for payment cycle.
 */
class PaymentActivity : AppCompatActivity() {
    private var resultForHandle: ResultForHandle? = null
    private val sdkPaymentConfig: SDKPaymentConfig = SDKPayment.sdkPaymentConfig

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
                    .registeredFrom(MSDKRegisteredFrom.MSDK_PAYMENT)
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
     *  Property for initializing the 3DS1 web form, calling the Challenge Flow window, and completing the transaction.
     */

    private val threeDS1FormDelegate = object : ThreeDS1FormDelegate {

        override fun openWebChallenge(webChallengeParam: WebChallengeParam) {
            this@PaymentActivity.startActivityForResult(
                Activity3DS1Challenge.prepareIntent(
                    this@PaymentActivity,
                    webChallengeParam,
                ),
                REQUEST_CODE_3DS1
            )
        }
    }

    /**
     *  Property for initializing the 3DS2 service, calling the Challenge Flow window, and completing the transaction.
     */
    private val threeDS2FormDelegate = object : ThreeDS2FormDelegate {

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

        override fun cleanup(transaction: Transaction?, threeDS2Service: ThreeDS2Service) {
            transaction?.close()
            try {
                threeDS2Service.cleanup(this@PaymentActivity)
            } catch (ex: SDKNotInitializedException) {
                LogDebug.logIfDebug(ex.toString())
            }
        }

        override fun getApplicationContext(): Context {
            return this@PaymentActivity.applicationContext
        }

        override fun getBaseContext(): Context {
            return this@PaymentActivity.baseContext
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
    private val paymentManager: PaymentManagerImpl = PaymentManagerImpl(
        cardFormDelegate,
        threeDS1FormDelegate,
        threeDS2FormDelegate,
        activityDelegate,
        gPayDelegate,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)
        val mdOrder = intent.getStringExtra(MDORDER)
        paymentManager.checkout(mdOrder!!, intent.getBooleanExtra(IS_GOOGLE_PAY, false))
    }

    override fun onResume() {
        super.onResume()
        resultForHandle?.let {
            val (requestCode, resultCode, data) = it
            resultForHandle = null
            if (requestCode == REQUEST_CODE_3DS1) {
                // Processing the result of the 3DS1 flow challenge.
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
                                    deleteBindingCards(result.deletedCardsList)
                                    when (val info = result.info) {
                                        is PaymentInfoNewCard -> {
                                            paymentNewCard(result.seToken, info)
                                        }

                                        is PaymentInfoBindCard -> {
                                            paymentBindingCard(result.seToken, info)
                                        }

                                        is PaymentInfoGooglePay -> {
                                            gPayPayment(result.seToken, info)
                                            Log.d("PAYRDRSDK", "GPay seToken: ${result.seToken}")
                                        }
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
                            paymentManager.finishWithError(e)
                        }
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        paymentManager.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        this.resultForHandle = ResultForHandle(
            requestCode,
            resultCode,
            data
        )
    }

    private fun paymentNewCard(seToken: String, paymentInfo: PaymentInfoNewCard) {
        paymentManager.processNewCard(
            seToken = seToken,
            mdOrder = paymentInfo.order,
            holder = paymentInfo.holder,
            saveCard = paymentInfo.saveCard,
        )
    }

    private fun paymentBindingCard(seToken: String, paymentInfo: PaymentInfoBindCard) {
        paymentManager.processBindingCard(
            seToken = seToken,
            mdOrder = paymentInfo.order,
        )
    }

    private fun gPayPayment(cryptogram: String, paymentInfoGooglePay: PaymentInfoGooglePay) {
        paymentManager.gPayProcessForm(
            cryptogramGPayApiData = GooglePayProcessFormRequest(
                paymentToken = cryptogram,
                mdOrder = paymentInfoGooglePay.order
            )
        )
    }

    @Suppress("TooGenericExceptionCaught")
    private fun deleteBindingCards(cardsSet: Set<Card>) {
        cardsSet.forEach { card ->
            try {
                paymentManager.unbindCard(card.bindingId)
            } catch (e: Throwable) {
                LogDebug.logIfDebug("Error unbind card.")
            }
        }
    }

    /**
     * Terminates the [Activity] on which this method was called with passing as a result
     * [Activity] work with value [paymentData].
     *
     * @param paymentData - result of [Activity] work.
     */
    private fun finishWithResult(paymentData: PaymentResult) {
        val resultIntent = Intent().apply {
            putExtra(INTENT_EXTRA_RESULT, paymentData)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    private data class ResultForHandle(
        val requestCode: Int,
        val resultCode: Int,
        val data: Intent?
    )

    companion object {

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
