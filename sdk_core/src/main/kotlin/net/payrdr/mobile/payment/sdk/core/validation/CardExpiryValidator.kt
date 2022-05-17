package net.payrdr.mobile.payment.sdk.core.validation

import android.content.Context
import net.payrdr.mobile.payment.sdk.core.R
import net.payrdr.mobile.payment.sdk.core.validation.rules.ExpiryRule
import net.payrdr.mobile.payment.sdk.core.validation.rules.RegExRule
import net.payrdr.mobile.payment.sdk.core.validation.rules.StringRequiredRule

/**
 * Card expiration value validator.
 *
 * @param context context for getting string resources.
 */
class CardExpiryValidator(context: Context) : BaseValidator<String>() {

    init {
        addRules(
            StringRequiredRule(
                code = ValidationCodes.required,
                message = context.getString(R.string.payrdr_card_incorrect_expiry)
            ),
            RegExRule(
                code = ValidationCodes.invalidFormat,
                message = context.getString(R.string.payrdr_card_incorrect_expiry),
                regex = PATTERN
            ),
            ExpiryRule(
                code = ValidationCodes.invalid,
                message = context.getString(R.string.payrdr_card_incorrect_expiry)
            )
        )
    }

    companion object {
        private val PATTERN = "^\\d{2}/\\d{2}".toRegex()
    }
}
