package net.payrdr.mobile.payment.sdk.form.ui

import android.content.Context
import android.content.Intent
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View.VISIBLE
import android.widget.Toast
import com.caverock.androidsvg.SVG
import kotlinx.android.synthetic.main.activity_card_selected.addressLine1Input
import kotlinx.android.synthetic.main.activity_card_selected.addressLine1InputLayout
import kotlinx.android.synthetic.main.activity_card_selected.addressLine2Input
import kotlinx.android.synthetic.main.activity_card_selected.addressLine2InputLayout
import kotlinx.android.synthetic.main.activity_card_selected.addressLine3Input
import kotlinx.android.synthetic.main.activity_card_selected.addressLine3InputLayout
import kotlinx.android.synthetic.main.activity_card_selected.bankCardView
import kotlinx.android.synthetic.main.activity_card_selected.cardCodeInput
import kotlinx.android.synthetic.main.activity_card_selected.cardNumberInputLayout
import kotlinx.android.synthetic.main.activity_card_selected.cityInput
import kotlinx.android.synthetic.main.activity_card_selected.cityInputLayout
import kotlinx.android.synthetic.main.activity_card_selected.doneButton
import kotlinx.android.synthetic.main.activity_card_selected.emailInput
import kotlinx.android.synthetic.main.activity_card_selected.emailInputLayout
import kotlinx.android.synthetic.main.activity_card_selected.phoneNumberInput
import kotlinx.android.synthetic.main.activity_card_selected.phoneNumberInputLayout
import kotlinx.android.synthetic.main.activity_card_selected.postalCodeInput
import kotlinx.android.synthetic.main.activity_card_selected.postalCodeInputLayout
import kotlinx.android.synthetic.main.activity_card_selected.stateInput
import kotlinx.android.synthetic.main.activity_card_selected.stateInputLayout
import kotlinx.android.synthetic.main.activity_card_selected.toolbar
import kotlinx.android.synthetic.main.activity_card_selected.view.arrow_back
import kotlinx.android.synthetic.main.activity_card_selected.view.title
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.payrdr.mobile.payment.sdk.form.AdditionalFieldsHelper
import net.payrdr.mobile.payment.sdk.form.Constants
import net.payrdr.mobile.payment.sdk.form.R
import net.payrdr.mobile.payment.sdk.form.SDKForms
import net.payrdr.mobile.payment.sdk.form.model.AdditionalField
import net.payrdr.mobile.payment.sdk.form.model.Card
import net.payrdr.mobile.payment.sdk.form.model.CryptogramData
import net.payrdr.mobile.payment.sdk.form.model.FilledAdditionalPayerParams
import net.payrdr.mobile.payment.sdk.form.model.PaymentConfig
import net.payrdr.mobile.payment.sdk.form.model.PaymentDataStatus
import net.payrdr.mobile.payment.sdk.form.model.PaymentInfoBindCard
import net.payrdr.mobile.payment.sdk.form.model.PaymentSystem
import net.payrdr.mobile.payment.sdk.form.ui.helper.CardLogoAssetsResolver
import net.payrdr.mobile.payment.sdk.form.ui.helper.CardResolver
import net.payrdr.mobile.payment.sdk.form.ui.widget.BaseTextInputEditText
import net.payrdr.mobile.payment.sdk.form.ui.widget.BaseTextInputLayout
import net.payrdr.mobile.payment.sdk.form.utils.finishWithError
import net.payrdr.mobile.payment.sdk.form.utils.finishWithResult
import net.payrdr.mobile.payment.sdk.form.utils.onDisplayError

/**
 * Screen of the selected card from the list of card bindings.
 */
class CardSelectedActivity : BaseActivity() {

