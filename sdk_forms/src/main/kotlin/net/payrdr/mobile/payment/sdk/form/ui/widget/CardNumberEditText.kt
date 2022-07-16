package net.payrdr.mobile.payment.sdk.form.ui.widget

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import net.payrdr.mobile.payment.sdk.core.utils.digitsOnly
import net.payrdr.mobile.payment.sdk.core.validation.CardNumberValidator
import net.payrdr.mobile.payment.sdk.form.ui.adapter.TextWatcherAdapter
import kotlin.math.min

/**
 * UI element for entering the card number.
 */
open class CardNumberEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : BaseTextInputEditText(context, attrs) {

    private var cardNumberValidator: CardNumberValidator = CardNumberValidator(context)

    init {
        inputType = InputType.TYPE_CLASS_NUMBER
        maxLines = 1
        isSingleLine = true
        addTextChangedListener(object : TextWatcherAdapter() {

            private var lock = false

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentText: String = text.toString()
                if (lock || currentText.isEmpty()) {
                    return
                }
                lock = true
                val selection = selectionStart
                val digitsOnly = currentText
                    .digitsOnly(NUMBER_MAX_LENGTH)
                val formatted = StringBuilder(digitsOnly)
                var deltaLength = 0
                for (position in SPACE_POSITIONS) {
                    if (formatted.length > position) {
                        formatted.insert(position, SPACE).toString()
                        if (selection != -1 && selection >= position) {
                            deltaLength += 1
                        }
                    }
                }
                setText(formatted)
                if (selection == -1) {
                    setSelection(text.toString().length)
                } else {
                    setSelection(min(selection + deltaLength, text.toString().length))
                }
                lock = false
                validate(digitsOnly)
            }
        })
        validate(text.toString())
    }

    private fun validate(value: String) {
        errorMessage = cardNumberValidator.validate(value).errorMessage
    }

    companion object {
        private const val SPACE = " "
        private const val NUMBER_MAX_LENGTH = 19
        private val SPACE_POSITIONS = intArrayOf(4, 9, 14)
    }
}
