package net.payrdr.mobile.payment.sdk.form.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.PictureDrawable
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.caverock.androidsvg.SVG
import com.github.devnied.emvnfccard.exception.CommunicationException
import io.card.payment.CardIOActivity
import io.card.payment.CardIOActivity.EXTRA_HIDE_CARDIO_LOGO
import io.card.payment.CardIOActivity.EXTRA_LANGUAGE_OR_LOCALE
import io.card.payment.CardIOActivity.EXTRA_REQUIRE_CARDHOLDER_NAME
import io.card.payment.CardIOActivity.EXTRA_REQUIRE_CVV
import io.card.payment.CardIOActivity.EXTRA_REQUIRE_EXPIRY
import io.card.payment.CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE
import io.card.payment.CardIOActivity.EXTRA_SUPPRESS_CONFIRMATION
import io.card.payment.CardIOActivity.EXTRA_USE_PAYPAL_ACTIONBAR_ICON
import io.card.payment.CreditCard
import kotlinx.android.synthetic.main.activity_card_new.bankCardView
import kotlinx.android.synthetic.main.activity_card_new.cardCodeInput
import kotlinx.android.synthetic.main.activity_card_new.cardCodeInputLayout
import kotlinx.android.synthetic.main.activity_card_new.cardExpiryInput
import kotlinx.android.synthetic.main.activity_card_new.cardExpiryInputLayout
import kotlinx.android.synthetic.main.activity_card_new.cardHolderInput
import kotlinx.android.synthetic.main.activity_card_new.cardNumberInput
import kotlinx.android.synthetic.main.activity_card_new.cardNumberInputLayout
import kotlinx.android.synthetic.main.activity_card_new.doneButton
import kotlinx.android.synthetic.main.activity_card_new.switchBox
import kotlinx.android.synthetic.main.activity_card_new.switchBoxText
import kotlinx.android.synthetic.main.activity_card_new.toolbar
import kotlinx.android.synthetic.main.activity_card_new.view.arrow_back
import kotlinx.android.synthetic.main.activity_card_new.view.title
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.payrdr.mobile.payment.sdk.core.model.CardInfo
import net.payrdr.mobile.payment.sdk.core.model.CardPanIdentifier
import net.payrdr.mobile.payment.sdk.core.utils.digitsOnly
import net.payrdr.mobile.payment.sdk.core.utils.toExpDate
import net.payrdr.mobile.payment.sdk.core.utils.toStringExpDate
import net.payrdr.mobile.payment.sdk.form.Constants.INTENT_EXTRA_CONFIG
import net.payrdr.mobile.payment.sdk.form.Constants.REQUEST_CODE_SCAN_CARD
import net.payrdr.mobile.payment.sdk.form.R
import net.payrdr.mobile.payment.sdk.form.SDKForms
import net.payrdr.mobile.payment.sdk.form.component.CryptogramProcessor
import net.payrdr.mobile.payment.sdk.form.model.CameraScannerOptions
import net.payrdr.mobile.payment.sdk.form.model.CardSaveOptions
import net.payrdr.mobile.payment.sdk.form.model.CryptogramData
import net.payrdr.mobile.payment.sdk.form.model.NfcScannerOptions
import net.payrdr.mobile.payment.sdk.form.model.PaymentConfig
import net.payrdr.mobile.payment.sdk.form.model.PaymentDataStatus
import net.payrdr.mobile.payment.sdk.form.model.PaymentInfoNewCard
import net.payrdr.mobile.payment.sdk.form.nfc.NFCReadDelegate
import net.payrdr.mobile.payment.sdk.form.ui.helper.CardLogoAssetsResolver
import net.payrdr.mobile.payment.sdk.form.ui.helper.CardResolver
import net.payrdr.mobile.payment.sdk.form.ui.helper.LocalizationSetting
import net.payrdr.mobile.payment.sdk.form.ui.widget.BaseTextInputEditText
import net.payrdr.mobile.payment.sdk.form.utils.addRightButtons
import net.payrdr.mobile.payment.sdk.form.utils.afterTextChanged
import net.payrdr.mobile.payment.sdk.form.utils.askToEnableNfc
import net.payrdr.mobile.payment.sdk.form.utils.deviceHasCamera
import net.payrdr.mobile.payment.sdk.form.utils.deviceHasNFC
import net.payrdr.mobile.payment.sdk.form.utils.finishWithError
import net.payrdr.mobile.payment.sdk.form.utils.finishWithResult
import net.payrdr.mobile.payment.sdk.form.utils.onDisplayError
import net.payrdr.mobile.payment.sdk.form.utils.onInputStatusChanged
import java.util.Date

/**
 * New card screen.
 */
@Suppress("TooManyFunctions")
class CardNewActivity : BaseActivity() {

    private var cryptogramProcessor: CryptogramProcessor = SDKForms.cryptogramProcessor
    private val cardResolver: CardResolver by lazy {
        CardResolver(
            bankCardView = bankCardView,
            cardInfoProvider = SDKForms.sdkConfig.cardInfoProvider
        )
    }
    private val config: PaymentConfig by lazy {
        intent.getParcelableExtra<PaymentConfig>(INTENT_EXTRA_CONFIG) as PaymentConfig
    }