    private val config: PaymentConfig by lazy {
        intent.getParcelableExtra<PaymentConfig>(Constants.INTENT_EXTRA_CONFIG)!! as PaymentConfig
    }
    private val card: Card by lazy {
        intent.getParcelableExtra<Card>(Constants.INTENT_EXTRA_CARD)!! as Card
    }
    private val cardResolver: CardResolver by lazy {
        CardResolver(
            bankCardView = bankCardView,
            cardInfoProvider = SDKForms.sdkConfig.cardInfoProvider
        )
    }
    private val mandatoryAdditionalFields = mutableSetOf<BaseTextInputEditText>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_selected)
        toolbar.title.text = resources.getString(R.string.payrdr_title_payment)
        toolbar.arrow_back.setOnClickListener {
            onBackPressed()
        }
        setStartLogoPaymentSystem()
        configureAdditionalFields()
        cardResolver.resolve(card.pan)
        config.storedPaymentMethodCVCRequired.let { cvcRequired ->
            cardCodeInput.isEnabled = cvcRequired
        }
        cardCodeInput onDisplayError { message ->
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
        doneButton.setOnClickListener {
            onDone()
        }
    }

    private fun configureAdditionalFields() {
        val paymentSystem = AdditionalFieldsHelper.resolvePaymentSystem(card.pan)
        when (paymentSystem) {
            PaymentSystem.VISA -> configureAdditionalParams(config.fieldsNeedToBeFilledForVisa)
            PaymentSystem.MASTERCARD -> configureAdditionalParams(config.fieldsNeedToBeFilledForMastercard)
            PaymentSystem.OTHER_SYSTEM -> {}
        }
    }

    private fun configureAdditionalParams(fieldsNeedToBeFilled: List<AdditionalField>) {
        fieldsNeedToBeFilled.forEach { additionalField ->
            val (fieldInputLayoutId, fieldInputId) = AdditionalFieldsHelper.resolveFieldIdByName(
                additionalField.fieldName
            )
            val fieldInputLayout = findViewById<BaseTextInputLayout>(fieldInputLayoutId)
            val fieldInput = findViewById<BaseTextInputEditText>(fieldInputId)
            if (additionalField.isMandatory) mandatoryAdditionalFields.add(fieldInput)
            AdditionalFieldsHelper.configureField(additionalField, fieldInputLayout, fieldInput)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finishWithResult(
                cryptogram = CryptogramData(
                    status = PaymentDataStatus.CANCELED,
                )
            )
        }
        return true
    }

    private fun prepareAdditionalParam(
        inputLayout: BaseTextInputLayout,
        input: BaseTextInputEditText
    ): String? {
        if (inputLayout.visibility != VISIBLE || input.text?.trim().isNullOrEmpty()) return null
        return input.text.toString()
    }

    private fun onDone() {
        val fields = mandatoryAdditionalFields
        if (config.storedPaymentMethodCVCRequired) {
            fields.add(cardCodeInput)
        }
        if (fields.all { it.errorMessage == null }) {
            preparePaymentData()
        } else {
            fields.filter { it.errorMessage != null }.forEach { it.showError = true }
        }
    }

    private fun setStartLogoPaymentSystem() {
        val logoResource = CardLogoAssetsResolver.resolveByPan(this, card.pan)
        if (logoResource != null) {
            val logoSVG = SVG.getFromAsset(resources.assets, logoResource)
            logoSVG.documentHeight = PAYMENT_SYSTEM_LOGO_HEIGHT
            logoSVG.documentWidth = PAYMENT_SYSTEM_LOGO_WIDTH
            val logoPictures = logoSVG.renderToPicture()
            val logoDrawable = PictureDrawable(logoPictures)
            cardNumberInputLayout.startIconDrawable = logoDrawable
        }
    }

    private fun prepareAdditionalFields() = FilledAdditionalPayerParams(
        city = prepareAdditionalParam(cityInputLayout, cityInput),
        country = prepareAdditionalParam(cityInputLayout, cityInput),
        addressLine1 = prepareAdditionalParam(
            addressLine1InputLayout,
            addressLine1Input
        ),
        addressLine2 = prepareAdditionalParam(
            addressLine2InputLayout,
            addressLine2Input
        ),
        addressLine3 = prepareAdditionalParam(
            addressLine3InputLayout,
            addressLine3Input
        ),
        postalCode = prepareAdditionalParam(
            postalCodeInputLayout,
            postalCodeInput
        ),
        state = prepareAdditionalParam(stateInputLayout, stateInput),
        email = prepareAdditionalParam(emailInputLayout, emailInput),
        phone = prepareAdditionalParam(
            phoneNumberInputLayout,
            phoneNumberInput
        )
    )

    @Suppress("TooGenericExceptionCaught")
    private fun preparePaymentData() {
        workScope.launch(Dispatchers.Main) {
            try {
                finishWithResult(
                    CryptogramData(
                        status = PaymentDataStatus.SUCCEEDED,
                        info = PaymentInfoBindCard(
                            order = config.order,
                            bindingId = card.bindingId,
                            cvc = cardCodeInput.text.toString(),
                            filledAdditionalPayerParams = prepareAdditionalFields()
                        ),
                    )
                )
            } catch (e: Exception) {
                finishWithError(exception = e)
            }
        }
    }

    companion object {

        private const val PAYMENT_SYSTEM_LOGO_HEIGHT = 50f
        private const val PAYMENT_SYSTEM_LOGO_WIDTH = 80f

        /**
         * Prepares [Intent] to launch the payment screen of the selected card.
         *
         * @param context to prepare intent.
         * @param config payment configuration.
         */
        fun prepareIntent(
            context: Context,
            config: PaymentConfig,
            card: Card
        ): Intent = Intent(context, CardSelectedActivity::class.java).apply {
            putExtra(Constants.INTENT_EXTRA_CONFIG, config)
            putExtra(Constants.INTENT_EXTRA_CARD, card)
        }
    }
}
