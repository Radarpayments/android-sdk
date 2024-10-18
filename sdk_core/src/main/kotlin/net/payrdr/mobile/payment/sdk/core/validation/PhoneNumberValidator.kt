package net.payrdr.mobile.payment.sdk.core.validation

import android.content.Context
import net.payrdr.mobile.payment.sdk.core.R
import net.payrdr.mobile.payment.sdk.core.validation.rules.RegExRule
import net.payrdr.mobile.payment.sdk.core.validation.rules.StringRequiredRule

/**
 * Phone number validator.
 * @param context context for getting string resources.
 */
class PhoneNumberValidator(context: Context):BaseValidator<String>() {

    init {
        addRules(
            StringRequiredRule(
                code = ValidationCodes.required,
                message = context.getString(R.string.payrdr_not_empty_required)
            ),
            RegExRule(
                code = ValidationCodes.invalidFormat,
                message = context.getString(R.string.payrdr_incorrect_phone_number),
                regex = PATTERN
            )
        )
    }

    companion object {
        private val PATTERN = "^\\+?[0-9]+\$".toRegex()
    }
}
