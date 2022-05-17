package net.payrdr.mobile.payment.sdk.form.utils

import android.text.Editable
import net.payrdr.mobile.payment.sdk.form.ui.adapter.TextWatcherAdapter
import net.payrdr.mobile.payment.sdk.form.ui.widget.BaseTextInputEditText

/**
 * Installing a handler to display the error message.
 *
 * @param block handler for displaying an error message.
 */
infix fun BaseTextInputEditText.onDisplayError(block: (s: String?) -> Unit) {
    errorMessageListener = object : BaseTextInputEditText.ErrorMessageListener {
        override fun displayError(message: String?) {
            block(message)
        }
    }
}

/**
 * Installing a handler to handle the event of correct completion of the field.
 *
 * @param block handler for correct completion of field filling.
 */
infix fun BaseTextInputEditText.onInputStatusChanged(block: () -> Unit) {
    inputStatusListener = object : BaseTextInputEditText.InputStatusListener {
        override fun inputCompleted() {
            block()
        }
    }
}

/**
 * Setting up a handler to handle the field value change event.
 *
 * @param block handler for the field value change event.
 */
infix fun BaseTextInputEditText.afterTextChanged(block: (s: String) -> Unit) {
    addTextChangedListener(object : TextWatcherAdapter() {
        override fun afterTextChanged(s: Editable?) {
            block(s.toString())
        }
    })
}
