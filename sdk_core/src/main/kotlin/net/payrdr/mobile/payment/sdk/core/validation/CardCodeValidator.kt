package net.payrdr.mobile.payment.sdk.core.validation

import android.content.Context
import net.payrdr.mobile.payment.sdk.core.R
import net.payrdr.mobile.payment.sdk.core.validation.rules.RegExRule
import net.payrdr.mobile.payment.sdk.core.validation.rules.StringLengthRule
import net.payrdr.mobile.payment.sdk.core.validation.rules.StringRequiredRule

/**
 * Validator of the value of the secret code of the card.
 *
 * @param context context for getting string resources.
 */
class CardCodeValidator(context: Context) : BaseValidator<String>() {

    init {
        addRules(
            StringRequiredRule(
                code = ValidationCodes.required,
                message = context.getString(R.string.payrdr_card_incorrect_cvc)
            ),
            StringLengthRule(
                code = ValidationCodes.invalid,
                message = context.getString(R.string.payrdr_card_incorrect_cvc),
                minLength = MIN_LENGTH,
                maxLength = MAX_LENGTH
            ),
            RegExRule(
                code = ValidationCodes.invalid,
                message = context.getString(R.string.payrdr_card_incorrect_cvc),
                regex = PATTERN
            )
        )
    }

    companion object {
        private const val MIN_LENGTH = 3
        private const val MAX_LENGTH = 3
        private val PATTERN = "[0-9]+".toRegex()
    }
}
