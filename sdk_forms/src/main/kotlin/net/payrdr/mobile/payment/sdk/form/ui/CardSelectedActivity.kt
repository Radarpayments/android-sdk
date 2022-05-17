package net.payrdr.mobile.payment.sdk.form.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_card_new.bankCardView
import kotlinx.android.synthetic.main.activity_card_new.cardCodeInput
import kotlinx.android.synthetic.main.activity_card_new.doneButton
import kotlinx.android.synthetic.main.activity_card_new.toolbar
import kotlinx.android.synthetic.main.activity_card_selected.cardCodeInputLayout
import kotlinx.android.synthetic.main.list_item_card_saved.view.cardExpiry
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
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.payrdr_title_payment)
        }
        cardResolver.resolve(card.pan)
        config.buttonText?.let { text ->
            doneButton.text = text
        }
        config.bindingCVCRequired.let { cvcRequired ->
            cardCodeInputLayout.visibility = if (cvcRequired) VISIBLE else INVISIBLE
        }
        bankCardView.apply {
            setNumber(card.pan)
            enableHolderName(false)
            if (card.expiryDate != null) {
                cardExpiry.setExpiry(card.expiryDate!!)
                cardExpiry.visibility = VISIBLE
            } else {
                cardExpiry.visibility = INVISIBLE
                cardExpiry.setExpiry("")
            }
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
                    cryptogram = "",
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

    @Suppress("TooGenericExceptionCaught")
    private fun preparePaymentData() {
        workScope.launch(Dispatchers.Main) {
            try {
                val cryptogram = cryptogramProcessor.create(
                    order = config.order,
                    uuid = config.uuid,
                    timestamp = config.timestamp,
                    cardInfo = CardInfo(
                        identifier = CardBindingIdIdentifier(card.bindingId),
                        cvv = cardCodeInput.text.toString()
                    )
                )
                finishWithResult(
                    CryptogramData(
                        status = PaymentDataStatus.SUCCEEDED,
                        cryptogram = cryptogram,
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