    private val newCardEntered: MutableLiveData<Boolean> = MutableLiveData(false)
    private val firstFieldCardEntered: MutableLiveData<String> = MutableLiveData("")
    private val secondFieldCardEntered: MutableLiveData<String> = MutableLiveData("")
    private val firthFieldCardEntered: MutableLiveData<String> = MutableLiveData("")

    private var nfcReader: NFCReadDelegate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_new)
        toolbar.title.text = resources.getString(R.string.payrdr_title_payment)
        toolbar.arrow_back.setOnClickListener {
            onBackPressed()
        }
        configure(config)
    }

    private val jumpToNextInput: () -> Unit = {
        val firstNotCompletedField =
            activeInputFields().firstOrNull {
                it.errorMessage != null || it.text.toString().isEmpty()
            }
        if (firstNotCompletedField != null) {
            firstNotCompletedField.requestFocus()
        } else {
            hideKeyboard()
        }
    }

    private val nfcCardListener = object : NFCReadDelegate.NFCCardListener {
        override fun onCardReadSuccess(number: String, expiryDate: Date?) {
            cardNumberInput.setText(number)
            expiryDate?.let { date ->
                cardExpiryInput.setText(date.toStringExpDate())
            } ?: cardExpiryInput.setText("")
        }

        override fun onCardReadError(e: Exception) {
            val errorMessage = when (e) {
                is CommunicationException -> R.string.payrdr_nfc_do_not_move_card
                else -> R.string.payrdr_nfc_read_error
            }
            Toast.makeText(
                this@CardNewActivity,
                errorMessage,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun hideKeyboard() {
        val imm: InputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        val view: View = currentFocus ?: View(this)
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
    }

    @Suppress("LongMethod")
    private fun configure(config: PaymentConfig) {
        configureStateButton()
        bankCardView.setupUnknownBrand()
        cardNumberInput onInputStatusChanged jumpToNextInput
        cardExpiryInput onInputStatusChanged jumpToNextInput
        cardCodeInput onInputStatusChanged jumpToNextInput
        cardNumberInput onDisplayError { cardNumberInputLayout.error = it }
        cardExpiryInput onDisplayError { cardExpiryInputLayout.error = it }
        cardCodeInput onDisplayError { cardCodeInputLayout.error = it }
        cardNumberInput afterTextChanged { number ->
            cardResolver.resolve(
                number = number,
                withDelay = true
            )
        }
        doneButton.setOnClickListener { onDone() }
        when (config.cardSaveOptions) {
            CardSaveOptions.HIDE -> {
                switchBox.visibility = GONE
                switchBoxText.visibility = GONE
            }
            CardSaveOptions.YES_BY_DEFAULT -> {
                switchBox.visibility = VISIBLE
                switchBoxText.visibility = VISIBLE
                switchBox.isChecked = true
            }
            CardSaveOptions.NO_BY_DEFAULT -> {
                switchBox.visibility = VISIBLE
                switchBoxText.visibility = VISIBLE
                switchBox.isChecked = false
            }
        }
        val buttons: MutableList<Pair<Int, () -> Unit>> = mutableListOf()
        if (config.nfcScannerOptions == NfcScannerOptions.ENABLED && deviceHasNFC(this)) {
            nfcReader = NFCReadDelegate(NfcAdapter.getDefaultAdapter(applicationContext)).apply {
                nfcCardListener = this@CardNewActivity.nfcCardListener
            }
            buttons.add(R.drawable.icon_nfc to { showHintNFC() })
        } else {
            nfcReader = null
        }
        if (config.cameraScannerOptions == CameraScannerOptions.ENABLED && deviceHasCamera(this)) {
            buttons.add(R.drawable.icon_card to { startScanner() })
        }
        cardNumberInput.addRightButtons(buttons)
    }

    private fun configureStateButton() {
        newCardEntered.observe(this) {
            doneButton.isEnabled = it
        }
        firstFieldCardEntered.observe(this) {
            newCardEntered.value = firstFieldCardEntered.value!!.isNotEmpty() &&
                secondFieldCardEntered.value!!.isNotEmpty() &&
                firthFieldCardEntered.value!!.isNotEmpty()
        }
        secondFieldCardEntered.observe(this) {
            newCardEntered.value = firstFieldCardEntered.value!!.isNotEmpty() &&
                secondFieldCardEntered.value!!.isNotEmpty() &&
                firthFieldCardEntered.value!!.isNotEmpty()
        }
        firthFieldCardEntered.observe(this) {
            newCardEntered.value = firstFieldCardEntered.value!!.isNotEmpty() &&
                secondFieldCardEntered.value!!.isNotEmpty() &&
                firthFieldCardEntered.value!!.isNotEmpty()
        }

        cardNumberInput afterTextChanged { number ->
            setStartLogoPaymentSystem(number)
            firstFieldCardEntered.value = number
        }
        cardCodeInput afterTextChanged { code ->
            secondFieldCardEntered.value = code
        }
        cardExpiryInput afterTextChanged { expiry ->
            firthFieldCardEntered.value = expiry
        }
    }

    override fun onResume() {
        super.onResume()
        nfcReader?.onResume(this, javaClass)
    }

    override fun onPause() {
        super.onPause()
        nfcReader?.onPause(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        nfcReader?.onNewIntent(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finishWithResult(
                cryptogram = CryptogramData(
                    status = PaymentDataStatus.CANCELED,
                    seToken = "",
                    deletedCardsList = config.cardsToDelete
                )
            )
        }
        return true
    }

    @Suppress("TooGenericExceptionCaught")
    private fun setStartLogoPaymentSystem(pan: String) {
        try {
            if (pan == "") {
                cardNumberInputLayout.startIconDrawable = null
            }
            val logoResource = CardLogoAssetsResolver.resolveByPan(this, pan)
            if (logoResource != null) {
                val logoSVG = SVG.getFromAsset(resources.assets, logoResource)
                logoSVG.documentHeight = PAYMENT_SYSTEM_LOGO_HEIGHT
                logoSVG.documentWidth = PAYMENT_SYSTEM_LOGO_WIDTH
                val logoPictures = logoSVG.renderToPicture()
                val logoDrawable = PictureDrawable(logoPictures)
                cardNumberInputLayout.startIconDrawable = logoDrawable
            }
        } catch (exception: Exception) {
            Log.e("PAYRDRSDK", exception.message ?: exception.toString())
        }
    }

    private fun onDone() {
        val fields = activeInputFields()
        if (fields.all { it.errorMessage == null }) {
            preparePaymentData()
        } else {
            fields.filter { it.errorMessage != null }.forEach { it.showError = true }
        }
    }

    private fun activeInputFields(): MutableList<BaseTextInputEditText> {
        val fields = mutableListOf(cardNumberInput, cardExpiryInput, cardCodeInput)
        return fields
    }

    @Suppress("TooGenericExceptionCaught")
    private fun preparePaymentData() {
        workScope.launch(Dispatchers.Main) {
            try {
                val seToken = cryptogramProcessor.create(
                    order = config.order,
                    uuid = config.uuid,
                    timestamp = config.timestamp,
                    cardInfo = CardInfo(
                        identifier = CardPanIdentifier(
                            cardNumberInput.text.toString().digitsOnly()
                        ),
                        expDate = cardExpiryInput.text.toString().toExpDate(),
                        cvv = cardCodeInput.text.toString()
                    )
                )
                finishWithResult(
                    CryptogramData(
                        status = PaymentDataStatus.SUCCEEDED,
                        seToken = seToken,
                        info = PaymentInfoNewCard(
                            order = config.order,
                            saveCard = switchBox.isChecked,
                            holder = cardHolderInput.text.toString()
                        ),
                        deletedCardsList = config.cardsToDelete
                    )
                )
            } catch (e: Exception) {
                finishWithError(exception = e)
            }
        }
    }

    private fun startScanner() {
        val scanIntent = Intent(this, CardIOActivity::class.java).apply {
            putExtra(EXTRA_REQUIRE_EXPIRY, false)
            putExtra(EXTRA_REQUIRE_CARDHOLDER_NAME, false)
            putExtra(EXTRA_REQUIRE_CVV, false)
            putExtra(EXTRA_REQUIRE_POSTAL_CODE, false)
            putExtra(EXTRA_HIDE_CARDIO_LOGO, true)
            putExtra(EXTRA_USE_PAYPAL_ACTIONBAR_ICON, false)
            putExtra(EXTRA_SUPPRESS_CONFIRMATION, true)
            putExtra(CardIOActivity.EXTRA_SUPPRESS_MANUAL_ENTRY, true)
            putExtra(
                CardIOActivity.EXTRA_SCAN_INSTRUCTIONS,
                getString(R.string.payrdr_card_scan_message)
            )
            LocalizationSetting.getLanguage()?.toLanguageTag().let { languageTag ->
                putExtra(EXTRA_LANGUAGE_OR_LOCALE, languageTag)
            }
        }
        startActivityForResult(scanIntent, REQUEST_CODE_SCAN_CARD)
    }

    private fun showHintNFC() {
        if (nfcReader!!.isEnabled()) {
            Toast.makeText(this, R.string.payrdr_nfc_hold_card_to_phone, Toast.LENGTH_SHORT).show()
        } else {
            askToEnableNfc(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_SCAN_CARD) {
            if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
                data.getParcelableExtra<CreditCard>(CardIOActivity.EXTRA_SCAN_RESULT)
                    ?.let { result ->
                        cardNumberInput.setText(result.cardNumber)
                    }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {

        private const val PAYMENT_SYSTEM_LOGO_HEIGHT = 50f
        private const val PAYMENT_SYSTEM_LOGO_WIDTH = 80f

        /**
         * Prepares [Intent] to launch the new card payment screen.
         *
         * @param context to prepare intent.
         * @param config payment configuration.
         */
        fun prepareIntent(
            context: Context,
            config: PaymentConfig
        ): Intent = Intent(context, CardNewActivity::class.java).apply {
            putExtra(INTENT_EXTRA_CONFIG, config)
        }
    }
}
