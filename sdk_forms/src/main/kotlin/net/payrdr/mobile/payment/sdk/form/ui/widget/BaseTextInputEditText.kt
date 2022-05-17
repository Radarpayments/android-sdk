package net.payrdr.mobile.payment.sdk.form.ui.widget

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputEditText

/**
 * Base component for implementing input fields.
 */
open class BaseTextInputEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.editTextStyle
) : TextInputEditText(context, attrs, defStyleAttr) {

    /**
     * Listener to output an error message.
     */
    var errorMessageListener: ErrorMessageListener? = null

    /**
     * A listener to determine the state of filling a field.
     */
    var inputStatusListener: InputStatusListener? = null

    /**
     * The current data entry error message.
     */
    var errorMessage: String? = null
        protected set(message) {
            errorMessageListener?.displayError(message.takeIf { showError })
            field = message
            if (message == null) {
                inputStatusListener?.inputCompleted()
            }
        }

    /**
     * Setting whether to display a data entry error.
     */
    var showError: Boolean = false
        set(needShow) {
            errorMessage?.let { message ->
                errorMessageListener?.displayError(message.takeIf { needShow })
            }
            field = needShow
        }

    /**
     * An interface for displaying an error message.
     */
    interface ErrorMessageListener {

        /**
         * Called to print the current error message.
         *
         * @param message error message if present, null otherwise.
         */
        fun displayError(message: String?)
    }

    /**
     * An interface for determining the state of filling a field.
     */
    interface InputStatusListener {

        /**
         * Called after the last character is entered, when the field is filled in completely
         * and correctly.
         */
        fun inputCompleted()
    }
}
