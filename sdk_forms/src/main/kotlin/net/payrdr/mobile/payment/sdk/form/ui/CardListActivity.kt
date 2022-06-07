package net.payrdr.mobile.payment.sdk.form.ui

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_card_list.cardList
import kotlinx.android.synthetic.main.activity_card_list.doneButton
import kotlinx.android.synthetic.main.activity_card_list.editCardsList
import kotlinx.android.synthetic.main.activity_card_new.toolbar
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
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.payrdr_title_card_list)
        }
        cardsAdapter.cards = config.cards.toList()
        cardList.apply {
            adapter = cardsAdapter
            layoutManager = LinearLayoutManager(this@CardListActivity)
            addItemDecoration(DividerItemDecoration(context, RecyclerView.VERTICAL))
        }
        doneButton.setOnClickListener {
            openNewCard()
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
        val builder: AlertDialog.Builder = AlertDialog.Builder(this@CardListActivity)
        val messageDialog = getString(R.string.payrdr_card_deleting_question, card.pan)
        val dialogClickListener: DialogInterface.OnClickListener =
            DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        config.cardsToDelete.add(card)
                        changeEditCardState()
                    }
                }
            }
        builder.setMessage(messageDialog)
            .setPositiveButton(getString(R.string.payrdr_yes_title), dialogClickListener)
            .setNegativeButton(getString(R.string.payrdr_no_title), dialogClickListener)
            .show()
    }

    private fun changeEditCardState() {
        cardsAdapter.showDelIcon = !cardsAdapter.showDelIcon
        updateCardsListView(config)
        doneButton.visibility = if (cardsAdapter.showDelIcon) View.GONE else View.VISIBLE
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
