package net.payrdr.mobile.payment.sdk.form.ui.widget

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputLayout

/**
 * Base component for implementing markup for input fields.
 */
open class BaseTextInputLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.textInputStyle
) : TextInputLayout(context, attrs, defStyleAttr)
