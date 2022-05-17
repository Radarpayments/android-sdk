package net.payrdr.mobile.payment.sdk.form.ui.widget

import android.content.Context
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.util.AttributeSet
import net.payrdr.mobile.payment.sdk.core.utils.digitsOnly
import net.payrdr.mobile.payment.sdk.core.validation.CardExpiryValidator
import net.payrdr.mobile.payment.sdk.form.ui.adapter.TextWatcherAdapter

/**
 * UI element for entering the expiration date of the card.
 */
class CardExpiryEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.editTextStyle
) : BaseTextInputEditText(context, attrs, defStyleAttr) {

    private var cardExpiryValidator: CardExpiryValidator = CardExpiryValidator(context)

    init {
        filters = arrayOf(LengthFilter(EXPIRED_MAX_LENGTH))
        inputType = InputType.TYPE_CLASS_NUMBER
        maxLines = 1
        isSingleLine = true

        addTextChangedListener(object : TextWatcherAdapter() {

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var value = s.toString()

                if (value.length > MONTH_END_POSITION && !value.contains(DIVIDER)) {
                    value = StringBuilder(value.digitsOnly())
                        .insert(MONTH_END_POSITION, DIVIDER)
                        .toString()
                    setText(value)
                    setSelection(value.length)
                }
                validate(value)
            }
        })
        validate(text.toString())
    }

    private fun validate(value: String) {
        errorMessage = cardExpiryValidator.validate(value).errorMessage
    }

    companion object {
        private const val EXPIRED_MAX_LENGTH = 5
        private const val MONTH_END_POSITION = 2
        private const val DIVIDER = "/"
    }
}
