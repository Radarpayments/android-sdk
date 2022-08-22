@file:Suppress("MagicNumber", "TooManyFunctions", "UndocumentedPublicClass")

package net.payrdr.mobile.payment.sample.kotlin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.WalletConstants
import kotlinx.android.synthetic.main.activity_main.amountInput
import kotlinx.android.synthetic.main.activity_main.bottomSheetButton
import kotlinx.android.synthetic.main.activity_main.cardCameraOffButton
import kotlinx.android.synthetic.main.activity_main.cardDoneTextButton
import kotlinx.android.synthetic.main.activity_main.cardEasyButton
import kotlinx.android.synthetic.main.activity_main.cardListNotRequiredCVCButton
import kotlinx.android.synthetic.main.activity_main.cardListNotRequiredCVCButtonWithDelCard
import kotlinx.android.synthetic.main.activity_main.cardListRequiredCVCButton
import kotlinx.android.synthetic.main.activity_main.cardNewButton
import kotlinx.android.synthetic.main.activity_main.cardNfcOffButton
import kotlinx.android.synthetic.main.activity_main.challengeFlowButton
import kotlinx.android.synthetic.main.activity_main.challengeFlowManualButton
import kotlinx.android.synthetic.main.activity_main.countryCodeInput
import kotlinx.android.synthetic.main.activity_main.currencyCodeInput
import kotlinx.android.synthetic.main.activity_main.darkThemeButton
import kotlinx.android.synthetic.main.activity_main.gatewayInput
import kotlinx.android.synthetic.main.activity_main.gatewayMerchantIdInput
import kotlinx.android.synthetic.main.activity_main.googlePayArea
import kotlinx.android.synthetic.main.activity_main.googlePayButtonFirst
import kotlinx.android.synthetic.main.activity_main.googlePayButtonFourth
import kotlinx.android.synthetic.main.activity_main.googlePayButtonSecond
import kotlinx.android.synthetic.main.activity_main.googlePayButtonThird
import kotlinx.android.synthetic.main.activity_main.googlePayCryptogram
import kotlinx.android.synthetic.main.activity_main.googlePayNotAvailableMessage
import kotlinx.android.synthetic.main.activity_main.lightThemeButton
import kotlinx.android.synthetic.main.activity_main.localeDeButton
import kotlinx.android.synthetic.main.activity_main.localeEnButton
import kotlinx.android.synthetic.main.activity_main.localeEsButton
import kotlinx.android.synthetic.main.activity_main.localeFrButton
import kotlinx.android.synthetic.main.activity_main.localeRuButton
import kotlinx.android.synthetic.main.activity_main.localeUkButton
import kotlinx.android.synthetic.main.activity_main.merchantIdInput
import kotlinx.android.synthetic.main.activity_main.sdkButton
import kotlinx.android.synthetic.main.activity_main.sdkButtonWeb
import net.payrdr.mobile.payment.sample.kotlin.helpers.copyToClipboard
import net.payrdr.mobile.payment.sample.kotlin.helpers.log
import net.payrdr.mobile.payment.sample.kotlin.payment.PaymentFormActivity
import net.payrdr.mobile.payment.sample.kotlin.payment.PaymentFormActivityWeb
import net.payrdr.mobile.payment.sample.kotlin.threeds.ThreeDSActivity
import net.payrdr.mobile.payment.sample.kotlin.threeds.ThreeDSManualActivity
import net.payrdr.mobile.payment.sdk.core.LogInterface
import net.payrdr.mobile.payment.sdk.core.Logger
import net.payrdr.mobile.payment.sdk.core.model.ExpiryDate
import net.payrdr.mobile.payment.sdk.form.GooglePayConfigBuilder
import net.payrdr.mobile.payment.sdk.form.PaymentConfigBuilder
import net.payrdr.mobile.payment.sdk.form.ResultCryptogramCallback
import net.payrdr.mobile.payment.sdk.form.SDKException
import net.payrdr.mobile.payment.sdk.form.SDKForms
import net.payrdr.mobile.payment.sdk.form.gpay.AllowedPaymentMethods
import net.payrdr.mobile.payment.sdk.form.gpay.GooglePayAuthMethod
import net.payrdr.mobile.payment.sdk.form.gpay.GooglePayCardNetwork
import net.payrdr.mobile.payment.sdk.form.gpay.GooglePayCheckoutOption
import net.payrdr.mobile.payment.sdk.form.gpay.GooglePayPaymentDataRequest
import net.payrdr.mobile.payment.sdk.form.gpay.GooglePayPaymentMethod
import net.payrdr.mobile.payment.sdk.form.gpay.GooglePayTotalPriceStatus
import net.payrdr.mobile.payment.sdk.form.gpay.GooglePayUtils
import net.payrdr.mobile.payment.sdk.form.gpay.GoogleTokenizationSpecificationType
import net.payrdr.mobile.payment.sdk.form.gpay.MerchantInfo
import net.payrdr.mobile.payment.sdk.form.gpay.PaymentMethodParameters
import net.payrdr.mobile.payment.sdk.form.gpay.TokenizationSpecification
import net.payrdr.mobile.payment.sdk.form.gpay.TokenizationSpecificationParameters
import net.payrdr.mobile.payment.sdk.form.gpay.TransactionInfo
import net.payrdr.mobile.payment.sdk.form.model.CameraScannerOptions
import net.payrdr.mobile.payment.sdk.form.model.Card
import net.payrdr.mobile.payment.sdk.form.model.CardDeleteOptions
import net.payrdr.mobile.payment.sdk.form.model.CardSaveOptions
import net.payrdr.mobile.payment.sdk.form.model.CryptogramData
import net.payrdr.mobile.payment.sdk.form.model.GooglePayPaymentConfig
import net.payrdr.mobile.payment.sdk.form.model.HolderInputOptions
import net.payrdr.mobile.payment.sdk.form.model.NfcScannerOptions
import net.payrdr.mobile.payment.sdk.form.model.PaymentInfoBindCard
import net.payrdr.mobile.payment.sdk.form.model.PaymentInfoGooglePay
import net.payrdr.mobile.payment.sdk.form.model.PaymentInfoNewCard
import net.payrdr.mobile.payment.sdk.form.model.Theme
import net.payrdr.mobile.payment.sdk.form.ui.helper.Locales.english
import net.payrdr.mobile.payment.sdk.form.ui.helper.Locales.french
import net.payrdr.mobile.payment.sdk.form.ui.helper.Locales.german
import net.payrdr.mobile.payment.sdk.form.ui.helper.Locales.russian
import net.payrdr.mobile.payment.sdk.form.ui.helper.Locales.spanish
import net.payrdr.mobile.payment.sdk.form.ui.helper.Locales.ukrainian
import net.payrdr.mobile.payment.sdk.threeds.ThreeDSLogger
import org.json.JSONObject
import java.math.BigDecimal
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val launchLocale = Locale.getDefault()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Logger.addLogInterface(object : LogInterface {
            override fun log(
                classMethod: Class<Any>,
                tag: String,
                message: String,
                exception: Exception?
            ) {
                Log.i(tag, "$classMethod: $message", exception)
            }
        })

        ThreeDSLogger.INSTANCE.addLogInterface(object : net.payrdr.mobile.payment.sdk.threeds.LogInterface {
            override fun log(
                classMethod: Class<*>,
                tag: String,
                message: String,
                exception: Throwable?
            ) {
                Log.i(tag, "$classMethod: $message", exception)
            }
        })

        setContentView(R.layout.activity_main)
        bottomSheetButton.setOnClickListener {
            val cards = setOf(
                Card(
                    "492980xxxxxx7724", "aa199a55-cf16-41b2-ac9e-cddc731edd19",
                    ExpiryDate(2025, 12)
                ),
                Card(
                    "558620xxxxxx6614", "6617c0b1-9976-45d9-b659-364ecac099e2",
                    ExpiryDate(2024, 6)
                ),
                Card(
                    "415482xxxxxx0000", "3d2d320f-ca9a-4713-977c-c852accf8a7b",
                    ExpiryDate(2019, 1)
                ),
                Card(
                    "532130xxxxxx1687", "aa199a55-cf16-41b2-ac9e-cddc731edd19",
                    ExpiryDate(2023, 4)
                ),
                Card(
                    "427644xxxxxx1831", "6617c0b1-9976-45d9-b659-364ecac099e2",
                    ExpiryDate(2020, 3)
                ),
                Card(
                    "427644xxxxxx9407", "3d2d320f-ca9a-4713-977c-c852accf8a7b",
                    ExpiryDate(2019, 1)
                ),
                Card(
                    "532130xxxxxx2712", "aa199a55-cf16-41b2-ac9e-cddc731edd19",
                    ExpiryDate(2022, 8)
                ),
                Card(
                    "532130xxxxxx6972", "6617c0b1-9976-45d9-b659-364ecac099e2",
                    ExpiryDate(2022, 8)
                ),
                Card(
                    "553691xxxxxx9319", "3d2d320f-ca9a-4713-977c-c852accf8a7b",
                    ExpiryDate(2025, 3)
                ),
                Card("411790xxxxxx123456", "ceae68c1-cb02-4804-9526-6d6b2f1f2793"),
                Card(
                    "492980xxxxxx3333", "aa199a55-cf16-41b2-ac9e-cddc731edd19",
                    ExpiryDate(2025, 12)
                ),
                Card(
                    "558620xxxxxx6444", "6617c0b1-9976-45d9-b659-364ecac099e2",
                    ExpiryDate(2024, 6)
                ),
                Card(
                    "415482xxxxxx1322", "3d2d320f-ca9a-4713-977c-c852accf8a7b",
                    ExpiryDate(2019, 1)
                ),
                Card("411790xxxxxx123456", "ceae68c1-cb02-4804-9526-6d6b2f1f2793")
            )
            val order = "00210bac-0ed1-474b-8ec2-5648cdfc4212"
            val paymentConfig = PaymentConfigBuilder(order)
                // Optional, by default localized translation "Pay".
                .buttonText("Оплатить 200 Ꝑ")
                // Optional, default HIDE.
                .cardSaveOptions(CardSaveOptions.YES_BY_DEFAULT)
                // Optional, default HIDE.
                .holderInputOptions(HolderInputOptions.VISIBLE)
                // Optional, default true.
                .bindingCVCRequired(false)
                // Optional, default ENABLED.
                .cameraScannerOptions(CameraScannerOptions.ENABLED)
                // Optional, default ENABLED.
                .nfcScannerOptions(NfcScannerOptions.ENABLED)
                // Optional, default DEFAULT.
                .theme(Theme.DEFAULT)
                // Optionally, the locale of the payment form is determined automatically.
                .locale(launchLocale)
                // Optional, the default is an empty list.
                .cards(cards)
                // Optionally, a unique payment identifier is generated automatically.
                .uuid("27fb1ebf-895e-4b15-bfeb-6ecae378fe8e")
                // Optionally, the time for generating the payment is set automatically.
                .timestamp(System.currentTimeMillis())
                // Optional, default is NO_DELETE.
                .cardDeleteOptions(CardDeleteOptions.NO_DELETE)
                .build()
            SDKForms.cryptogram(
                supportFragmentManager,
                "bottom sheet",
                paymentConfig,
                createGooglePayConfig()
            )
        }
        cardEasyButton.setOnClickListener { executeEasyCheckout() }
        cardCameraOffButton.setOnClickListener { executeCameraOffCheckout() }
        cardNfcOffButton.setOnClickListener { executeNfcOffCheckout() }
        cardDoneTextButton.setOnClickListener { executeDoneTextCheckout() }
        cardNewButton.setOnClickListener { executeCheckoutWithoutCards() }
        cardListNotRequiredCVCButton.setOnClickListener { executeCheckout(false) }
        cardListNotRequiredCVCButtonWithDelCard.setOnClickListener {
            executeCheckoutWithDeletingCard(
                false
            )
        }
        darkThemeButton.setOnClickListener { executeThemeCheckout(true) }
        lightThemeButton.setOnClickListener { executeThemeCheckout(false) }
        cardListRequiredCVCButton.setOnClickListener { executeCheckout(true) }
        localeRuButton.setOnClickListener { executeLocaleCheckout(russian()) }
        localeEnButton.setOnClickListener { executeLocaleCheckout(english()) }
        localeDeButton.setOnClickListener { executeLocaleCheckout(german()) }
        localeFrButton.setOnClickListener { executeLocaleCheckout(french()) }
        localeEsButton.setOnClickListener { executeLocaleCheckout(spanish()) }
        localeUkButton.setOnClickListener { executeLocaleCheckout(ukrainian()) }
        challengeFlowButton.setOnClickListener { executeThreeDSChallengeFlow() }
        challengeFlowManualButton.setOnClickListener { executeThreeDSChallengeFlowManual() }
        sdkButton.setOnClickListener { executePaymentCycleSdk() }
        sdkButtonWeb.setOnClickListener { executePaymentCycleSdkWeb() }

        GooglePayUtils.possiblyShowGooglePayButton(
            context = this,
            paymentsClient = GooglePayUtils.createPaymentsClient(
                context = this,
                environment = WalletConstants.ENVIRONMENT_TEST
            ),
            isReadyToPayJson = JSONObject(),
            callback = object : GooglePayUtils.GooglePayCheckCallback {
                override fun onNoGooglePlayServices() {
                    showGPayNotAvailableMessage()
                }

                override fun onNotReadyToRequest() {
                    showGPayNotAvailableMessage()
                }

                override fun onReadyToRequest() {
                    showGPayButtons()
                }
            }
        )
    }

    private fun executePaymentCycleSdk() {
        startActivity(Intent(this, PaymentFormActivity::class.java))
    }

    private fun executePaymentCycleSdkWeb() {
        startActivity(Intent(this, PaymentFormActivityWeb::class.java))
    }

    private fun executeThreeDSChallengeFlow() {
        startActivity(Intent(this, ThreeDSActivity::class.java))
    }

    private fun executeThreeDSChallengeFlowManual() {
        startActivity(Intent(this, ThreeDSManualActivity::class.java))
    }

    private fun showGPayNotAvailableMessage() {
        googlePayArea.visibility = GONE
        googlePayNotAvailableMessage.visibility = VISIBLE
    }

    private fun showGPayButtons() {
        googlePayArea.visibility = VISIBLE
        googlePayNotAvailableMessage.visibility = GONE
        listOf(
            googlePayButtonFirst,
            googlePayButtonSecond,
            googlePayButtonThird,
            googlePayButtonFourth
        ).forEach { button ->
            button.apply {
                setOnClickListener {
                    SDKForms.cryptogram(this@MainActivity, createGooglePayConfig())
                }
            }
        }
        googlePayCryptogram.setOnClickListener {
            copyToClipboard("Google Pay Cryptogram", googlePayCryptogram.text.toString())
        }
    }

    private fun executeEasyCheckout() {
        // Order ID is required.
        val order = "00210bac-0ed1-474b-8ec2-5648cdfc4212"
        val paymentConfig = PaymentConfigBuilder(order)
            .build()

        // Calling up the payment screen.
        SDKForms.cryptogram(this, paymentConfig)
    }

    private fun executeCameraOffCheckout() {
        // Order ID is required.
        val order = "00210bac-0ed1-474b-8ec2-5648cdfc4212"
        val paymentConfig = PaymentConfigBuilder(order)
            .cameraScannerOptions(CameraScannerOptions.DISABLED)
            .build()

        // Calling up the payment screen.
        SDKForms.cryptogram(this, paymentConfig)
    }

    private fun executeNfcOffCheckout() {
        // Order ID is required.
        val order = "00210bac-0ed1-474b-8ec2-5648cdfc4212"
        val paymentConfig = PaymentConfigBuilder(order)
            .nfcScannerOptions(NfcScannerOptions.DISABLED)
            .build()

        // Calling up the payment screen.
        SDKForms.cryptogram(this, paymentConfig)
    }

    private fun executeDoneTextCheckout() {
        // Order ID is required.
        val order = "00210bac-0ed1-474b-8ec2-5648cdfc4212"
        val paymentConfig = PaymentConfigBuilder(order)
            .buttonText("Renew subscription")
            .build()

        // Calling up the payment screen.
        SDKForms.cryptogram(this, paymentConfig)
    }

    private fun executeCheckoutWithoutCards() {
        // List of binding cards.
        val cards = emptySet<Card>()

        // Order ID is required.
        val order = "00210bac-0ed1-474b-8ec2-5648cdfc4212"
        val paymentConfig = PaymentConfigBuilder(order)
            // Optional, default HIDE.
            .cardSaveOptions(CardSaveOptions.YES_BY_DEFAULT)
            // Optional, default HIDE.
            .holderInputOptions(HolderInputOptions.VISIBLE)
            // Optionally, the locale of the payment form is determined automatically.
            .locale(launchLocale)
            // Optional, the default is an empty list.
            .cards(cards)
            // Optionally, a unique payment identifier is generated automatically.
            .uuid("27fb1ebf-895e-4b15-bfeb-6ecae378fe8e")
            // Optionally, the time for generating the payment is set automatically.
            .timestamp(System.currentTimeMillis())
            .build()

        // Calling up the payment screen.
        SDKForms.cryptogram(this, paymentConfig)
    }

    private fun executeCheckout(bindingCVCRequired: Boolean) {
        // List of binding cards.
        val cards = setOf(
            Card(
                "492980xxxxxx7724", "aa199a55-cf16-41b2-ac9e-cddc731edd19",
                ExpiryDate(2025, 12)
            ),
            Card(
                "558620xxxxxx6614", "6617c0b1-9976-45d9-b659-364ecac099e2",
                ExpiryDate(2024, 6)
            ),
            Card(
                "415482xxxxxx0000", "3d2d320f-ca9a-4713-977c-c852accf8a7b",
                ExpiryDate(2019, 1)
            ),
            Card("411790xxxxxx123456", "ceae68c1-cb02-4804-9526-6d6b2f1f2793")
        )

        // Order ID is required.
        val order = "00210bac-0ed1-474b-8ec2-5648cdfc4212"
        val paymentConfig = PaymentConfigBuilder(order)
            // Optional, by default localized translation "Pay".
            .buttonText("Оплатить 200 Ꝑ")
            // Optional, default HIDE.
            .cardSaveOptions(CardSaveOptions.YES_BY_DEFAULT)
            // Optional, default HIDE.
            .holderInputOptions(HolderInputOptions.VISIBLE)
            // Optional, default true.
            .bindingCVCRequired(bindingCVCRequired)
            // Optional, default ENABLED.
            .cameraScannerOptions(CameraScannerOptions.ENABLED)
            // Optional, default ENABLED.
            .nfcScannerOptions(NfcScannerOptions.ENABLED)
            // Optional, default DEFAULT.
            .theme(Theme.DEFAULT)
            // Optionally, the locale of the payment form is determined automatically.
            .locale(launchLocale)
            // Optional, the default is an empty list.
            .cards(cards)
            // Optionally, a unique payment identifier is generated automatically.
            .uuid("27fb1ebf-895e-4b15-bfeb-6ecae378fe8e")
            // Optionally, the time for generating the payment is set automatically.
            .timestamp(System.currentTimeMillis())
            // Optional, default is NO_DELETE.
            .cardDeleteOptions(CardDeleteOptions.NO_DELETE)
            .build()

        // Calling up the payment screen.
        SDKForms.cryptogram(this, paymentConfig)
    }

    private fun executeCheckoutWithDeletingCard(bindingCVCRequired: Boolean) {
        // List of binding cards.
        val cards = setOf(
            Card(
                "492980xxxxxx7724", "aa199a55-cf16-41b2-ac9e-cddc731edd19",
                ExpiryDate(2025, 12)
            ),
            Card(
                "558620xxxxxx6614", "6617c0b1-9976-45d9-b659-364ecac099e2",
                ExpiryDate(2024, 6)
            ),
            Card(
                "415482xxxxxx0000", "3d2d320f-ca9a-4713-977c-c852accf8a7b",
                ExpiryDate(2019, 1)
            ),
            Card("411790xxxxxx123456", "ceae68c1-cb02-4804-9526-6d6b2f1f2793")
        )

        // Order ID is required.
        val order = "00210bac-0ed1-474b-8ec2-5648cdfc4212"
        val paymentConfig = PaymentConfigBuilder(order)
            // Optional, by default localized translation "Pay".
            .buttonText("Оплатить 200 Ꝑ")
            // Optional, default HIDE.
            .cardSaveOptions(CardSaveOptions.YES_BY_DEFAULT)
            // Optional, default HIDE.
            .holderInputOptions(HolderInputOptions.VISIBLE)
            // Optional, default true.
            .bindingCVCRequired(bindingCVCRequired)
            // Optional, default ENABLED.
            .cameraScannerOptions(CameraScannerOptions.ENABLED)
            // Optional, default ENABLED.
            .nfcScannerOptions(NfcScannerOptions.ENABLED)
            // Optional, default DEFAULT.
            .theme(Theme.DEFAULT)
            // Optionally, the locale of the payment form is determined automatically.
            .locale(launchLocale)
            // Optional, the default is an empty list.
            .cards(cards)
            // Optionally, a unique payment identifier is generated automatically.
            .uuid("27fb1ebf-895e-4b15-bfeb-6ecae378fe8e")
            // Optionally, the time for generating the payment is set automatically.
            .timestamp(System.currentTimeMillis())
            // Optional, default is NO_DELETE.
            .cardDeleteOptions(CardDeleteOptions.YES_DELETE)
            .build()

        // Calling up the payment screen.
        SDKForms.cryptogram(this, paymentConfig)
    }

    private fun executeThemeCheckout(isDark: Boolean) {
        // List of binding cards.
        val cards = setOf(
            Card(
                "492980xxxxxx7724", "aa199a55-cf16-41b2-ac9e-cddc731edd19",
                ExpiryDate(2025, 12)
            ),
            Card(
                "558620xxxxxx6614", "6617c0b1-9976-45d9-b659-364ecac099e2",
                ExpiryDate(2024, 6)
            ),
            Card(
                "415482xxxxxx0000", "3d2d320f-ca9a-4713-977c-c852accf8a7b",
                ExpiryDate(2019, 1)
            ),
            Card("411790xxxxxx123456", "ceae68c1-cb02-4804-9526-6d6b2f1f2793")
        )

        // Order ID is required.
        val order = "00210bac-0ed1-474b-8ec2-5648cdfc4212"
        val paymentConfig = PaymentConfigBuilder(order)
            // Optional, by default localized translation "Pay".
            .buttonText("Оплатить 200 Ꝑ")
            // Optional, default HIDE.
            .cardSaveOptions(CardSaveOptions.YES_BY_DEFAULT)
            // Optional, default HIDE.
            .holderInputOptions(HolderInputOptions.VISIBLE)
            // Optional, default true.
            .bindingCVCRequired(true)
            // Optional, default ENABLED.
            .cameraScannerOptions(CameraScannerOptions.ENABLED)
            // Optional, default SYSTEM.
            .theme(if (isDark) Theme.DARK else Theme.LIGHT)
            // Optionally, the locale of the payment form is determined automatically.
            .locale(launchLocale)
            // Optional, the default is an empty list.
            .cards(cards)
            // Optionally, a unique payment identifier is generated automatically.
            .uuid("27fb1ebf-895e-4b15-bfeb-6ecae378fe8e")
            // Optionally, the time for generating the payment is set automatically.
            .timestamp(System.currentTimeMillis())
            .build()

        // Calling up the payment screen.
        SDKForms.cryptogram(this, paymentConfig)
    }

    private fun executeLocaleCheckout(locale: Locale) {
        // List of binding cards.
        val cards = setOf(
            Card("492980xxxxxx7724", "aa199a55-cf16-41b2-ac9e-cddc731edd19"),
            Card("5586200016956614", "ee199a55-cf16-41b2-ac9e-cc1c731edd19")
        )

        // Order ID is required.
        val order = "00210bac-0ed1-474b-8ec2-5648cdfc4212"
        val paymentConfig = PaymentConfigBuilder(order)
            // Optional, default HIDE.
            .cardSaveOptions(CardSaveOptions.YES_BY_DEFAULT)
            // Optional, default HIDE.
            .holderInputOptions(HolderInputOptions.VISIBLE)
            // Optionally, the locale of the payment form is determined automatically.
            .locale(locale)
            // Optional, the default is an empty list.
            .cards(cards)
            // Optionally, a unique payment identifier is generated automatically.
            .uuid("27fb1ebf-895e-4b15-bfeb-6ecae378fe8e")
            // Optionally, the time for generating the payment is set automatically.
            .timestamp(System.currentTimeMillis())
            .build()

        // Calling up the payment screen.
        SDKForms.cryptogram(this, paymentConfig)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Processing the result.
        SDKForms.handleCryptogramResult(requestCode, data, object :
            ResultCryptogramCallback<CryptogramData> {

            override fun onSuccess(result: CryptogramData) {
                // The result of creating a cryptogram.
                when {
                    result.status.isSucceeded() -> {
                        val info = result.info
                        if (info is PaymentInfoNewCard) {
                            log("New card ${info.holder} ${info.saveCard}")
                        } else if (info is PaymentInfoBindCard) {
                            log("Saved card ${info.bindingId}")
                        } else if (info is PaymentInfoGooglePay) {
                            log("Google Pay ${info.order}")
                            googlePayCryptogram.text = result.seToken
                        }
                        log("$result")
                        log("Deleted cards ${result.deletedCardsList}")
                    }
                    result.status.isCanceled() -> {
                        log("canceled")
                        log("Deleted cards ${result.deletedCardsList}")
                    }
                }
            }

            override fun onFail(e: SDKException) {
                // An error has occurred.
                log("${e.message} ${e.cause}")
            }
        })
    }

    private fun createGooglePayConfig(): GooglePayPaymentConfig {
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
                                    gateway = gatewayInput.text.toString()
                                    gatewayMerchantId = gatewayMerchantIdInput.text.toString()
                                }
                        }
                }
            }
            transactionInfo = TransactionInfo.transactionInfoCreate {
                totalPrice = BigDecimal.valueOf(amountInput.text.toString().toDoubleOrNull() ?: 0.0)
                totalPriceStatus = GooglePayTotalPriceStatus.FINAL
                countryCode = countryCodeInput.text.toString()
                currencyCode = currencyCodeInput.text.toString()
                checkoutOption = GooglePayCheckoutOption.COMPLETE_IMMEDIATE_PURCHASE
            }
            merchantInfo = MerchantInfo.merchantInfoCreate {
                merchantName = "Example Merchant"
                merchantId = merchantIdInput.text.toString()
            }
        }.toJson().toString()

        return GooglePayConfigBuilder(
            order = "5daf3fe4-09a7-74d2-95a2-521b1917ef58",
            paymentData = PaymentDataRequest.fromJson(paymentData)
        ).testEnvironment(true)
            .build()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.menu_theme_toggle) {
            toggleTheme()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun toggleTheme() {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }
}
