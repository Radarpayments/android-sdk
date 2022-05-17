package net.payrdr.mobile.payment.sdk.core.validation

import android.content.Context
import net.payrdr.mobile.payment.sdk.core.R
import net.payrdr.mobile.payment.sdk.core.validation.rules.RegExRule
import net.payrdr.mobile.payment.sdk.core.validation.rules.StringLengthRule
import net.payrdr.mobile.payment.sdk.core.validation.rules.StringRequiredRule

/**
 *  Order number value validator.
 *
 *  @param context context for getting string resources.
 * */

class OrderNumberValidator(context: Context) : BaseValidator<String>() {

    init {
        addRules(
            StringRequiredRule(
                code = ValidationCodes.required,
                message = context.getString(R.string.payrdr_order_incorrect_length)
            ),
            StringLengthRule(
                code = ValidationCodes.invalid,
                message = context.getString(R.string.payrdr_order_incorrect_length),
                minLength = MIN_LENGTH
            ),
            RegExRule(
                code = ValidationCodes.invalid,
                message = context.getString(R.string.payrdr_order_incorrect_length),
                regex = PATTERN.toRegex()
            )
        )
    }

    companion object {
        private const val MIN_LENGTH = 1
        private const val PATTERN = "\\S+"
    }
}
