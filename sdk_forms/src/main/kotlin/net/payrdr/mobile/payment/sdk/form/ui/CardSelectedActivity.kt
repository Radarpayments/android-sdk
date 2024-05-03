package net.payrdr.mobile.payment.sdk.form.ui

import android.content.Context
import android.content.Intent
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import com.caverock.androidsvg.SVG
import kotlinx.android.synthetic.main.activity_card_selected.bankCardView
import kotlinx.android.synthetic.main.activity_card_selected.cardCodeInput
import kotlinx.android.synthetic.main.activity_card_selected.cardNumberInputLayout
import kotlinx.android.synthetic.main.activity_card_selected.doneButton
import kotlinx.android.synthetic.main.activity_card_selected.toolbar
import kotlinx.android.synthetic.main.activity_card_selected.view.arrow_back
import kotlinx.android.synthetic.main.activity_card_selected.view.title
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.payrdr.mobile.payment.sdk.core.model.CardBindingIdIdentifier
import net.payrdr.mobile.payment.sdk.core.model.CardInfo
import net.payrdr.mobile.payment.sdk.form.Constants
import net.payrdr.mobile.payment.sdk.form.R
import net.payrdr.mobile.payment.sdk.form.SDKForms
import net.payrdr.mobile.payment.sdk.form.component.CryptogramProcessor
import net.payrdr.mobile.payment.sdk.form.model.Card
import net.payrdr.mobile.payment.sdk.form.model.CryptogramData
import net.payrdr.mobile.payment.sdk.form.model.PaymentConfig
import net.payrdr.mobile.payment.sdk.form.model.PaymentDataStatus
import net.payrdr.mobile.payment.sdk.form.model.PaymentInfoBindCard
import net.payrdr.mobile.payment.sdk.form.ui.helper.CardLogoAssetsResolver
import net.payrdr.mobile.payment.sdk.form.ui.helper.CardResolver
import net.payrdr.mobile.payment.sdk.form.utils.finishWithError
import net.payrdr.mobile.payment.sdk.form.utils.finishWithResult
import net.payrdr.mobile.payment.sdk.form.utils.onDisplayError

/**
 * Screen of the selected card from the list of card bindings.
 */
class CardSelectedActivity : BaseActivity() {

    private var cryptogramProcessor: CryptogramProcessor = SDKForms.cryptogramProcessor
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_selected)
        toolbar.title.text = resources.getString(R.string.payrdr_title_payment)
        toolbar.arrow_back.setOnClickListener {
            onBackPressed()
        }
        setStartLogoPaymentSystem()
        cardResolver.resolve(card.pan)
        config.bindingCVCRequired.let { cvcRequired ->
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

    private fun onDone() {
        if (!config.bindingCVCRequired) {
            preparePaymentData()
        } else if (cardCodeInput.errorMessage != null) {
            cardCodeInput.showError = true
        } else {
            preparePaymentData()
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

    @Suppress("TooGenericExceptionCaught")
    private fun preparePaymentData() {
        workScope.launch(Dispatchers.Main) {
            try {
                val seToken = cryptogramProcessor.create(
                    order = config.order,
                    uuid = config.uuid,
                    timestamp = config.timestamp,
                    cardInfo = CardInfo(
                        identifier = CardBindingIdIdentifier(card.bindingId),
                        cvv = cardCodeInput.text.toString(),
                        cardHolder = null,
                    ),
                    registeredFrom = config.registeredFrom,
                )
                finishWithResult(
                    CryptogramData(
                        status = PaymentDataStatus.SUCCEEDED,
                        seToken = seToken,
                        info = PaymentInfoBindCard(
                            order = config.order,
                            bindingId = card.bindingId
                        ),
                        deletedCardsList = config.cardsToDelete
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
