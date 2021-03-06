package net.payrdr.mobile.payment.sdk.form.ui.widget

import android.content.Context
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.text.Spanned
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.View.OnFocusChangeListener
import net.payrdr.mobile.payment.sdk.core.utils.digitsOnly
import net.payrdr.mobile.payment.sdk.core.validation.CardCodeValidator
import net.payrdr.mobile.payment.sdk.form.ui.adapter.TextWatcherAdapter

/**
 * UI element for entering the secret code of the card.
 */
class CardCodeEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : BaseTextInputEditText(context, attrs) {

    private var cardCodeValidator: CardCodeValidator = CardCodeValidator(context)

    init {
        inputType = InputType.TYPE_CLASS_NUMBER
        transformationMethod = PasswordTransformationMethod()
        onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                transformationMethod = PasswordTransformationMethod()
            }
        }
        filters = arrayOf(
            object : InputFilter {
                override fun filter(
                    source: CharSequence,
                    start: Int,
                    end: Int,
                    dest: Spanned?,
                    dstart: Int,
                    dend: Int
                ): CharSequence {
                    if (source == "" || source.toString().matches("[0-9]+".toRegex())) {
                        return source
                    }
                    return ""
                }
            },
            LengthFilter(CODE_MAX_LENGTH)
        )

        addTextChangedListener(object : TextWatcherAdapter() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validate(s!!.toString().digitsOnly())
            }
        })
        validate(text.toString().digitsOnly())
    }

    private fun validate(value: String) {
        errorMessage = cardCodeValidator.validate(value).errorMessage
    }

    companion object {
        private const val CODE_MAX_LENGTH = 3
    }
}
