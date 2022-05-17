package net.payrdr.mobile.payment.sdk.core.validation

import android.content.Context
import net.payrdr.mobile.payment.sdk.core.R
import net.payrdr.mobile.payment.sdk.core.validation.rules.RegExRule
import net.payrdr.mobile.payment.sdk.core.validation.rules.StringLengthRule
import net.payrdr.mobile.payment.sdk.core.validation.rules.StringRequiredRule

/**
 * Cardholder name value validator.
 *
 * @param context context for getting string resources.
 */
class CardHolderValidator(context: Context) : BaseValidator<String>() {

    init {
        addRules(
            StringRequiredRule(
                code = ValidationCodes.required,
                message = context.getString(R.string.payrdr_card_incorrect_card_holder)
            ),
            StringLengthRule(
                code = ValidationCodes.invalid,
                message = context.getString(R.string.payrdr_card_incorrect_card_holder),
                maxLength = MAX_LENGTH
            ),
            RegExRule(
                code = ValidationCodes.invalidFormat,
                message = context.getString(R.string.payrdr_card_incorrect_card_holder),
                regex = PATTERN
            )
        )
    }

    companion object {
        private const val MAX_LENGTH = 30
        private val PATTERN = "[a-zA-Z ]+".toRegex()
    }
}
