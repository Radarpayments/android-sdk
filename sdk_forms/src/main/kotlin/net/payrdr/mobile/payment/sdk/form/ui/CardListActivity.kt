package net.payrdr.mobile.payment.sdk.form.ui

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_card_list.cardList
import kotlinx.android.synthetic.main.activity_card_list.editCardsList
import kotlinx.android.synthetic.main.activity_card_list.toolbar
import kotlinx.android.synthetic.main.activity_card_list.view.arrow_back
import kotlinx.android.synthetic.main.activity_card_list.view.title
import net.payrdr.mobile.payment.sdk.form.Constants
import net.payrdr.mobile.payment.sdk.form.R
import net.payrdr.mobile.payment.sdk.form.model.Card
import net.payrdr.mobile.payment.sdk.form.model.CardDeleteOptions
import net.payrdr.mobile.payment.sdk.form.model.CryptogramData
import net.payrdr.mobile.payment.sdk.form.model.PaymentConfig
import net.payrdr.mobile.payment.sdk.form.model.PaymentDataStatus
import net.payrdr.mobile.payment.sdk.form.ui.adapter.CardListAdapter
import net.payrdr.mobile.payment.sdk.form.utils.finishWithResult

/**
 * Binding card list screen.
 */
class CardListActivity : BaseActivity() {

    private val cardsAdapter = CardListAdapter()
    private val config: PaymentConfig by lazy {
        intent.getParcelableExtra<PaymentConfig>(Constants.INTENT_EXTRA_CONFIG) as PaymentConfig
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_list)
        toolbar.title.text = resources.getString(R.string.payrdr_title_card_list)
        toolbar.arrow_back.setOnClickListener {
            onBackPressed()
        }
        cardsAdapter.cards = config.cards.toList()
        cardList.apply {
            adapter = cardsAdapter
            layoutManager = LinearLayoutManager(this@CardListActivity)
        }
        if (config.cardDeleteOptions == CardDeleteOptions.YES_DELETE) {
            editCardsList.visibility = View.VISIBLE
            editCardsList.setOnClickListener {
                changeEditCardState()
            }
        } else {
            editCardsList.visibility = View.GONE
        }
        cardsAdapter.cardSelectListener = object : CardListAdapter.CardSelectListener {
            override fun onCardSelected(card: Card) {
                openSavedCard(card)
            }

            override fun onCardDeleted(card: Card) {
                deleteCardFromList(card)
            }
        }
        cardsAdapter.newCardSelectListener = object : CardListAdapter.NewCardSelectListener {
            override fun onCardSelected() {
                openNewCard()
            }
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

    private fun deleteCardFromList(card: Card) {
        val dialogClickListener: DialogInterface.OnClickListener =
            DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        config.cardsToDelete.add(card)
                        updateCardsListView(config)
                    }
                }
            }
        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.payrdr_alert_dialog_title))
            .setMessage(resources.getString(R.string.payrdr_alert_dialog_text))
            .setPositiveButton(resources.getString(R.string.payrdr_alert_dialog_delete), dialogClickListener)
            .setNegativeButton(resources.getString(R.string.payrdr_alert_dialog_cancel), dialogClickListener)
            .show()
    }

    private fun changeEditCardState() {
        if (editCardsList.text == resources.getString(R.string.payrdr_save_changes)) {
            editCardsList.text = resources.getString(R.string.payrdr_title_edit_card_list)
        } else {
            editCardsList.text = resources.getString(R.string.payrdr_save_changes)
        }
        cardsAdapter.showDelIcon = !cardsAdapter.showDelIcon
        updateCardsListView(config)
    }

    private fun updateCardsListView(config: PaymentConfig) {
        cardsAdapter.cards = config.cards.subtract(config.cardsToDelete).toList()
        cardsAdapter.notifyDataSetChanged()
    }

    private fun openNewCard() {
        startActivity(
            CardNewActivity.prepareIntent(this, config).also {
                it.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
            }
        )
        finish()
    }

    private fun openSavedCard(card: Card) {
        startActivity(
            CardSelectedActivity.prepareIntent(
                this@CardListActivity,
                config,
                card
            ).also {
                it.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
            }
        )
        finish()
    }

    companion object {

        /**
         * Prepares [Intent] to launch the linked card list screen.
         *
         * @param context to prepare intent.
         * @param config payment configuration.
         */
        fun prepareIntent(
            context: Context,
            config: PaymentConfig
        ): Intent = Intent(context, CardListActivity::class.java).apply {
            putExtra(Constants.INTENT_EXTRA_CONFIG, config)
        }
    }
}
