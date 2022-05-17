package net.payrdr.mobile.payment.sdk.core.validation

import android.content.Context
import net.payrdr.mobile.payment.sdk.core.R
import net.payrdr.mobile.payment.sdk.core.validation.rules.LuhnStringRule
import net.payrdr.mobile.payment.sdk.core.validation.rules.RegExRule
import net.payrdr.mobile.payment.sdk.core.validation.rules.StringLengthRule
import net.payrdr.mobile.payment.sdk.core.validation.rules.StringRequiredRule

/**
 * Card number value validator.
 *
 * @param context context for getting string resources.
 */
class CardNumberValidator(context: Context) : BaseValidator<String>() {

    init {
        addRules(
            StringRequiredRule(
                code = ValidationCodes.required,
                message = context.getString(R.string.payrdr_card_incorrect_number)
            ),
            RegExRule(
                code = ValidationCodes.invalidFormat,
                message = context.getString(R.string.payrdr_card_incorrect_number),
                regex = PATTERN
            ),
            StringLengthRule(
                code = ValidationCodes.invalid,
                message = context.getString(R.string.payrdr_card_incorrect_length),
                minLength = MIN_LENGTH,
                maxLength = MAX_LENGTH
            ),
            LuhnStringRule(
                code = ValidationCodes.invalid,
                message = context.getString(R.string.payrdr_card_incorrect_number)
            )
        )
    }

    companion object {
        private const val MIN_LENGTH = 16
        private const val MAX_LENGTH = 19
        private val PATTERN = "[0-9]+".toRegex()
    }
}
