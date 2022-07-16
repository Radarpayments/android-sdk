package net.payrdr.mobile.payment.sdk.form.ui.widget

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.button.MaterialButton

/**
 * UI element for displaying the Button.
 */
class GeneralButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : MaterialButton(context, attrs) {

    companion object {

        private const val CLICKABLE = 1f
        private const val NOT_CLICKABLE = 0.24f
    }

    override fun setClickable(clickable: Boolean) {
        alpha = if (clickable) {
            CLICKABLE
        } else {
            NOT_CLICKABLE
        }
        super.setClickable(clickable)
    }
}
