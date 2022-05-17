package net.payrdr.mobile.payment.sdk.core.validation

import android.content.Context
import net.payrdr.mobile.payment.sdk.core.R
import net.payrdr.mobile.payment.sdk.core.validation.rules.RegExRule
import net.payrdr.mobile.payment.sdk.core.validation.rules.StringRequiredRule

/**
 *  Bind ID value validator.
 *
 *  @param context context for getting string resources.
 * */

class CardBindingIdValidator(context: Context) : BaseValidator<String>() {

    init {
        addRules(
            StringRequiredRule(
                code = ValidationCodes.required,
                message = context.getString(R.string.payrdr_binding_required)
            ),
            RegExRule(
                code = ValidationCodes.invalid,
                message = context.getString(R.string.payrdr_binding_incorrect),
                regex = PATTERN.toRegex()
            )
        )
    }

    companion object {
        private const val PATTERN = "\\S+"
    }
}
