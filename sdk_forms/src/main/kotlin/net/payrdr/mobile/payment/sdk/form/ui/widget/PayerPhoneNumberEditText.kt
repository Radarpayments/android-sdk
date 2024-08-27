package net.payrdr.mobile.payment.sdk.form.ui.widget

import android.content.Context
import android.text.InputFilter
import android.text.InputType
import android.text.Spanned
import android.util.AttributeSet
import net.payrdr.mobile.payment.sdk.core.utils.digitsOnly
import net.payrdr.mobile.payment.sdk.core.validation.PhoneNumberValidator
import net.payrdr.mobile.payment.sdk.form.ui.adapter.TextWatcherAdapter

/**
 * UI element for entering payer mobile phone.
 */
class PayerPhoneNumberEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : BaseTextInputEditText(context, attrs) {

    private val phoneNumberValidator = PhoneNumberValidator(context)

    init {
        inputType = InputType.TYPE_CLASS_PHONE
        maxLines = 1
        isSingleLine = true

        filters = arrayOf(
            object : InputFilter {
                private val regExp = "^\\+?\\d*\$".toRegex()
                override fun filter(
                    source: CharSequence,
                    start: Int,
                    end: Int,
                    dest: Spanned?,
                    dstart: Int,
                    dend: Int
                ): CharSequence {
                    val newText = dest.toString() + source
                    if (source == "" || newText.matches(regExp)) {
                        return source
                    }
                    return ""
                }
            }
        )

        addTextChangedListener(object : TextWatcherAdapter() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validate(s!!.toString().digitsOnly())
            }
        })
        validate(text.toString().digitsOnly())
    }

    private fun validate(value: String) {
        errorMessage = phoneNumberValidator.validate(value).errorMessage
    }
}
