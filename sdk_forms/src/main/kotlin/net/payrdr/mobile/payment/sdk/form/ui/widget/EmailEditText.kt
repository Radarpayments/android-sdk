package net.payrdr.mobile.payment.sdk.form.ui.widget

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import net.payrdr.mobile.payment.sdk.core.validation.EmailValidator
import net.payrdr.mobile.payment.sdk.form.ui.adapter.TextWatcherAdapter

/**
 * UI element for entering payer email.
 */
class EmailEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : BaseTextInputEditText(context, attrs) {

    private val emailValidator = EmailValidator(context)

    init {
        maxLines = 1
        isSingleLine = true
        inputType = InputType.TYPE_CLASS_TEXT

        addTextChangedListener(object : TextWatcherAdapter() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validate(s!!.toString())
            }
        })
        validate(text.toString())
    }
    private fun validate(value: String) {
        errorMessage = emailValidator.validate(value).errorMessage
    }
}
