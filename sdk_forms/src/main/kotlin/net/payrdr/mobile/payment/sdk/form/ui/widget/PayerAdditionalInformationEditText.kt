package net.payrdr.mobile.payment.sdk.form.ui.widget

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import net.payrdr.mobile.payment.sdk.core.validation.PayerAdditionalInformationValidator
import net.payrdr.mobile.payment.sdk.form.ui.adapter.TextWatcherAdapter

/**
 * UI element for entering information about payer.
 */
class PayerAdditionalInformationEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : BaseTextInputEditText(context, attrs) {

    private var payerAdditionalInformationValidator: PayerAdditionalInformationValidator? = null

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
        errorMessage = payerAdditionalInformationValidator?.validate(value)?.errorMessage
    }

    /**
     * Adds validator for this field.
     */
    fun setValidator() {
        if (payerAdditionalInformationValidator == null)
            payerAdditionalInformationValidator = PayerAdditionalInformationValidator(context)
        validate(this.text.toString())
    }
}
