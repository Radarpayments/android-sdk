package net.payrdr.mobile.payment.sdk.form.ui.widget

import android.content.Context
import android.util.AttributeSet
import kotlinx.android.synthetic.main.layout_bank_card.view.cardExpiry
import net.payrdr.mobile.payment.sdk.core.model.ExpiryDate
import net.payrdr.mobile.payment.sdk.core.utils.digitsOnly

/**
 * UI element for displaying the expiration date of the card.
 */
class ExpiryTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle
) : BaseTextView(context, attrs, defStyleAttr) {

    /**
     * Set the expiration date of the card.
     *
     * @param expiry card expiration date string in "MM/YY" format.
     */
    fun setExpiry(expiry: String) {
        val digitsOnly = StringBuilder(expiry.digitsOnly(EXPIRY_MASK_LENGTH)).apply {
            if (length >= EXPIRY_MASK_DIVIDER_INDEX) {
                insert(EXPIRY_MASK_DIVIDER_INDEX, EXPIRY_MASK_DIVIDER)
            }
        }
        val formatted = StringBuilder(EXPIRY_MASK)
        formatted.replace(0, digitsOnly.length, digitsOnly.toString())
        cardExpiry.text = formatted
    }

    /**
     * Set the expiration date of the card.
     *
     * @param expiry card expiry date.
     */
    fun setExpiry(expiry: ExpiryDate) =
        setExpiry(
            StringBuilder().apply {
                append(expiry.expMonth.toString().padStart(2, '0'))
                append((expiry.expYear % EXPIRY_YEAR_DIVIDER).toString().padStart(2, '0'))
            }.toString()
        )

    companion object {
        private const val EXPIRY_MASK_DIVIDER_INDEX = 2
        private const val EXPIRY_MASK_DIVIDER = "/"
        private const val EXPIRY_MASK = "••$EXPIRY_MASK_DIVIDER••"
        private const val EXPIRY_MASK_LENGTH = 4
        private const val EXPIRY_YEAR_DIVIDER = 100
    }
}
