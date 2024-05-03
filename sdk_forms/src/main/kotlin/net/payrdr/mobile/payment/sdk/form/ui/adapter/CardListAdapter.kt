package net.payrdr.mobile.payment.sdk.form.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.list_item_card_saved.view.bottomLineCardItem
import kotlinx.android.synthetic.main.list_item_card_saved.view.cardNumber
import kotlinx.android.synthetic.main.list_item_card_saved.view.cardSystem
import kotlinx.android.synthetic.main.list_item_card_saved.view.delete
import kotlinx.android.synthetic.main.list_item_card_saved.view.expiryDate
import kotlinx.android.synthetic.main.list_item_new_card.view.newCardItem
import kotlinx.android.synthetic.main.list_item_new_card.view.newCardText
import net.payrdr.mobile.payment.sdk.form.R
import net.payrdr.mobile.payment.sdk.form.model.Card
import net.payrdr.mobile.payment.sdk.form.ui.helper.CardLogoAssetsResolver
import net.payrdr.mobile.payment.sdk.form.ui.helper.CardNumberFormatter.maskCardNumber

internal class CardListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var showDelIcon: Boolean = false

    /**
     * List of cards to display.
     */
    var cards: List<Card> = emptyList()

    /**
     * Listener to track the selected card.
     */
    var cardSelectListener: CardSelectListener? = null

    var newCardSelectListener: NewCardSelectListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        when (viewType) {
            0 -> CardHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_card_saved, parent, false)
            )
            else -> NewCardHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_new_card, parent, false)
            )
        }

    override fun getItemViewType(position: Int): Int =
        if (position < cards.size) 0 else 1

    override fun getItemCount(): Int = cards.size + 1

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position < cards.size) {
            (holder as CardHolder).bind(cards[position], position)
        } else {
            (holder as NewCardHolder).bind()
        }
    }

    inner class CardHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        /**
         * Method of binding data [card] to the UI element of the list of cards.
         *
         * @param card card data to display in the list.
         */
        fun bind(card: Card, position: Int) {
            with(itemView) {
                cardNumber.text = maskCardNumber(itemView.context, card.pan)
                val logoResource = CardLogoAssetsResolver.resolveByPan(context, card.pan)
                if (logoResource != null) {
                    cardSystem.setImageAsset(logoResource)
                } else {
                    cardSystem.setImageDrawable(null)
                }
                if (showDelIcon) {
                    delete.visibility = View.VISIBLE
                    if (position == cards.lastIndex) {
                        bottomLineCardItem.visibility = View.GONE
                    }
                    delete.setImageResource(R.drawable.ic_delete)
                    delete.setOnClickListener {
                        cardSelectListener?.onCardDeleted(card)
                    }
                    setOnClickListener(null)
                } else {
                    delete.visibility = View.GONE
                    if (position == cards.lastIndex) {
                        bottomLineCardItem.visibility = View.VISIBLE
                    }
                    setOnClickListener {
                        cardSelectListener?.onCardSelected(card)
                    }
                }
                if (card.expiryDate != null) {
                    expiryDate.setExpiry(card.expiryDate)
                    expiryDate.visibility = View.VISIBLE
                } else {
                    expiryDate.visibility = View.INVISIBLE
                    expiryDate.setExpiry("")
                }
            }
        }
    }

    inner class NewCardHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        /**
         * Method of binding data [card] to the UI element of the list of cards.
         *
         * @param card card data to display in the list.
         */
        fun bind() {
            with(itemView) {
                if (showDelIcon) {
                    newCardItem.visibility = View.GONE
                } else {
                    newCardItem.setOnClickListener {
                        newCardSelectListener?.onCardSelected()
                    }
                    newCardItem.visibility = View.VISIBLE
                    newCardText.text = resources.getText(R.string.payrdr_button_card_new)
                }
            }
        }
    }

    /**
     * Interface for identifying the selected card.
     */
    interface CardSelectListener {

        /**
         * Called when the user selects a card.
         *
         * @param card the selected card.
         */
        fun onCardSelected(card: Card)

        /**
         * Called when the user selects a card for deletion.
         *
         * @param card the selected card.
         */
        fun onCardDeleted(card: Card)
    }

    /**
     * Interface for identifying the selected card.
     */
    interface NewCardSelectListener {

        /**
         * Called when the user selects a card.
         *
         * @param card the selected card.
         */
        fun onCardSelected()
    }
}
