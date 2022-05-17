package net.payrdr.mobile.payment.sdk.form.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.layout_bank_card.view.cardExpiry
import kotlinx.android.synthetic.main.layout_bank_card.view.cardNumber
import kotlinx.android.synthetic.main.layout_bank_card.view.cardSystem
import kotlinx.android.synthetic.main.list_item_card_saved.view.arrow
import net.payrdr.mobile.payment.sdk.form.R
import net.payrdr.mobile.payment.sdk.form.model.Card
import net.payrdr.mobile.payment.sdk.form.ui.helper.CardLogoAssetsResolver
import net.payrdr.mobile.payment.sdk.form.ui.helper.CardNumberFormatter.maskCardNumber

internal class CardListAdapter : RecyclerView.Adapter<CardListAdapter.CardHolder>() {

    var showDelIcon: Boolean = false

    /**
     * List of cards to display.
     */
    var cards: List<Card> = emptyList()

    /**
     * Listener to track the selected card.
     */
    var cardSelectListener: CardSelectListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        CardHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_card_saved, parent, false)
        )

    override fun getItemCount(): Int = cards.size

    override fun onBindViewHolder(holder: CardListAdapter.CardHolder, position: Int) {
        holder.bind(cards[position])
    }

    inner class CardHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        /**
         * Method of binding data [card] to the UI element of the list of cards.
         *
         * @param card card data to display in the list.
         */
        fun bind(card: Card) {
            with(itemView) {
                cardNumber.text = maskCardNumber(itemView.context, card.pan)
                val logoResource = CardLogoAssetsResolver.resolveByPan(context, card.pan)
                if (logoResource != null) {
                    cardSystem.setImageAsset(logoResource)
                } else {
                    cardSystem.setImageDrawable(null)
                }
                if (showDelIcon) {
                    arrow.setImageResource(R.drawable.ic_delete_red)
                    arrow.setOnClickListener {
                        cardSelectListener?.onCardDeleted(card)
                    }
                    setOnClickListener(null)
                } else {
                    arrow.setImageResource(R.drawable.ic_arrow_right)
                    setOnClickListener {
                        cardSelectListener?.onCardSelected(card)
                    }
                }
                if (card.expiryDate != null) {
                    cardExpiry.setExpiry(card.expiryDate)
                    cardExpiry.visibility = VISIBLE
                } else {
                    cardExpiry.visibility = INVISIBLE
                    cardExpiry.setExpiry("")
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
}
